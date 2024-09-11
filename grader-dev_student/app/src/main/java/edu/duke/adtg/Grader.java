package edu.duke.adtg;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.duke.adtg.domain.*;

import java.time.LocalDateTime;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
// import org.json.JSONObject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
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
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.StandardCopyOption;
public class Grader {
    private static String gitlabToken;
    private static String netId;
    private static String c_subject;
    private static int c_number;
    private static Assessment assn;
    private static String REPO_URL;
    private static final String FILE_PATH_IN_REPO = ""; // Update with your file's path
    private static final String CLONE_DIRECTORY_PATH = "cloned-repo";
    private static LocalDateTime timeStarted;
    DAOConn conn = new DAOConn();
    CourseDAO courseDAO = new CourseDAO(conn);
    PrerequisiteDAO pqDAO = new PrerequisiteDAO(conn);
    GradeDAO gradeDAO = new GradeDAO(conn);
    AssessmentDAO assessmentDAO = new AssessmentDAO(conn);
    DeliveryDAO deliverDAO = new DeliveryDAO(conn);

    public Grader(String testcase, String student) {
        loadConfig();

        REPO_URL = "https://gitlab.oit.duke.edu/" + testcase;
        netId = extractNetId(student);
        c_subject = "CS";
        c_number = 101;
        timeStarted = LocalDateTime.now();
        assn = getNextAssessment(c_subject, c_number, netId);
   
    }
    private static void loadConfig() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("config.properties"));
            gitlabToken = properties.getProperty("gitlab.token");

        } catch (IOException e) {
            e.printStackTrace();    
        }
    }
    

    public void mainFunction() {
        
        // try {
            // Step 1: Clone or pull the repository
            if (assn == null) {
                System.out.println("No assessment found for " + c_subject + " " + c_number + " for student " + netId);
                return;
            }
            File repoDirectory = new File(CLONE_DIRECTORY_PATH);
            System.out.println("Cloning repository to " + new File(repoDirectory, FILE_PATH_IN_REPO).getAbsolutePath());
            try {
                deleteDirectoryRecursively(repoDirectory);  // Delete the cloned repository if it exists        
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            try {
                cloneRepository(REPO_URL, gitlabToken, repoDirectory); // Clone the repository
            } 
            catch (Exception e) {
                e.printStackTrace();
                return;
            }
            String fileList =  CLONE_DIRECTORY_PATH + "/" + assn.getAssn() +"/requiredFiles.txt";
            fetchFilesFromList(fileList, "main");

            System.out.println("Step 2");
            // Step 2: Compare files
            File pulledFile = new File(repoDirectory, FILE_PATH_IN_REPO);
            // File localFile = new File(LOCAL_FILE_PATH);

            ScriptExecutor scriptExecutor = new ScriptExecutor(CLONE_DIRECTORY_PATH + "/" + assn.getAssn() + "/"+ "script.sh");
            File outputFile = scriptExecutor.executeFile();
            String log = printFile(outputFile);
            Boolean filesMatch = false;
            try {
                filesMatch = compareFiles(outputFile, new File(CLONE_DIRECTORY_PATH + "/" + assn.getAssn() + "/"+ "expectedOutput.txt"));
            
                System.out.println("Files match: " + filesMatch);
                // Step 3: Generate a report
                // generateReport(filesMatch);
                BigDecimal rawGrade = filesMatch ? BigDecimal.valueOf(100.0) : BigDecimal.valueOf(0.0);
                BigDecimal finalGrade = countFinalGrade(rawGrade, assn, timeStarted);
                Grade grade = updateGrade(c_subject, c_number, assn, netId, rawGrade, BigDecimal.valueOf(0.0), finalGrade, log);
                deliverAssessment(grade);
            }
            catch (Exception e) {
                e.printStackTrace();
                
            }
            // Step 4: Delete the pulled file and repository
            try {
                deleteFile(pulledFile);
                deleteDirectoryRecursively(repoDirectory);
            } catch (IOException e) {
                e.printStackTrace();
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
    public BigDecimal countFinalGrade(BigDecimal grade, Assessment assessment, LocalDateTime time) throws IllegalArgumentException{
        // if (assessment.getDueDate().isBefore(time)  ){
        //     throw new IllegalArgumentException("The assessment is over due");
        // }
        // if (grade.compareTo(assessment.getPassScore()) < 0){
        //     throw new IllegalArgumentException("The grade is lower than the pass score");
        // }
        // Todo: Set the penalty if needed
        return grade;
        
    }
    /**
     * Retrieves the next assessment for a given subject, number, and netid.
     *
     * @param c_subject the subject of the assessment
     * @param c_number the number of the assessment
     * @param netid the netid of the student
     * @return the next assessment, or null if an exception occurs
     */
    public Assessment getNextAssessment(String c_subject, int c_number, String netid) {
        try {
            return assessmentDAO.getNextAssessment(c_subject, c_number, netid);
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
    public Grade updateGrade(String c_subject, int c_number, Assessment assn, String netid, BigDecimal assn_grade, BigDecimal penalty, BigDecimal final_grade, String log_text) {
        Grade grade = null;
        try {
            Assessment assessment = assessmentDAO.getAssessment(c_subject, c_number, assn.getAssn());
            Student student = new Student(netid);

            grade = new Grade(assessment, student, timeStarted, assn_grade, penalty, final_grade, log_text);
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
     * Delivers the assessment to the student and updates the grading status.
     * If the student passes the assessment, the next assessment will be delivered.
     *
     * @param grade The grade object containing the assessment and student information.
     */
    public void deliverAssessment(Grade grade) {
        // check if student passes the assessment
        if (grade.getFinalGrade().compareTo(grade.getAssessment().getPassScore()) >= 0) {
            // get the next assessment to deliver
            Assessment assessmentToDeliver = getAssessmentsToDeliver(grade.getAssessment().getCourse(), grade.getAssessment());
            try {
                if (assessmentToDeliver != null) {
                    System.out.println("Student passed the assessment. Deliver " + assessmentToDeliver.getAssn() + " to the student.");
                    // Set the grading status of the current assessment to N 
                    setAssnGradingStatus(grade.getAssessment(), grade.getStudent().getNetId(), "N");
                    Delivery delivery = new Delivery(LocalDateTime.now(), Status.INIT, "Assessment delivered.", grade.getStudent(), assessmentToDeliver);
                    deliverDAO.save(delivery);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Student did not pass the assessment.");
        }
    }
    /**
     * Sets the grading status of an assessment for a specific student.
     *
     * @param assn   the assessment object
     * @param netid  the student's network ID
     * @param status the grading status to be set
     */
    public void setAssnGradingStatus(Assessment assn, String netid, String status) {
        try {
            assessmentDAO.setAssnGradingStatus(assn, netid, status);
            System.out.println("Assessment grading status set to N.");
        } catch (Exception e) {
            e.printStackTrace();
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
                if (assessmentDAO.checkPrerequisiteExists(a, assn)) {
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
    public static void fetchFilesFromList(String filesListPath, String branch) {

        String currentDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + currentDir);
        try (BufferedReader br = new BufferedReader(new FileReader(filesListPath))) {
            String filePath;
            while ((filePath = br.readLine()) != null) {
                // fetchAndSaveFile(projectId, filePath, branch);
                copyAndSaveFile(filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Copies a file from the given source path to the destination path and saves it.
     *
     * @param filePath the path of the file to be copied and saved
     */
    public static void copyAndSaveFile(String filePath) {
        Path sourcePath = Paths.get("..", filePath);
        Path destinationPath = Paths.get(CLONE_DIRECTORY_PATH, filePath);

        try {
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File copied successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Fetch the content of a file from GitLab.
     *
     * @param apiUrl The API URL to fetch the file.
     * @return The content of the file as a string, or null if an error occurs.
     */
    public static String fetchFileContent(String apiUrl) {
        System.out.println("Fetching file content from: " + apiUrl);
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("PRIVATE-TOKEN", gitlabToken);

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
     * Clones a Git repository from the specified URL into the given directory.
     *
     * @param repoUrl   the URL of the repository to clone
     * @param token     the access token or password for authentication
     * @param directory the directory where the repository will be cloned into
     * @return the cloned Git object
     * @throws GitAPIException if an error occurs during the cloning process
     */
    private static Git cloneRepository(String repoUrl, String token, File directory) throws GitAPIException {
        return Git.cloneRepository()
                .setURI(repoUrl)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", token))
                .setDirectory(directory)
                .call();
    }
    /**
     * Reads the content of a file and prints each line to the console.
     * 
     * @param file the file to read
     * @return the content of the file as a string
     */
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
     * Compares two files to check if their contents are equal.
     *
     * @param file1 the first file to compare
     * @param file2 the second file to compare
     * @return true if the files have the same content, false otherwise
     * @throws IOException if an I/O error occurs while reading the files
     */
    private static boolean compareFiles(File file1, File file2) throws IOException {
        System.out.println("Checking existence of pulled file: " + file1.getAbsolutePath());
        System.out.println("Checking existence of local file: " + file2.getAbsolutePath());

        if (!file1.exists()) {
            System.out.println("Pulled file does not exist: " + file1.getAbsolutePath());
            return false;
        }
        if (!file2.exists()) {
            System.out.println("Local file does not exist: " + file2.getAbsolutePath());
            return false;
        }

        try (Scanner sc1 = new Scanner(file1, StandardCharsets.UTF_8.name());
             Scanner sc2 = new Scanner(file2, StandardCharsets.UTF_8.name())) {

            while (sc1.hasNextLine() && sc2.hasNextLine()) {
                if (!sc1.nextLine().equals(sc2.nextLine())) {
                    return false;
                }
            }

            return !sc1.hasNextLine() && !sc2.hasNextLine();
        }
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

}
