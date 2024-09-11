package edu.duke.adtg;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import edu.duke.adtg.Grader;
import edu.duke.adtg.domain.*;
import java.util.List;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class GraderTest {
    @Test
    void testExtractNetId() {
        String path = "path/to/file_yz123";
        String expectedNetId = "yz123";
        
        String netId = Grader.extractNetId(path);
        
        assertEquals(expectedNetId, netId);
    }
    // @Test
    // void testCalculatePenalty() {
    //     LocalDateTime dueDateTime = LocalDateTime.of(2022, 1, 1, 12, 0);
    //     LocalDateTime submissionDateTime = LocalDateTime.of(2022, 1, 2, 12, 0);
    //     BigDecimal grade = BigDecimal.valueOf(0);
    //     String formula = "$@grade * @hour$";
    //     Assessment assessment = new Assessment(new Course("CS", 101), "assn");
    //     GradeRequest gradeRequest = new GradeRequest((Long.valueOf(100)), assessment, new Student("yz123"), submissionDateTime, "DONE", LocalDateTime.now(), "owner", "logText", "commitID", 'Y');
    //     try {
    //         List<GradeRequest> gradeRequests = new GradeRequestDAO(new DAOConn()).listGradeRequestByStatus("DONE");
    //         gradeRequests.add(gradeRequest);
    //         Grader grader = new Grader(gradeRequests);
    //         BigDecimal penalty = grader.calculatePenalty(dueDateTime, submissionDateTime, grade, formula);
    //         System.out.println(penalty);

    //         assertEquals(BigDecimal.valueOf(0), penalty);
    //         BigDecimal penalty2 = grader.calculatePenalty(dueDateTime, dueDateTime, grade, formula);
    //         assertEquals(BigDecimal.valueOf(0), penalty2);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }
    
}