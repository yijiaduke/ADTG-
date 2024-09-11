package edu.duke.delivery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ResourceLoader;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edu.duke.adtg.domain.Assessment;
import edu.duke.adtg.domain.AssessmentDAO;
import edu.duke.adtg.domain.AssnDeadlineDAO;
import edu.duke.adtg.domain.Course;
import edu.duke.adtg.domain.DAOConn;
import edu.duke.adtg.domain.Delivery;
import edu.duke.adtg.domain.DeliveryDAO;
import edu.duke.adtg.domain.Grade;
import edu.duke.adtg.domain.GradeDAO;
import edu.duke.adtg.domain.PrerequisiteDAO;
import edu.duke.adtg.domain.Section;
import edu.duke.adtg.domain.Status;
import edu.duke.adtg.domain.Student;

@Disabled("Local address may change for different PC/VM")
public class RepoFunctionLibTest {

    static DAOConn DaoConn = new DAOConn();
    RepoFuncLib test = new RepoFuncLib();
    
    @BeforeEach
    private void setUp(){
        Properties properties = new Properties();
        try (InputStream input = ResourceLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            // Load the properties file
            properties.load(input);

            // Access properties
            test.setSourceRepo(properties.getProperty("sourceRepoId"));
            test.setGroupId(properties.getProperty("targetGroupId"));
            test.setToken(properties.getProperty("targetGroupToken"));
            test.setSourceToken(properties.getProperty("sourceAssnToken"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void addScript(){
        try (Connection conn = DaoConn.getConnection();
            Statement stmt = conn.createStatement()) {

            // Insert into adtg.course
            stmt.executeUpdate("INSERT INTO adtg.course (c_subject, c_number, title, gitlab_repo, gitlab_token) VALUES " +
                    "('CS', 101, 'Intro to Computer Science', 'testing/Repo_13428', 'glpat-TQH4Ssiw_cNaxk5zbNwW'), "+
                    "('CS', 202, 'CSE Programming', '/home/xl435/data/Assessments', 'NOTOKEN')");

            // Insert into adtg.users
            stmt.executeUpdate("INSERT INTO adtg.users (netid, email, name) VALUES " +
                    "('yz853', 'yz853@duke.edu', 'Yz User'), " +
                    "('xl435', 'xl435@duke.edu', 'Louise'), " +
                    "('someone', 'someone@duke.edu', 'someone'), " +
                    "('xl435test', 'Louisexyli@hotmail.com', 'TestAcc'), " +
                    "('inst1', 'inst1@duke.edu', 'Inst1')");

            // Insert into adtg.section
            stmt.executeUpdate("INSERT INTO adtg.section (c_subject, c_number, sec_id, instructor_netid, startdate, gitlab_group, gitlab_token) " +
                    "VALUES ('CS', 101, 1, 'inst1', DATE '2024-07-10', 'jpastorino/adtg-test', 'glpat-ciy4u-diLpK12yiKnDDk'), "+
                    "('CS', 202, 1, 'inst1', DATE '2024-07-10', 'jpastorino/adtg-test', 'glpat-ciy4u-diLpK12yiKnDDk')");

            // Insert into adtg.enrollment
            stmt.executeUpdate("INSERT INTO adtg.enrollment (c_subject, c_number, sec_id, student_netid) VALUES " +
                    "('CS', 101, 1, 'xl435test'), " +
                    "('CS', 101, 1, 'yz853'), " +
                    "('CS', 101, 1, 'xl435'), " +
                    "('CS', 202, 1, 'xl435test'), " +
                    "('CS', 202, 1, 'yz853'), " +
                    "('CS', 202, 1, 'xl435')");

            // Insert into adtg.assn_category
            stmt.executeUpdate("INSERT INTO adtg.assn_category (category) VALUES " +
                    "('Formative'), " +
                    "('Evaluative')");

            // Insert into adtg.assessment
            stmt.executeUpdate("INSERT INTO adtg.assessment (c_subject, c_number, assn, title, start_date, due, category, max_score, passing_score, is_extra_credit, test_cmd, token_req) VALUES " +
                    "('CS', 101, 'cs101_FIRST_STEP', 'FIRST_STEP', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-06-19 23:59:59', 'Formative', 1.00, 1.00, false, 'python test.py', 0), " +
                    "('CS', 101, 'cs101_hw2', 'Homework 2', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-06-19 23:59:59', 'Formative', 100.00, 80.00, false, 'python test_hw2.py', 1), " +
                    "('CS', 101, 'cs101_hw5', 'Homework 5', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-06-28 23:59:59', 'Formative', 100.00, 80.00, false, 'python test_hw5.py', 1), " +
                    "('CS', 101, 'cs101_hw4', 'Homework 4', TIMESTAMP '2024-06-20 00:00:00', TIMESTAMP '2024-07-07 23:59:59', 'Formative', 100.00, 80.00, false, 'python test_hw4.py', 1), " +
                    "('CS', 101, 'cs101_hw1', 'Homework 1', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-06-27 23:59:59', 'Formative', 100.00, 80.00, false, 'python test_hw1.py', 1), " +
                    "('CS', 101, 'cs101_eval1', 'Eval1', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-06-28 23:59:59', 'Evaluative', 100.00, 80.00, false, 'python test_eval1', 0), " +
                    "('CS', 101, 'cs101_eval2', 'Eval2', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-07-01 23:59:59', 'Evaluative', 100.00, 80.00, false, 'python test_eval2.py', 0), " +
                    "('CS', 101, 'cs101_eval3', 'Eval3', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-07-07 23:59:59', 'Evaluative', 100.00, 80.00, false, 'python test_eval3.py', 0), "+ 
                    "('CS', 202, 'FIRST_STEP', 'FIRST_STEP', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-06-19 23:59:59', 'Formative', 1.00, 1.00, false, 'python test.py', 0), " +
                    "('CS', 202, 'cs202_hw2', 'Homework 2', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-06-19 23:59:59', 'Formative', 100.00, 80.00, false, 'python test_hw2.py', 1), " +
                    "('CS', 202, 'cs202_hw5', 'Homework 5', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-06-28 23:59:59', 'Formative', 100.00, 80.00, false, 'python test_hw5.py', 1), " +
                    "('CS', 202, 'cs202_hw4', 'Homework 4', TIMESTAMP '2024-06-20 00:00:00', TIMESTAMP '2024-07-07 23:59:59', 'Formative', 100.00, 80.00, false, 'python test_hw4.py', 1), " +
                    "('CS', 202, 'cs202_hw1', 'Homework 1', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-06-27 23:59:59', 'Formative', 100.00, 80.00, false, 'python test_hw1.py', 1), " +
                    "('CS', 202, 'cs202_eval1', 'Eval1', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-06-28 23:59:59', 'Evaluative', 100.00, 80.00, false, 'python test_eval1', 0), " +
                    "('CS', 202, 'cs202_eval2', 'Eval2', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-07-01 23:59:59', 'Evaluative', 100.00, 80.00, false, 'python test_eval2.py', 0)");

            // Insert into adtg.prerequisite
            stmt.executeUpdate("INSERT INTO adtg.prerequisite (c_subject, c_number, assn, pre_c_subject, pre_c_number, pre_assn) VALUES " +
                    "('CS', 101, 'cs101_hw2', 'CS', 101, 'cs101_hw1'), " +
                    "('CS', 101, 'cs101_hw4', 'CS', 101, 'cs101_hw2'), " +
                    "('CS', 101, 'cs101_eval1', 'CS', 101, 'cs101_hw2'), " +
                    "('CS', 101, 'cs101_eval2', 'CS', 101, 'cs101_eval1'), " +
                    "('CS', 101, 'cs101_eval3', 'CS', 101, 'cs101_eval2'), " +
                    "('CS', 101, 'cs101_hw5', 'CS', 101, 'cs101_hw4'), " +
                    "('CS', 202, 'cs202_hw2', 'CS', 202, 'cs202_hw1'), " +
                    "('CS', 202, 'cs202_hw4', 'CS', 202, 'cs202_hw2'), " +
                    "('CS', 202, 'cs202_eval1', 'CS', 202, 'cs202_hw2'), " +
                    "('CS', 202, 'cs202_eval2', 'CS', 202, 'cs202_eval1'), " +
                    "('CS', 202, 'cs202_hw5', 'CS', 202, 'cs202_hw4')"
                    );

            System.out.println("Data inserted successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error inserting data: " + e.getMessage());
        }
    }

    public static void deleteScript(){
        try (Connection conn = DaoConn.getConnection();
            Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("DELETE from adtg.grades where c_subject = 'CS';");
            stmt.executeUpdate("DELETE from adtg.assn_deadline where c_subject = 'CS';");
            stmt.executeUpdate("DELETE from adtg.delivery where c_subject = 'CS';");
            // Delete specific data from adtg.prerequisite
            // stmt.executeUpdate(
            //     "DELETE FROM adtg.prerequisite " +
            //     "WHERE (c_subject = 'CS' AND c_number = 101 AND assn = 'cs101_hw2' AND pre_c_subject = 'CS' AND pre_c_number = 101 AND pre_assn = 'cs101_hw1') OR " +
            //     "(c_subject = 'CS' AND c_number = 101 AND assn = 'cs101_hw4' AND pre_c_subject = 'CS' AND pre_c_number = 101 AND pre_assn = 'cs101_hw2') OR " +
            //     "(c_subject = 'CS' AND c_number = 101 AND assn = 'cs101_eval1' AND pre_c_subject = 'CS' AND pre_c_number = 101 AND pre_assn = 'cs101_hw2') OR " +
            //     "(c_subject = 'CS' AND c_number = 101 AND assn = 'cs101_eval2' AND pre_c_subject = 'CS' AND pre_c_number = 101 AND pre_assn = 'cs101_eval1') OR " +
            //     "(c_subject = 'CS' AND c_number = 101 AND assn = 'cs101_eval3' AND pre_c_subject = 'CS' AND pre_c_number = 101 AND pre_assn = 'cs101_eval2') OR " +
            //     "(c_subject = 'CS' AND c_number = 101 AND assn = 'cs101_hw5' AND pre_c_subject = 'CS' AND pre_c_number = 101 AND pre_assn = 'cs101_hw4')");
            stmt.executeUpdate("DELETE FROM adtg.prerequisite where c_subject = 'CS';");

            // Delete specific data from adtg.assessment
            // stmt.executeUpdate(
            //     "DELETE FROM adtg.assessment " +
            //     "WHERE (c_subject = 'CS' AND c_number = 101 AND assn = 'cs101_hw2' AND title = 'Homework 2') OR " +
            //     "(c_subject = 'CS' AND c_number = 101 AND assn = 'cs101_FIRST_STEP' AND title = 'FIRST_STEP') OR " +
            //     "(c_subject = 'CS' AND c_number = 101 AND assn = 'cs101_hw5' AND title = 'Homework 5') OR " +
            //     "(c_subject = 'CS' AND c_number = 101 AND assn = 'cs101_hw4' AND title = 'Homework 4') OR " +
            //     "(c_subject = 'CS' AND c_number = 101 AND assn = 'cs101_hw1' AND title = 'Homework 1') OR " +
            //     "(c_subject = 'CS' AND c_number = 101 AND assn = 'cs101_eval1' AND title = 'Eval1') OR " +
            //     "(c_subject = 'CS' AND c_number = 101 AND assn = 'cs101_eval2' AND title = 'Eval2') OR " +
            //     "(c_subject = 'CS' AND c_number = 101 AND assn = 'cs101_eval3' AND title = 'Eval3')");
            stmt.executeUpdate("DELETE FROM adtg.assessment where c_subject = 'CS';");

            // Delete specific data from adtg.assn_category
            stmt.executeUpdate(
                "DELETE FROM adtg.assn_category " +
                "WHERE category IN ('Formative', 'Evaluative')");

            // Delete specific data from adtg.enrollment
            // stmt.executeUpdate(
            //     "DELETE FROM adtg.enrollment " +
            //     "WHERE (c_subject = 'CS' AND c_number = 101 AND sec_id = 1 AND student_netid IN ('xl435test', 'yz853', 'xl435'))");
            stmt.executeUpdate("DELETE FROM adtg.enrollment where c_subject = 'CS';");

            // Delete specific data from adtg.section
            // stmt.executeUpdate(
            //     "DELETE FROM adtg.section " +
            //     "WHERE c_subject = 'CS' AND c_number = 101 AND sec_id = 1 AND instructor_netid = 'inst1'");
            stmt.executeUpdate("DELETE FROM adtg.section where c_subject = 'CS';");

            // Delete specific data from adtg.users
            stmt.executeUpdate(
                "DELETE FROM adtg.users " +
                "WHERE (netid = 'yz853' AND email = 'yz853@duke.edu') OR " +
                "(netid = 'xl435' AND email = 'xl435@duke.edu') OR " +
                "(netid = 'someone' AND email = 'someone@duke.edu') OR " +
                "(netid = 'xl435test' AND email = 'Louisexyli@hotmail.com') OR " +
                "(netid = 'inst1' AND email = 'inst1@duke.edu')");

            // Delete specific data from adtg.course
            // stmt.executeUpdate(
            //     "DELETE FROM adtg.course " +
            //     "WHERE c_subject = 'CS' AND c_number = 101 AND title = 'Intro to Computer Science'");
            stmt.executeUpdate("DELETE FROM adtg.course where c_subject = 'CS';");

            System.out.println("Specific data deleted successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error deleting data: " + e.getMessage());
        }
    }

    @BeforeAll
    static void testAddScript(){
        addScript();
    }


    

    @Test
    public void testGettersSetters(){
        String groupId = "56917";
        test.setGroupId(groupId);
        assertEquals(groupId, test.getGroupId());
        String token = "testToken";
        test.setToken(token);
        assertEquals(token, test.getToken());
        String sourceToken = "testSourceToken";
        test.setSourceToken(sourceToken);
        assertEquals(sourceToken, test.getSourceToken());
        String studentRepo = "56917";
        test.setStudentRepo(studentRepo);
        assertEquals(studentRepo, test.getStudentRepo());
    }

    @Test
    public void testReadCommands(){
        String[] commands = test.readCommands("test_path");
        assertEquals(3, commands.length);
    }

    @Test
    public void testRunCommand(){
        String[] commands = {"sh", "-c", "cd ~/grader && ls && echo Hello World"};
        test.runCommand(commands);
    }


    @Test
    public void testExtractFeatures(){
        JsonArray jsonArray = new JsonArray();

        JsonObject jsonObject1 = new JsonObject();
        jsonObject1.addProperty("feature1", "id1");
        jsonArray.add(jsonObject1);

        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("feature1", "id2");
        jsonArray.add(jsonObject2);


        JsonObject jsonObject3 = new JsonObject();
        jsonObject3.addProperty("feature2", "id3");
        jsonArray.add(jsonObject3);

        JsonObject jsonObject4 = new JsonObject();
        jsonObject4.addProperty("feature2", "id4");
        jsonArray.add(jsonObject4);

        String jsonString = jsonArray.toString();
        String feature1 = "feature1";
        String feature2 = "feature2";

        //System.out.println(jsonString);
        List<String> result1 = test.extractFeature(jsonString, feature1);
        // System.out.println(json);
        List<String> result2 = test.extractFeature(jsonString, feature2);

        assertNotNull(result1);
        assertEquals(2, result1.size());
        assertEquals("id1", result1.get(0));
        assertEquals("id2", result1.get(1));

        assertNotNull(result2);
        assertEquals(2, result2.size());
        assertEquals("id3", result2.get(0));
        assertEquals("id4", result2.get(1));

    }

    @Test
    public void testGetIdByUrl(){
        try {
            assertEquals("42483", test.getIdByUrl("testing/Repo_13428", false, test.getSourceToken()));
            assertEquals("56917", test.getIdByUrl("jpastorino/adtg-test", true, test.getToken()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSubGroup(){
        List<String> sg;
        try {
            sg = test.getSubgroups(test.getGroupId());
            assertEquals(0, sg.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testGetGroupMembers(){
        List<String> gm;
        try {
            gm = test.getGroupMembers(test.getGroupId());
            assertEquals("13004,13428,13414,14216",String.join(",", gm));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testGetFeatureInProjectByName(){
        try {
            assertEquals("42536",test.getFeatureInProjectByName("testcase", "id"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetAllRepoIdInGroup(){
        // List<String> repos = test.getAllRepoFeatureInGroup(test.getGroupId(),"id");

        // System.out.println(String.join(",", repos));
    }

    @Test
    public void testIgnorePrefix(){
        // String testURL1 = "CS101/Delivery/hw1";
        // String testURL2 = "CS101/delivery/hw2/quiz2.c";
        // String testURL3 = "CS101/README.md";
        // assertEquals("CS101%2Fhw1", test.ignorePrefix(testURL1, "DELIVERY"));
        // assertEquals("CS101%2Fhw2%2Fquiz2.c", test.ignorePrefix(testURL2, "DELIVERY"));
        // assertEquals("CS101%2FREADME.md", test.ignorePrefix(testURL3, "DELIVERY"));
    }

    @Test
    public void testCreationDeletion(){
        String id;
        try {
            Course course = new Course("CS",101);
            id = test.createRepo(course, test.getGroupId(), "test");
            assertNotNull(id);
            assertEquals(id, test.getFeatureInProjectByName("f24_CS_101_test", "id"));
            test.deleteRepo(id);
            assertNull(test.getIdByUrl("jpastorino/f24_CS_101_test", false, test.getToken()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //---------- these two functions are convenitent to use when you need to create ane delete two specific repos.
    @Test
    public void testDeletion(){
        // try{
        //     String id = test.getIdByUrl("jpastorino/adtg-test/su24_CS202_xl435", false, test.getToken());
        //     test.deleteRepo(id);
        //     id = test.getIdByUrl("jpastorino/adtg-test/su24_CS202_yz853", false, test.getToken());
        //     test.deleteRepo(id);
        // }catch (Exception e) {
        //     e.printStackTrace();
        // }
    }

    @Test
    public void testCreate(){
        // Student student1 = new Student("xl435","Louise","xl435@duke.com");
        // Student student2 = new Student("yz853","Yijia","yz853@duke.edu");
        // Course course = new Course("CS",202);
        // Assessment assessment = new Assessment(course, "FIRST_STEP");
        // Delivery delivery1 = new Delivery(LocalDateTime.now(), Status.INIT, null,student1, assessment);
        // Delivery delivery2 = new Delivery(LocalDateTime.now(), Status.INIT, null,student2, assessment);
        // Assessment assessment2 = new Assessment(course, "cs202_hw1");
        // Delivery delivery3 = new Delivery(LocalDateTime.now(), Status.DELIVER, null,student1, assessment2);
        // Delivery delivery4 = new Delivery(LocalDateTime.now(), Status.DELIVER, null,student2, assessment2);
        // try {
        //     test.initDelivery(delivery1);
        //     test.initDelivery(delivery2);
        //     test.deliverDelivery(delivery3);
        //     test.deliverDelivery(delivery4);
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
    }

    @Test
    public void failureInCreationDeletion(){
        test.setGroupId("somethingbad");
        try {
            Course course = new Course("CS",101);
            assertThrows(Exception.class, ()->test.createRepo(course,test.getGroupId(), "test"));
            assertThrows(Exception.class, ()->test.deleteRepo("Not Exist Proj"));
            assertThrows(Exception.class, ()->test.deleteGroup("Not Exist Group"));
            // assertNull(test.createRepo(course,test.getGroupId(), "test"));
            // test.deleteRepo("Not Exist Proj");
            // test.deleteGroup("Not Exist Group");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInitials(){
        Course course = new Course("CS", 101);
        try{
            List<Assessment> as = test.initialAssessment(course);
            // for(Assessment a:as){
            //     System.out.println(a.getAssn());
            // }
            assertEquals(2,as.size());
            assertEquals("cs101_FIRST_STEP",as.get(0).getAssn());
            assertEquals("cs101_hw1",as.get(1).getAssn());
        }catch(Exception e){
            e.getStackTrace();
        }

    }

    @Test
    public void testFetchFileList(){
        try {
            JsonArray files = test.fetchFileList(test.getSourceToken(), "cs101_hw1", "main");
            assertEquals(5, files.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFetchFileContent(){
        try {
            String content = test.fetchFileContent("42483", "cs101_hw1/README", "main");
            assertEquals("# This is Readme", content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPushFileContent(){
        try {
            String id = test.getFeatureInProjectByName("testcase", "id");
            String filePath = LocalDateTime.now().toString()+".txt";
            test.pushFileContent(id, filePath, "master", "Current Test time:"+filePath);
            test.pushFileContent(id, filePath, "NonExistBranch", "Current Test time:"+filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTransferFile(){
        try{
            String id = test.getFeatureInProjectByName("testcase", "id");
            String sourceRepoId = test.getIdByUrl("testing/Repo_13428", false, test.getSourceToken());
            test.transferFiles(id, sourceRepoId,"cs101_hw2","master","main");
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFetchFileListLocal(){
        try {
            //Course course = new Course("CS", 202,"TITLE","/data/Assessments","NoTOKEN");
            test.setSourceRepo("/home/xl435/data/Assessments");
            List<Path> paths = test.fetchFileList("cs202_hw1");
            for(Path p:paths){
                System.out.println(p.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFetchFileContentLocal(){
        try {
            test.setSourceRepo("/home/xl435/data/Assessments");
            List<Path> paths = test.fetchFileList("cs202_hw1");
            for(Path p:paths){
                System.out.println(test.fetchFileContent(p));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNewRelativePath(){
        try {
            test.setSourceRepo("/home/xl435/data/Assessments");
            List<Path> paths = test.fetchFileList("cs202_hw2");
            for(Path p:paths){
                System.out.println(test.newRelativePath(p.toString(), "cs202_hw2", "DELIVERY"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPushFileContentLocal(){
        try {
            test.setSourceRepo("/home/xl435/data/Assessments");
            String id = test.getFeatureInProjectByName("testcase", "id");
            List<Path> paths = test.fetchFileList("cs202_hw2");
            for(Path p:paths){
                String newFilePath = test.newRelativePath(p.toString(), "cs202_hw2", "DELIVERY");
                test.pushFileContentNew(id, "master", newFilePath, test.fetchFileContent(p));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSendREADMELocal(){
        try {
            test.setSourceRepo("/home/xl435/data/Assessments");
            String id = test.getFeatureInProjectByName("testcase", "id");
            test.sendSourceREADME(id,"master");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTransferFileLocal(){
        try {
            test.setSourceRepo("/home/xl435/data/Assessments");
            String id = test.getFeatureInProjectByName("testcase", "id");
            test.transferFiles(id, "cs202_hw2", "master");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCheckStart(){
        Assessment assessment = new Assessment(new Course("ECE", 551), "ece551_hw1");
        assessment.setStartDate(LocalDateTime.of(2024, 6, 1, 0, 0));
        try {
            assertTrue(test.checkStart(assessment));
            assessment.setStartDate(LocalDateTime.of(2025, 6, 1, 0, 0));
            assertFalse(test.checkStart(assessment));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCheckPass(){
        Course course = new Course("CS", 101);
        Assessment assessment = new Assessment(course, "cs101_hw1");
        Student student = new Student("xl435test","TestAcc","Louisexyli@hotmail.com");
        Grade grade = new Grade(assessment, student, LocalDateTime.now(), new BigDecimal(75), new BigDecimal(0),new BigDecimal(75), "test");
        DAOConn conn = new DAOConn();
        GradeDAO gradeDAO = new GradeDAO(conn);
        List<Assessment> tests = new ArrayList<>();
        tests.add(assessment);
        try {
            gradeDAO.save(grade);
            assertFalse(test.checkPass(student, course, tests));
            Grade grade2 = new Grade(assessment, student, LocalDateTime.now(), new BigDecimal(80), new BigDecimal(5),new BigDecimal(75), "test");
            gradeDAO.save(grade2);
            assertTrue(test.checkPass(student, course, tests));
            gradeDAO.remove(grade);
            gradeDAO.remove(grade2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCheckPossibleAssessments(){
        Course course = new Course("CS", 101);
        Student student = new Student("xl435","Louise","xl435@duke.edu");
        try {
            test.checkPossibleAssessments(student, course);
            //System.out.println(String.join(",", test.checkPossibleAssessments(student, course)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSetUpSection(){
        RepoFuncLib test2 = new RepoFuncLib();
        Student student1 = new Student("xl435");
        Delivery delivery1 = new Delivery(LocalDateTime.now(), null, null,student1 , null);
        Student student2 = new Student("xl123");
        Delivery delivery2 = new Delivery(LocalDateTime.now(), null, null,student2 , null);
        Course course = new Course("CS", 101);
        try {
            Section sec1 = test2.setupSection(delivery1, course);
            Section sec2 = test2.setupSection(delivery2, course);
            assertNotNull(sec1);
            assertNotNull(delivery2.getLog());
            test2.setupTargetGroupAndToken(delivery1,sec1);
            assertEquals(test.getGroupId(), test2.getGroupId());
            assertEquals(test.getToken(), test2.getToken());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testInit(){
        //section not exist
        RepoFuncLib test2 = new RepoFuncLib();
        Student student1 = new Student("xl435");
        Course course = new Course("CS", 202);
        Assessment assessment = new Assessment(course, "cs202_hw1");
        try{
            Student student2 = new Student("someone");
            Delivery delivery1 = new Delivery(LocalDateTime.now(), Status.INIT, null,student2, assessment);
            test2.initDelivery(delivery1);
            assertEquals(Status.ERROR, delivery1.getStatus());
            assertEquals("Cannot find the student's section, please register the gitlab group for the section first", delivery1.getLog());
        }catch(Exception e){
            e.printStackTrace();
        }
        //student repo already exist in group
        try{
            Delivery delivery2 = new Delivery(LocalDateTime.now(), Status.INIT, null,student1, assessment);
            test2.initDelivery(delivery2);
            assertEquals(Status.COMPLETED, delivery2.getStatus());
            assertEquals("already created the repo", delivery2.getLog());
        }catch(Exception e){
            e.printStackTrace();
        }
        //create and send initial assssement & delete
        try{
            Student student3 = new Student("xl435test","TestAcc","Louisexyli@hotmail.com");
            Delivery delivery3 = new Delivery(LocalDateTime.now(), Status.INIT, null,student3, assessment);
            test2.initDelivery(delivery3);
            Section section = test.setupSection(delivery3, course);
            String id = test.getFeatureInProjectByName(section.getSemesterYear()+"_CS202_xl435test", "id");
            test2.deleteRepo(id);
            DAOConn conn = new DAOConn();
            AssnDeadlineDAO assnddlDAO = new AssnDeadlineDAO(conn);
            assnddlDAO.remove(assessment, student3);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testDelivery(){
        //section not exist
        RepoFuncLib test2 = new RepoFuncLib();
        Student student1 = new Student("xl435");
        Course course = new Course("CS", 202);
        Assessment assessment = new Assessment(course, "cs202_hw2");
        DAOConn conn = new DAOConn();
        AssnDeadlineDAO assnddlDAO = new AssnDeadlineDAO(conn);
        GradeDAO gradeDAO = new GradeDAO(conn);
        DeliveryDAO deliverDAO = new DeliveryDAO(conn);
        AssessmentDAO assessmentDAO = new AssessmentDAO(conn);
        try{
            Student student2 = new Student("someone");
            Delivery delivery1 = new Delivery(LocalDateTime.now(), Status.DELIVER, null,student2, assessment);
            test2.deliverDelivery(delivery1);
            assertEquals(Status.ERROR, delivery1.getStatus());
            assertEquals("Cannot find the student's section, please register the gitlab group for the section first", delivery1.getLog());
        }catch(Exception e){
            e.printStackTrace();
        }
        //student repo does not exist in group
        try{
            Student student3 = new Student("xl435test");
            Delivery delivery2 = new Delivery(LocalDateTime.now(), Status.DELIVER, null,student3, assessment);
            test2.deliverDelivery(delivery2);
            assertEquals(Status.ERROR, delivery2.getStatus());
            System.out.println(delivery2.getLog());
            assertEquals("Cannot find the repository for the student, please check all the information", delivery2.getLog());
        }catch(Exception e){
            e.printStackTrace();
        }
        //check whether meet all req
        // try{
        //     Assessment assessment2 = new Assessment(course, "cs101_eval3");
        //     Delivery delivery3 = new Delivery(LocalDateTime.now(), Status.DELIVER, null,student1, assessment2);
        //     test2.deliverDelivery(delivery3);
        //     assertEquals(Status.ERROR, delivery3.getStatus());
        //     System.out.println(delivery3.getLog());
        //     assertEquals("The student has not passed all the required prerequisites", delivery3.getLog());
        // }catch(Exception e){
        //     e.printStackTrace();
        // }
        //check whether it already delivered
        try{
            Assessment assessment2 = new Assessment(course, "cs202_hw1");
            assessmentDAO.loadAssessmentByPK(assessment2);
            Delivery delivery4 = new Delivery(LocalDateTime.now(), Status.DELIVER, null,student1, assessment2);
            deliverDAO.deliverAssn(delivery4);
            test2.deliverDelivery(delivery4);
            assertEquals(Status.COMPLETED, delivery4.getStatus());
            assertEquals("already sent the assessment", delivery4.getLog());
        }catch(Exception e){
            e.printStackTrace();
        }
        //send assessment to testcases
        try{
            Student student3 = new Student("xl435test","TestAcc","Louisexyli@hotmail.com");
            Assessment first_step = new Assessment(course, "FIRST_STEP");
            Delivery delivery5 = new Delivery(LocalDateTime.now(), Status.INIT, null,student3, first_step);
            test2.initDelivery(delivery5);
            Section section = test2.setupSection(delivery5, course);
            String id = test.getFeatureInProjectByName(section.getSemesterYear()+"_CS202_xl435test", "id");
            //String id = test.getFeatureInProjectByName("f24_CS_101_xl435test", "id");
            Assessment assessment2 = new Assessment(course, "cs202_hw1");
            Grade grade = new Grade(assessment2, student3, LocalDateTime.now(), 80,10,70, "---");
            gradeDAO.save(grade);
            Delivery delivery6 = new Delivery(LocalDateTime.now(), Status.DELIVER, null,student3, assessment);
            test2.deliverDelivery(delivery6);
            assertEquals(Status.COMPLETED, delivery6.getStatus());
            assertEquals("Sent the assessment to Student", delivery6.getLog());
            Assessment assessment3 = new Assessment(course, "cs202_eval1");
            Delivery delivery7 = new Delivery(LocalDateTime.now(), Status.DELIVER, null,student1, assessment3);
            test2.deliverDelivery(delivery7);
            assertEquals(Status.COMPLETED, delivery7.getStatus());
            assertEquals("Sent the assessment to Student", delivery7.getLog());
            test2.deleteRepo(id);
            assnddlDAO.remove(assessment, student3);
            assnddlDAO.remove(assessment2, student3);
            assnddlDAO.remove(assessment3, student3);
            gradeDAO.remove(grade);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @AfterAll
    static void testDelete(){
        deleteScript();
    }

}
