package edu.duke.adtg.domain;

import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AssessmentDAOTest {

    // private static DAOConn daoConn;
    // private static AssessmentDAO assessmentDAO;
    // private static GradeDAO gradeDAO;

    // @BeforeAll
    // public static void setUp(){
    //     daoConn = new DAOConn();
    //     assessmentDAO = new AssessmentDAO(daoConn);
        
    // }

    // // @Test
    // // void testGetGradesForStudentAssn() throws SQLException {
    // //     // Fetch grades for each assessment
    // //     List<Grade> gradesListHW1 = gradeDAO.getGradesForStudentAssn("yz123", "CS", 101, "cs101_hw1");
    // //     List<Grade> gradesListHW2 = gradeDAO.getGradesForStudentAssn("yz123", "CS", 101, "cs101_hw2");
    // //     List<Grade> gradesListHW5 = gradeDAO.getGradesForStudentAssn("yz123", "CS", 101, "cs101_hw5");

    // //     // Verify the size of each list according to your data
    // //     assertEquals(3, gradesListHW1.size(), "HW1 should have 3 grades.");
    // //     assertEquals(2, gradesListHW2.size(), "HW2 should have 2 grades.");
    // //     assertEquals(2, gradesListHW5.size(), "HW5 should have 2 grades.");

    // //     Grade gradeHW1Latest = gradesListHW1.get(0); // Assuming the latest grade is first due to ORDER BY
    // //    LocalDateTime expectedGradeTime = LocalDateTime.of(2024, 6, 20, 23, 55, 00);

    // //     // getFinalGrade() returns a BigDecimal
    // //     BigDecimal expectedGrade = new BigDecimal("100.00");
    // //     BigDecimal actualGrade = gradeHW1Latest.getFinalGrade();
    // //     assertTrue(expectedGrade.compareTo(actualGrade) == 0, "Check final grade of the latest HW1 grade.");
    // //     assertEquals(expectedGradeTime, gradeHW1Latest.getGradeTime());
    // // }

    // @Test
    // void testGetPastDueAssessments() throws SQLException {

    //     List<Assessment> assessments = assessmentDAO.getPastDueAssessments("yz853", "CS", 101, "cs101_hw2");

    //     assertNotNull(assessments, "The list of assessments should not be null.");

    //     // Example assertions based on expected data
    //     Assessment assn1 = assessments.get(0);
    //     assertEquals("cs101_hw2", assn1.getAssn());
    //     assertTrue(assn1.getDueDate().isBefore(LocalDateTime.now()));
    //     // assertTrue(assn1.getPassingScore() > assn1.getFinalGrade());
    // }

    // @Test
    // void testGetAssessmentsInTwoWeeks() throws SQLException {
    //     Course testCourse = new Course("CS", 101, "Intro to Computer Science");

    //     List<Assessment> assessments = assessmentDAO.getAssessmentsInTwoWeeks(testCourse);
    //     assertNotNull(assessments, "The returned list should not be null.");


    //     assertEquals(2, assessments.size());
    //     // More specific tests can be added, such as checking the due dates of returned assessments
    //     assessments.forEach(assessment -> {
    //         assertTrue(
    //             assessment.getDueDate().isAfter(LocalDateTime.now()) && 
    //             assessment.getDueDate().isBefore(LocalDateTime.now().plusWeeks(2)),
    //             "Each assessment's due date should be within the next two weeks."
    //         );
    //     Assessment ass0 = assessments.get(0);
    //     Assessment ass1 = assessments.get(1);
    //     LocalDateTime expectedDueDate0 = LocalDateTime.of(2024, 6, 27, 23, 59, 59);
    //     LocalDateTime expectedDueDate1 = LocalDateTime.of(2024, 6, 28, 23, 59, 59);

    //     assertEquals("cs101_hw1", ass0.getAssn());
    //     assertEquals("cs101_hw5", ass1.getAssn());
    //     assertEquals(expectedDueDate0, ass0.getDueDate(), "Check due date for cs101_hw1");
    //     assertEquals(expectedDueDate1, ass1.getDueDate(), "Check due date for cs101_hw5");

    //     });
    // }




}
