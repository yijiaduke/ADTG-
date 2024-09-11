package edu.duke.adtg;
import org.junit.jupiter.api.Test;

import edu.duke.adtg.Grader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GraderTest {
    @Test
    void testExtractNetId() {
        String path = "path/to/file_yz123";
        String expectedNetId = "yz123";
        
        String netId = Grader.extractNetId(path);
        
        assertEquals(expectedNetId, netId);
    }
}