package edu.duke.adtg.domain;

import java.sql.*;
import java.util.*;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GradeRequestDAO implements DAOFactory<GradeRequest>{

    private DAOConn conn;
    private Connection connection;
    private static final Logger logger = LoggerFactory.getLogger(AssnDeadlineDAO.class);

    public GradeRequestDAO(DAOConn conn){
        this.conn = conn;
        connection = getConnection();
    }

    @Override
    public void setConnection(DAOConn conn) {
        this.conn = conn;
        connection = getConnection();
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

    public void updateGradeRequest(GradeRequest grade_Request, String status, String owner, String log) {
        try {
            grade_Request.setStatus(status);
            grade_Request.setOwner(owner);
            grade_Request.setLogText(log);
            this.updateGradeRequest(grade_Request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    /**
     * Retrieves a list of pending grade requests from the database.
     *
     * @return a list of GradeRequest objects representing the pending grade requests
     * @throws SQLException if an error occurs while accessing the database
     */
    public List<GradeRequest> listPendingGradeRequest() throws SQLException {
        List<GradeRequest> gradeRequests = new ArrayList<>();
        String sql = "SELECT * FROM grade_request WHERE status = ? AND (owner IS NULL OR LENGTH(owner) = 0) ORDER BY id DESC";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "PENDING");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GradeRequest gradeRequest = createGradeRequestFromRS(rs);
                    gradeRequests.add(gradeRequest);
                }
            }
        }
        return gradeRequests;
    }
    /**
     * Checks if a grade request is pending and no owner.
     *
     * @param gradeRequest the grade request to check
     * @return true if the grade request is pending, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public Boolean isPendingRequestAndNoOwner(GradeRequest gradeRequest) throws SQLException {
        String sql = "SELECT * FROM grade_request WHERE status = ? AND id = ? AND (owner IS NULL OR LENGTH(owner) = 0)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "PENDING");
            ps.setLong(2, gradeRequest.getId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        
        return false;
    }
    /**
     * Checks the status and ownership of a grade request.
     *
     * @param gradeRequest The grade request to check.
     * @param status The status to compare against.
     * @param owner The owner to compare against.
     * @return {@code true} if the grade request has the specified status and owner, {@code false} otherwise.
     */
    public Boolean checkStatusAndOwnership(GradeRequest gradeRequest, String status, String owner) {
        String sql = "SELECT * FROM grade_request WHERE status = ? AND owner = ? AND id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, owner);
            ps.setLong(3, gradeRequest.getId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    


}
