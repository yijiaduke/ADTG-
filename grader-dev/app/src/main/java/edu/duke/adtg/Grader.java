package edu.duke.adtg;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.duke.adtg.domain.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.checkerframework.checker.units.qual.C;
import org.checkerframework.checker.units.qual.s;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.apache.commons.io.FileUtils;


import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public class Grader {
    // private static String gitlabToken;
    private static String gitlabTokenStudent;
    private GradeRequest gradeRequest;
    private static String netId;
    private static Assessment assn;
    private static String REPO_URL;
    private static String projectId; // Replace with your GitLab project ID
    private static final String FILE_PATH_IN_REPO = ""; // Update with your file's path
    private static String OWNER;
    private static LocalDateTime timeStarted;
    private static String inputPath;
    private static String test_cmd; 
    private static final String TEST_FOLDER = "/TESTS/";
    private static final String GITLAB_API_URL = "https://gitlab.oit.duke.edu/api/v4";
    private static String log = "Grading in progress";
    private DAOConn conn = new DAOConn();
    private CourseDAO courseDAO = new CourseDAO(conn);
    private PrerequisiteDAO pqDAO = new PrerequisiteDAO(conn);
    private GradeDAO gradeDAO = new GradeDAO(conn);
    private AssessmentDAO assessmentDAO = new AssessmentDAO(conn);
    private DeliveryDAO deliverDAO = new DeliveryDAO(conn);
    private GradeRequestDAO gradeRequestDAO = new GradeRequestDAO(conn);
    private SectionDAO sectionDAO = new SectionDAO(conn);

    public Grader(GradeRequest gradeRequest, DAOConn conn, String owner) {
        // loadConfig();
        this.OWNER = owner;
        // Step 1: Get the next assessment to grade
        this.gradeRequest = gradeRequest;
        // this.conn = conn;

        netId = gradeRequest.getStudent().getNetId();
        
        timeStarted = gradeRequest.getRequestTime();
        assn = gradeRequest.getAssessment();
        
        Course course = new Course(assn.getSubject(), assn.getNumber());
        System.out.println("Assessment: " + assn.toString());
        courseDAO.load(course);
        REPO_URL = course.getRepo();
        // gitlabToken = course.getRepoToken();
        inputPath = OWNER + "/" + assn.getAssn() + TEST_FOLDER;

        // Get student's section
        try {
            //load assessment
            assessmentDAO.loadAssessmentByPK(assn);
            test_cmd = assn.getTestCmd();
            //load section
            Section section = sectionDAO.getSectionByNetIdAndCourse(netId, course);
            gitlabTokenStudent = section.getGitlab_token();
            String semesterYear = section.getSemesterYear();
            projectId = section.getGitlab_group() + "/" + semesterYear+ "_" + assn.getSubject() + assn.getNumber() +"_" + netId;
            System.out.println("Project ID: " + projectId);
        } catch (Exception e) {
            log = e.getMessage();
            e.printStackTrace();
        }
        System.out.println("Assessment " + assn.getAssn());
    }
    

    public void mainFunction() {
        if (assn == null) {
            System.out.println("No grading request found");
            return;
        }
        // gradeRequestDAO.updateGradeRequest(gradeRequest, "IN PROGRESS", OWNER, log);
        String commitId = null;
        try {
            commitId = getLatestCommitId(projectId, gitlabTokenStudent);
        } catch (Exception e) {
            printAndStoreStackTrace(e);
        }
        updateCommitId(commitId, gradeRequest);

        // Step 1: Clone the repository
        File repoDirectory = new File(OWNER);
        System.out.println("Cloning repository to " + new File(repoDirectory, FILE_PATH_IN_REPO).getAbsolutePath());
        try {
            deleteDirectoryRecursively(repoDirectory);  // Delete the cloned repository if it exists        
        }
        catch (IOException e) {
            System.out.println("Error deleting the cloned repository");
            e.printStackTrace();
            log = e.getMessage() + log;
        }
        try {
            copyTestFolder("/graderApp/volume/" + REPO_URL, repoDirectory);
            // cloneRepository(REPO_URL, gitlabToken, repoDirectory); // Clone the repository
        } 
        catch (Exception exception) {
            printAndStoreStackTrace(exception);
            return;
        }
        String testFolder = assn.getAssn() + TEST_FOLDER;
        String fileList =  OWNER + "/" + testFolder + "requiredFiles.txt";
        try {
            fetchFilesFromList(projectId, assn.getAssn(), fileList, "main", gitlabTokenStudent);
        } catch (Exception exception2) {
            printAndStoreStackTrace(exception2);
            return;
        }
        File outputFile = null;
        File pulledFile = null;
        try {
            System.out.println("Step 2: Execute the test command");
            pulledFile = new File(repoDirectory, FILE_PATH_IN_REPO);

            ScriptExecutor scriptExecutor = new ScriptExecutor(inputPath, test_cmd);
            outputFile = scriptExecutor.executeFile();
            
            String log = printFile(outputFile);
        
    
            // Step 3: Record the grade
            System.out.println("Step 3: Record the grade");
            BigDecimal rawGrade = extractGrade(outputFile);
            BigDecimal finalGrade = countFinalGrade(rawGrade, assn, timeStarted);
            System.out.println("Raw grade: " + rawGrade);
            Grade grade = updateGrade(assn, netId, rawGrade, BigDecimal.valueOf(0.0), finalGrade, log);
            deliverAssessment(grade);
            gradeRequestDAO.updateGradeRequest(gradeRequest, "DONE", OWNER, "Grading completed");
        }
        catch (Exception exception3) {
            printAndStoreStackTrace(exception3);  
        }
        // Step 4: Delete the pulled file and repository
        try {
            deleteFile(outputFile);   
            deleteFile(pulledFile);
            deleteDirectoryRecursively(repoDirectory);
        } catch (IOException e) {
            printAndStoreStackTrace(e);
        }
    }
    /**
     * Extracts the netId from a given path.
     *
     * @param path The path from which to extract the netId.
     * @return The extracted netId, or null if no netId is found.
     */
    public static String extractNetId(String path) {
        // Regular expression to match the netId at the end of the path
        Pattern pattern = Pattern.compile("_(\\w+)$");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
    
    /**
     * Calculates the final grade based on the given grade, assessment, and time.
     * 
     * @param grade the grade obtained by the student
     * @param assessment the assessment for which the grade is being calculated
     * @param time the current time
     * @return the final grade
     * @throws IllegalArgumentException if the assessment is overdue or the grade is lower than the pass score
     */
    public BigDecimal countFinalGrade(BigDecimal grade, Assessment assessment, LocalDateTime time) throws IllegalArgumentException, SQLException{
        LocalDateTime dueDate = new AssnDeadlineDAO(conn).getAssnDeadlineByPK(assessment, netId).getDue();
        // System.out.println(assessment.toString());
        if (dueDate.isBefore(time)  ){
            return grade.subtract(calculatePenalty(dueDate, time, grade, assessment.getPenaltyFormula()));
        
        }
        return grade;
        
    }
    /**
     * Calculates the penalty for a late submission based on the given parameters.
     *
     * @param dueDateTime      the due date and time of the assignment
     * @param submissionDateTime  the submission date and time of the assignment taken from grade_request
     * @param grade            the original grade of the assignment
     * @param formula          the formula used to calculate the penalty
     * @return the penalty for the late submission
     * @throws IllegalArgumentException if the formula is invalid or does not evaluate to a number
     */
    public BigDecimal calculatePenalty(LocalDateTime dueDateTime, LocalDateTime submissionDateTime, 
                                              BigDecimal grade, String formula) {
        long hoursLate = ChronoUnit.HOURS.between(dueDateTime, submissionDateTime);
        
        // If submission is not late, return the full grade
        if (hoursLate <= 0) {
            return grade;
        }
        
        // Replace placeholders in the formula and remove $ symbols
        String processedFormula = formula.replace("$", "")
                                         .replace("@grade", grade.toPlainString())
                                         .replace("@hour", String.valueOf(hoursLate))
                                         .replace("e^(", "Math.exp(");;
        
        try (Context context = Context.newBuilder("js")
            .option("engine.WarnInterpreterOnly", "false")
            .build()) {
            // System.out.println("Processed formula: " + processedFormula);
            Value result = context.eval("js", processedFormula);
            return BigDecimal.valueOf(result.asDouble()).max(BigDecimal.ZERO);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid formula: " + formula, e);
        }
        
}
    
    /**
     * Retrieves the next pending grade request from the grade request DAO.
     * 
     * @return The next pending grade request, or null if there are no pending requests.
     */
    public GradeRequest getNextGradeRequest() {
        try {
            GradeRequest grade_request = gradeRequestDAO.listGradeRequestByStatus("PENDING").get(0);
            Assessment assessment = grade_request.getAssessment();
            assessmentDAO.load(assessment);
            grade_request.setAssessment(assessment);
            return grade_request;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Updates the grade for a specific assessment of a student.
     *
     * @param c_subject   the subject of the assessment
     * @param c_number    the number of the assessment
     * @param assn        the assessment object
     * @param netid       the student's network ID
     * @param assn_grade  the grade for the assessment
     * @param penalty     the penalty for the assessment
     * @param final_grade the final grade for the assessment
     * @param log_text    the log text for the assessment
     */
    public Grade updateGrade(Assessment assn, String netid, BigDecimal assn_grade, BigDecimal penalty, BigDecimal final_grade, String log_text) {
        Grade grade = null;
        try {
            Assessment assessment = assessmentDAO.getAssessment(assn.getSubject(), assn.getNumber(), assn.getAssn());
            Student student = new Student(netid);

            grade = new Grade(assessment, student, LocalDateTime.now(), assn_grade, penalty, final_grade, log_text);
            gradeDAO.save(grade);
            if (final_grade.compareTo(assessment.getPassScore()) >= 0) {
                gradeDAO.updateFinalGrade(grade);
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return grade;

    }
    /**
     * Prints the stack trace of the given exception and stores it along with additional log information.
     *
     * @param e the exception for which the stack trace needs to be printed and stored
     */
    void printAndStoreStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        gradeRequestDAO.updateGradeRequest(gradeRequest, "DONE", OWNER, stackTrace + log);
    }
    /**
     * Delivers the assessment to the student and updates the grading status.
     * If the student passes the assessment, the next assessment will be delivered.
     *
     * @param grade The grade object containing the assessment and student information.
     */
    public void deliverAssessment(Grade grade) {
        // check if student passes the assessment
        if (grade.getFinalGrade().compareTo(grade.getAssessment().getPassScore()) >= 0) {
            // get the next assessment to deliver
            try {
                deliverDAO.deliverNextAssessment(grade.getAssessment(), grade.getStudent());
            } catch (Exception e) {
                printAndStoreStackTrace(e);
            }
        }
        else {
            System.out.println("Student did not pass the assessment. Grade: " + grade.getFinalGrade());
        }
    }
   
    /**
     * Retrieves the assessments to deliver for a given course and assessment.
     *
     * @param course The course for which to retrieve the assessments.
     * @param assn The assessment for which to retrieve the assessments.
     * @return The assessments to deliver, or null if none are found.
     */
    public Assessment getAssessmentsToDeliver(Course course, Assessment assn) {
        Assessment assessmentsToDeliver = null;
        try {
            List<Assessment> all = assessmentDAO.listAssessmentsForCourse(course);
            for (Assessment a : all) {
                if (pqDAO.checkPrerequisiteExists(a, assn)) {
                    assessmentsToDeliver = a;
                    break;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return assessmentsToDeliver;
    }
  
        /**
     * Fetch files from the list provided in a text file.
     *
     * @param filesListPath Path to the text file containing file paths.
     */
    /**
     * Fetches and saves files from a list of file paths.
     *
     * @param projectId           the ID of the project
     * @param inputPath           the input path where the files will be saved
     * @param filesListPath       the path to the file containing the list of file paths
     * @param branch              the branch to fetch the files from
     * @param gitlabTokenStudent  the GitLab token for authentication
     */
    public static void fetchFilesFromList(String projectId, String inputPath, String filesListPath, String branch, String gitlabTokenStudent) {

        // String currentDir = System.getProperty("user.dir");
        // System.out.println("Current working directory: " + currentDir);
        try (BufferedReader br = new BufferedReader(new FileReader(filesListPath))) {
            String filePath;
            while ((filePath = br.readLine()) != null) {
                fetchAndSaveFile(projectId, inputPath,  filePath, branch, gitlabTokenStudent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches a file from a GitLab repository and saves it locally.
     *
     * @param projectId          The ID of the GitLab project.
     * @param inputPath          The input path of the file in the repository.
     * @param filePath           The file path within the input path.
     * @param branch             The branch of the repository.
     * @param gitlabTokenStudent The GitLab token for authentication.
     */
    public static void fetchAndSaveFile(String projectId, String inputPath, String filePath, String branch, String gitlabTokenStudent) {
        try {
            String encodedPath = urlEncodePath(inputPath + "/" +filePath);
            // System.out.println("Encoded path: " + encodedPath);
            String apiUrl = constructApiUrl(encodedPath, branch, "https://gitlab.oit.duke.edu/api/v4/projects/" + projectId + "/repository/files");
            String fileContent = fetchFileContent(apiUrl, gitlabTokenStudent);

            if (fileContent != null) {
                saveFileContent(OWNER + "/"+ inputPath+TEST_FOLDER +filePath, fileContent);
                System.out.println("Fetched: " + inputPath + TEST_FOLDER + filePath);
            } else {
                System.out.println("Error fetching " + inputPath + TEST_FOLDER + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * URL-encode the file path.
     *
     * @param path The file path to encode.
     * @return The URL-encoded file path.
     * @throws Exception If an encoding error occurs.
     */
    public static String urlEncodePath(String path) throws Exception {
        return URLEncoder.encode(path, StandardCharsets.UTF_8.toString());
    }

    /**
     * Construct the GitLab API URL for fetching a file.
     *
     * @param encodedPath The URL-encoded file path.
     * @return The constructed API URL.
     */
    public static String constructApiUrl(String encodedPath, String branch,  String BASE_URL) {
        return GITLAB_API_URL + "/projects/" + projectId.replace("/", "%2F") + "/repository/files/" + encodedPath.replace("/", "%2F") + "/raw?ref=" + branch;
    }

    /**
     * Fetch the content of a file from GitLab.
     *
     * @param apiUrl The API URL to fetch the file.
     * @return The content of the file as a string, or null if an error occurs.
     */
    public static String fetchFileContent(String apiUrl, String gitLabTokenStudent) {
        System.out.println("Fetching file content from: " + apiUrl);
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("PRIVATE-TOKEN", gitlabTokenStudent);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine).append("\n");
                    }
                }
            } else {
                System.out.println("HTTP response code: " + responseCode);
                return null;
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return content.toString();
    }

    /**
     * Save the file content to the local file system.
     *
     * @param filePath The file path to save the content.
     * @param content  The content to save.
     * @throws Exception If an I/O error occurs.
     */
    public static void saveFileContent(String filePath, String content) throws Exception {
        Files.createDirectories(Paths.get(filePath).getParent()); // Create directories if they don't exist
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }

    /**
     * Retrieves the latest commit ID from the specified API URL using the provided private token.
     *
     * @param projectId     The ID of the project.
     * @param PRIVATE_TOKEN The private token used for authentication.
     * @return The ID of the latest commit, or null if no commits are found or an error occurs.
     * @throws IOException If an I/O error occurs while making the HTTP request.
     */
    public static String getLatestCommitId(String projectId, String PRIVATE_TOKEN) throws IOException {
        String apiUrl = GITLAB_API_URL + "/projects/" + projectId.replace("/", "%2F") + "/repository/commits";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(apiUrl);
        request.addHeader("PRIVATE-TOKEN", PRIVATE_TOKEN);

        HttpResponse response = httpClient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 200) {
            String responseBody = EntityUtils.toString(response.getEntity());
            JsonArray commits = JsonParser.parseString(responseBody).getAsJsonArray();
            if (commits.size() > 0) {
                JsonElement latestCommit = commits.get(0);
                return latestCommit.getAsJsonObject().get("id").getAsString();
            }
        } else {
            System.err.println("Failed to fetch commits: HTTP " + statusCode);
        }

        return null;
    }

    /**
     * Updates the commit ID of a GradeRequest object.
     * 
     * @param commitId The new commit ID to be set.
     * @param grade_Request The GradeRequest object to be updated.
     */
    public void updateCommitId(String commitId, GradeRequest grade_Request) {
        try {
            grade_Request.setCommitID(commitId);
            gradeRequestDAO.updateGradeRequest(grade_Request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void copyTestFolder(String sourceDir, File DestinationFile) {
        File source = new File(sourceDir);
        
        // File dest = new File(DestinationDir);
       try {
            FileUtils.copyDirectory(source, DestinationFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Clones a Git repository from the specified URL into the given directory.
     *
     * @param repoUrl   the URL of the repository to clone
     * @param token     the access token for authentication
     * @param directory the directory where the repository will be cloned into
     * @return the cloned Git object
     * @throws GitAPIException if an error occurs during the cloning process
     */
    private static Git cloneRepository(String repoUrl, String token, File directory) throws GitAPIException {
        System.out.println("Cloning repository: " + repoUrl);
        return Git.cloneRepository()
                .setURI(repoUrl)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", token))
                .setDirectory(directory)
                .call();
    }
    private static String printFile(File file) {
        StringBuilder content = new StringBuilder();
        try (Scanner sc = new Scanner(file, StandardCharsets.UTF_8.name())) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                System.out.println(line);
                content.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    /**
     * Deletes the specified file if it exists and is a regular file.
     *
     * @param file the file to be deleted
     */
    private static void deleteFile(File file) {
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    /**
     * Recursively deletes a directory and all its contents.
     *
     * @param directory the directory to be deleted
     * @throws IOException if an I/O error occurs
     */
    private static void deleteDirectoryRecursively(File directory) throws IOException {
        if (directory.exists()) {
            Files.walk(directory.toPath())
                .map(Path::toFile)
                .sorted((o1, o2) -> -o1.compareTo(o2)) // Delete files before directories
                .forEach(File::delete);
        }
    }
    /**
     * Extracts the assessment grade from a given file.
     *
     * @param file the file from which to extract the grade
     * @return the final assessment grade as a BigDecimal, or null if the grade is not found
     * @throws IOException if an I/O error occurs while reading the file
     */
    public static BigDecimal extractGrade(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            Pattern pattern = Pattern.compile("FINAL ASSESSMENT GRADE: ([0-9]+\\.[0-9]+)");
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String gradeStr = matcher.group(1);
                    return new BigDecimal(gradeStr);
                }
            }
        }
        return null; // Grade not found
    }
}
