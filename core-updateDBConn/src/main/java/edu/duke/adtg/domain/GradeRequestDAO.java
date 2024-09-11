package edu.duke.adtg.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Repository
public class GradeRequestDAO implements DAOFactory<GradeRequest> {

    private final DAOConn conn;
    private static final Logger logger = LoggerFactory.getLogger(GradeRequestDAO.class);

    @Autowired
    public GradeRequestDAO(DAOConn conn) {
        this.conn = conn;
    }

    @Override
    public void setConnection(DAOConn conn) {
    }

    @Override
    public Connection getConnection() {
        try {
            return conn.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

   

    //--------------------------------- Helper ------------------------------------------------



    private GradeRequest createGradeRequestFromRS(ResultSet rs) throws SQLException {
        // Constructing Assessment from the result set
        Assessment assessment = new Assessment();
        Course course = new Course();
        course.setSubject(rs.getString("c_subject").trim());
        course.setNumber(rs.getInt("c_number"));
        assessment.setCourse(course);
        assessment.setAssn(rs.getString("assn"));
        AssessmentDAO assessmentDAO = new AssessmentDAO(conn);
        assessmentDAO.load(assessment);
        // Constructing Student from the result set
        Student student = new Student(rs.getString("netid"));

        // Handling potential null values
        LocalDateTime requestTime = rs.getTimestamp("tmstmp").toLocalDateTime();
        String status = rs.getString("status");
        LocalDateTime expiresTime = rs.getTimestamp("expires_on") != null ? rs.getTimestamp("expires_on").toLocalDateTime() : null;
        String owner = rs.getString("owner");
        String logText = rs.getString("log_text");
        String commitID = rs.getString("commitID");
        String applyPenaltyStr = rs.getString("apply_penalty");
        char applyPenalty = (applyPenaltyStr != null) ? applyPenaltyStr.charAt(0) : 'Y'; // Default to 'Y' if null

        return new GradeRequest(
            rs.getLong("id"), assessment, student, requestTime, status, expiresTime, owner, logText, commitID, applyPenalty );
    }

    public void updateGradeRequest(GradeRequest gradeRequest) throws SQLException {
        String sql = "UPDATE adtg.grade_request SET status = ?, expires_on = ?, owner = ?, log_text = ?, commitid = ?, apply_penalty = ? WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, gradeRequest.getStatus());
            ps.setObject(2, gradeRequest.getExpiresTime() != null ? Timestamp.valueOf(gradeRequest.getExpiresTime()) : null);
            ps.setString(3, gradeRequest.getOwner());
            ps.setString(4, gradeRequest.getLogText());
            ps.setString(5, gradeRequest.getCommitID());
            ps.setString(6, String.valueOf(gradeRequest.getApplyPenalty()));
            ps.setLong(7, gradeRequest.getId());

            ps.executeUpdate();
        }
    }

    //----------------------------------- CRUD -------------------------------------------------

    public GradeRequest getGradeRequestById(Long requestId) throws SQLException {
        String sql = "SELECT * FROM adtg.grade_request WHERE ID = ?";
        GradeRequest gradeRequest = null;

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, requestId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    gradeRequest = createGradeRequestFromRS(rs);
                }
            }
        }
        return gradeRequest;
    }

    public void insertGradeRequest(GradeRequest gradeRequest) throws SQLException {
        String sql = "INSERT INTO adtg.grade_request (c_subject, c_number, assn, netid, tmstmp, status) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, gradeRequest.getAssessment().getCourse().getSubject());
            ps.setInt(2, gradeRequest.getAssessment().getCourse().getNumber());
            ps.setString(3, gradeRequest.getAssessment().getAssn());
            ps.setString(4, gradeRequest.getStudent().getNetId());
            ps.setObject(5, Timestamp.valueOf(gradeRequest.getRequestTime()));
            ps.setString(6, gradeRequest.getStatus());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long generatedId = rs.getLong(1);
                gradeRequest.setId(generatedId);
            }
        }
    }


    // Load by Assessment and Student
    public GradeRequest getLatestGradeRequest(Assessment assessment, String netId ) throws SQLException {
        String sql = "SELECT * FROM grade_request WHERE c_subject = ? AND c_number = ? AND assn = ? AND netid = ? ORDER BY id DESC LIMIT 1";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, assessment.getCourse().getSubject());
            ps.setInt(2, assessment.getCourse().getNumber());
            ps.setString(3, assessment.getAssn());
            ps.setString(4, netId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return createGradeRequestFromRS(rs);
                }
            }
        }
        return null;
    }


    public List<GradeRequest> listGradeRequest(Assessment assessment, String netId) throws SQLException {
        List<GradeRequest> gradeRequests = new ArrayList<>();
        String sql = "SELECT * FROM grade_request WHERE c_subject = ? AND c_number = ? AND assn = ? AND netid = ? ORDER BY id DESC";
        
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, assessment.getCourse().getSubject());
            ps.setInt(2, assessment.getCourse().getNumber());
            ps.setString(3, assessment.getAssn());
            ps.setString(4, netId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GradeRequest gradeRequest = createGradeRequestFromRS(rs);
                    gradeRequests.add(gradeRequest);
                }
            }
        }
        return gradeRequests;
    }

    public List<GradeRequest> listGradeRequestByStatus(String status) throws SQLException {
        List<GradeRequest> gradeRequests = new ArrayList<>();
        String sql = "SELECT * FROM grade_request WHERE status = ? ORDER BY id DESC";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GradeRequest gradeRequest = createGradeRequestFromRS(rs);
                    gradeRequests.add(gradeRequest);
                }
            }
        }
        return gradeRequests;
    }


    


}
