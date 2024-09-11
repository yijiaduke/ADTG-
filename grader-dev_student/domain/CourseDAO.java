package edu.duke.adtg.domain;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO implements  DAOFactory<Course>{

    private DAOConn conn;
    private Connection connection;

    public CourseDAO(DAOConn conn) {
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


    //==============================================================================
    //============= Webapp use =====================================================
    //==============================================================================

     // Static factory method to create a course from a ResultSet
     public static Course createCourseFromRS(ResultSet rs) throws SQLException {
        return new Course(
            rs.getString("c_subject").trim(),
            rs.getInt("c_number")
        );
    }

      //==============================================================================
    //============= Webapp use =====================================================
    //==============================================================================




    // @Override
    public void map(ResultSet rs,Course course) throws SQLException {
        if (!rs.next()) {
            // No data found
            return;
        }

        course.setTitle(rs.getString("title"));
        course.setRepo(rs.getString("gitlab_repo"));
    }


    public void mapSections(ResultSet rs,Course course) throws SQLException {
        List<Section> sections = new ArrayList<>();
        if (!rs.next()) {
            course.setSectionList(null);
            return;
        }

        while (rs.next()) {
            Section section = new Section(rs.getInt("sec_id"),course);
            section.setInstructor(new Instructor(rs.getString("instructor_netid"),null,null));
            section.setDate(rs.getDate("startdate").toLocalDate());
            sections.add(section);
        }
        course.setSectionList(sections);
    }

    public void load(Course course) {
            String sql = "SELECT * FROM adtg.course WHERE c_subject = ? AND c_number = ?";
    
            try (PreparedStatement ps1 = connection.prepareStatement(sql)
            ) {
                setStatementObjects(ps1, course.getSubject(), course.getNumber());
                ps1.execute();
                ResultSet rs = ps1.getResultSet();
                map(rs, course);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    // @Override
    // public void save(Course course) {
    //     String sql = "INSERT INTO adtg.course (c_subject, c_number, title, gitlab_repo) VALUES (?,?, ?, ?);";
    //     try (PreparedStatement ps = connection.prepareStatement(sql)) {
    //         setStatementObjects(ps, course.getSubject(), course.getNumber(),course.getTitle(), course.getRepo());
    //         setStatementObjects(ps, course.getSubject(), course.getNumber(),course.getTitle(), course.getRepo());
    //         ps.executeUpdate();
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    // }


    // @Override
    // public void load(Course course) {
    //     String sql = "SELECT * FROM adtg.course WHERE c_subject = ? AND c_number = ?";
    //     String sectionSql = "SELECT s.sec_id, s.instructor_netid, s.start_date " +
    //             "FROM adtg.section s " +
    //             "JOIN adtg.course c ON c.c_subject = s.c_subject AND c.c_number = s.c_number " +
    //             "WHERE s.c_subject = ? AND s.c_number = ?;";
    //     String assessementSql = "SELECT a.assn, a.title, a.start_date, a.due, a.category, a.max_score, " +
    //             "a.passing_score, a.is_extra_credit, a.test_cmd " +
    //             "FROM adtg.assessment a " +
    //             "JOIN adtg.course c ON c.c_subject = a.c_subject AND c.c_number = a.c_number " +
    //             "WHERE a.c_subject = ? AND a.c_number = ?;";

    //     try (PreparedStatement ps1 = connection.prepareStatement(sql);
    //          PreparedStatement ps2 = connection.prepareStatement(sectionSql);
    //          PreparedStatement ps3 = connection.prepareStatement(assessementSql)
    //     ) {
    //         setStatementObjects(ps1, course.getSubject(), course.getNumber());
    //         ps1.execute();
    //         ResultSet rs = ps1.getResultSet();
    //         map(rs, course);
    //         setStatementObjects(ps2,course.getSubject(), course.getNumber());
    //         ps2.execute();
    //         rs = ps2.getResultSet();
    //         mapSections(rs,course);
    //         // setStatementObjects(ps3,course.getSubject(), course.getNumber());
    //         // ps3.execute();
    //         // rs = ps3.getResultSet();
    //         // mapAssessment(rs,course);
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    // }

    // @Override
    // public void delete(Course course) {
    //     String sql = "DELETE FROM adtg.course WHERE c_subject = ? AND c_number = ?";
    //     try {
    //         PreparedStatement ps = connection.prepareStatement(sql);
    //         setStatementObjects(ps,course.getSubject(),course.getTitle());
    //         ps.executeUpdate();
    //     } catch (SQLException e) {
    //         throw new RuntimeException(e);
    //     }
    // }


    // @Override
    // public void removeAll() {
    //     String sql = "DELETE FROM adtg.course";
    //     try (PreparedStatement ps = connection.prepareStatement(sql)) {
    //         ps.executeUpdate();
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    // }

}

