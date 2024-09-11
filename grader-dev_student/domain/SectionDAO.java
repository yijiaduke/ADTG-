package edu.duke.adtg.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SectionDAO implements DAOFactory<Section>{
    private DAOConn conn;
    private Connection connection;

    public SectionDAO(DAOConn conn) {
        this.conn = conn;
        this.connection = getConnection();
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

    // @Override
    // public void add(Section section) {
    //     String sql = "INSERT INTO Section (sectionid, courseid, instructorid, date,title) VALUES (?, ?, ?, ?,?)";

    //     String instructorId = section.getInstructor() != null ? section.getInstructor().getNetId() : null;

    //     try (PreparedStatement ps = connection.prepareStatement(sql)) {
    //         setStatementObjects(ps, section.getSectionId(),section.getCourse().getCourseId(),
    //                 instructorId, section.getDate(), section.getTitle());
    //         ps.executeUpdate();
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    // }

    // @Override
    // public Section getById(String id) {
    //     String sectionSql = "SELECT s.sectionid, s.courseid, s.date, s.title, u.netid AS instructorid, u.email, u.name " +
    //             "FROM section s " +
    //             "LEFT JOIN users u ON s.instructorid = u.netid " +
    //             "WHERE s.sectionid = '"+id+"'";

    //     String studentsSql = "SELECT u.netid, u.email, u.name " +
    //             "FROM enrollment e " +
    //             "JOIN users u ON e.netid = u.netid " +
    //             "WHERE e.sectionid = '"+id+"'";

    //     try (PreparedStatement sectionPs = connection.prepareStatement(sectionSql);
    //          PreparedStatement studentsPs = connection.prepareStatement(studentsSql)) {
    //         ResultSet secInsRs = sectionPs.executeQuery();
    //         Section section = map(secInsRs);
    //         ResultSet secStuRs = studentsPs.executeQuery();
    //         mapStudents(secStuRs,section);
    //         return section;

    //     }catch (SQLException e){
    //         throw new RuntimeException(e);
    //     }
    // }


    // @Override
    // public void deleteById(String id) {
    //     String sql = "DELETE FROM section WHERE sectionId = '"+id+"'";
    //     try (PreparedStatement ps = connection.prepareStatement(sql)) {
    //         ps.executeUpdate();
    //     } catch (SQLException e) {
    //         throw new RuntimeException(e);
    //     }
    // }

    // @Override
    // public void update(Section section) {
    //     String sql = "UPDATE section SET courseid = ?, instructorid = ?, date = ?, title = ? WHERE sectionid = ?";

    //     String instructorId = section.getInstructor() != null ? section.getInstructor().getNetId() : null;

    //     try (PreparedStatement ps = connection.prepareStatement(sql)) {
    //         setStatementObjects(ps,section.getCourse().getCourseId(),
    //                 instructorId, section.getDate(), section.getTitle());
    //         ps.executeUpdate();
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    // }

    // @Override
    public void map(ResultSet rs,Section section) throws SQLException {
        if (!rs.next()) {
            return;
        }

//        Course course = courseDAO.load();
//        Section section = new Section(rs.getString("sectionid"),course);
       mapInstructor(rs, section);

    }

    

    //==============================================================================
    //============= Webapp use =====================================================
    //==============================================================================
    public Section mapSectionFromRS(ResultSet rs) throws SQLException {
        Course course = new Course(rs.getString("c_subject"), rs.getInt("c_number"));
        Integer sectionId = rs.getInt("sec_id");
        Section section = new Section(sectionId,course);
        return section;
    }

    
    public Section getSectionsByPK(String courseSubject, int courseNumber, int sectionId) {
        String sql = "SELECT * FROM section WHERE c_subject = ? AND c_number = ? AND sec_id = ?;";
        Section section = null;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, courseSubject);
            ps.setInt(2, courseNumber);
            ps.setInt(3, sectionId);
    
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { // Assuming only one result is expected
                    section = mapSectionFromRS(rs);
                } else {
                    return null; // TODO: handle here
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while retrieving section", e);
        }
        return section;
    }


    //==============================================================================
    //============= Webapp use =====================================================
    //==============================================================================


    public void mapInstructor(ResultSet rs,Section section) throws SQLException {
        if(rs.getString("instructor_netid")!=null && section!=null) {
            Instructor instructor = new Instructor(
                    rs.getString("netid"), rs.getString("name"), rs.getString("email"));
            section.setInstructor(instructor);
        }
    }

    public void mapStudents(ResultSet rs,Section section) throws SQLException {
        if(rs.getString("netid")!=null && section!=null) {
            List<Student> enrolledStudents = new ArrayList<>();
            while (rs.next()) {
                Student student = new Student(
                        rs.getString("netid"),
                        rs.getString("email"),
                        rs.getString("name")
                );
                enrolledStudents.add(student);
            }
            section.setEnrollStudents(enrolledStudents);
        }
    }


    
    


}

