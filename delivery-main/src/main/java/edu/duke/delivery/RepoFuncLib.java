package edu.duke.delivery;


import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.duke.adtg.domain.Assessment;
import edu.duke.adtg.domain.AssessmentDAO;
import edu.duke.adtg.domain.Course;
import edu.duke.adtg.domain.CourseDAO;
import edu.duke.adtg.domain.DAOConn;
import edu.duke.adtg.domain.Delivery;
import edu.duke.adtg.domain.DeliveryDAO;
import edu.duke.adtg.domain.EnrollmentDAO;
import edu.duke.adtg.domain.Grade;
import edu.duke.adtg.domain.GradeDAO;
import edu.duke.adtg.domain.PrerequisiteDAO;
import edu.duke.adtg.domain.Section;
import edu.duke.adtg.domain.Status;
import edu.duke.adtg.domain.Student;
import edu.duke.adtg.domain.User;

// import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.Arrays;
// import org.springframework.core.io.ResourceLoader;

public class RepoFuncLib {
    private String groupPath; //the path(title) for this section page in gitlab
    private String groupId; //the groupId for this section page in gitlab
    private String token; //the token for the group access level: owner
    private String studentRepo;//it will be set later for the student's project Id
    private String sourceToken;//it will be set later for the source's token
    private String sourceRepo; // it will bet set later by course.getRepo

    DAOConn conn = new DAOConn();
    CourseDAO courseDAO = new CourseDAO(conn);
    PrerequisiteDAO pqDAO = new PrerequisiteDAO(conn);
    GradeDAO gradeDAO = new GradeDAO(conn);
    AssessmentDAO assessmentDAO = new AssessmentDAO(conn);
    DeliveryDAO deliverDAO = new DeliveryDAO(conn);
    EnrollmentDAO enrollmentDAO = new EnrollmentDAO(conn);


    public RepoFuncLib() {
    }

    /**
     * @param token
     */
    public void setToken(String token){
        this.token = token;
    }

    /**
     * @param token
     */
    public void setSourceToken(String token){
        this.sourceToken = token;
    }

    /**
     * @param groupId
     */
    public void setGroupId(String groupId){
        this.groupId = groupId;
    }

    /**
     * @param studentRepo
     */
    public void setStudentRepo(String studentRepo) {
        this.studentRepo = studentRepo;
    }

    /**
     * @param sourceRepo
     */
    public void setSourceRepo(String sourceRepo) {
        this.sourceRepo = sourceRepo;
    }

    /**
     * @return
     */
    public String getGroupPath() {
        return groupPath;
    }

    /**
     * @param groupPath
     */
    public void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }

    /**
     * @return the section's Id
     */
    public String getGroupId(){
        return this.groupId;
    }

    /**
     * @return the assessment's source project token, access_level = 50
     */
    public String getSourceToken() {
        return sourceToken;
    }

    /**
     * @return the student's repository's Id
     */
    public String getStudentRepo() {
        return studentRepo;
    }

    /**
     * @return the token of the section's group page, access_level = 50
     */
    public String getToken(){
        return this.token;
    }

    public DAOConn getConnection(){
        return this.conn;
    }

    private String readResourceFile(String fileName) {
        StringBuilder content = new StringBuilder();
        ClassLoader classLoader = getClass().getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString();
    }

    public String[] readCommands(String path){
        String content = readResourceFile("commands.txt");
        String[] commands = content.replace("%path",path).split("\n");
        return commands;
    }

    public void runCommand(String[] command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Exited with code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * exract 'feature' from 'jsonObject'
     * @param jsonObject
     * @param feature
     * @param ids the list of objects from jsonObject, only containing the specific feature
     */
    private void extractFeatureFromJsonObject(JsonObject jsonObject, String feature, List<String> ids) {
        JsonElement featureElement = jsonObject.get(feature);
        if (featureElement != null && featureElement.isJsonPrimitive()) {
            ids.add(featureElement.getAsString());
        }
    }

    /**
     * extract the specific 'feature' of each object from the 'body'
     * @param body body of a json string
     * @param feature specific feature to get extracted
     * @return the specific feature among all objects in body
     */
    public List<String> extractFeature(String body, String feature) {
        List<String> ids = new ArrayList<>();
        JsonElement jsonElement = JsonParser.parseString(body);

        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();
                extractFeatureFromJsonObject(jsonObject, feature, ids);
            }
        } else if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            extractFeatureFromJsonObject(jsonObject, feature, ids);
        }

        return ids;
    }

    /**
     * parsing get request with 'url' and 'specToken'
     * @param url url to do the get request
     * @param specToken token to the request folder/group/... 
     * @return a HttpResponse<String> response body
     * @throws Exception 
     */
    private HttpResponse<String> parsingGetRequest(String url, String specToken) throws Exception{
        if (token == null) {
            System.err.println("Failed to read token");
            return null;
        }
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Private-Token", specToken)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if(response!=null && response.statusCode()==200){
            return response;
        }
        else{
            if(response!=null){
                System.out.println(response.body());
                System.out.println("HTTP Request failed, see error above.");
                throw new Exception(response.body());
            }
            else{
                throw new Exception("Connection error of http requests may happen");
            }
        }
        // try{
        //     HttpClient client = HttpClient.newHttpClient();

        //     HttpRequest request = HttpRequest.newBuilder()
        //             .uri(new URI(url))
        //             .header("Private-Token", specToken)
        //             .GET()
        //             .build();

        //     HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        //     return response;
        // }catch(Exception e){
        //     System.err.println(e.getMessage());
        //     e.printStackTrace();
        //     return null;
        // }
    }

    /**
     * parsing post request with 'url' and 'specToken'
     * @param url url to do the post request
     * @param specToken token to the request folder/group/... 
     * @return a HttpResponse<String> response body
     * @throws Exception 
     */
    private HttpResponse<String> parsingPostRequest(String url,String jsonData, String specToken) throws Exception{
        if (token == null) {
            System.err.println("Failed to read token");
            return null;
        }
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Private-Token", specToken)
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(jsonData, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if(response!=null && response.statusCode()==201){
            return response;
        }
        else{
            if(response!=null){
                System.out.println(response.body());
                System.out.println("HTTP Request failed, see error above.");
                throw new Exception(response.body());
            }
            else{
                throw new Exception("Connection error of http requests may happen");
            }
        }
        // try{
        //     HttpClient client = HttpClient.newHttpClient();

        //     HttpRequest request = HttpRequest.newBuilder()
        //             .uri(new URI(url))
        //             .header("Private-Token", specToken)
        //             .header("Content-Type", "application/json")
        //             .POST(BodyPublishers.ofString(jsonData, StandardCharsets.UTF_8))
        //             .build();

        //     HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        //     return response;
        // }catch(Exception e){
        //     System.err.println(e.getMessage());
        //     e.printStackTrace();
        //     return null;
        // }
    }

    /**
     * parsing delete request with 'url' and 'specToken'
     * @param url url to do the delete request
     * @param specToken token to the request folder/group/... 
     * @return a HttpResponse<String> response body
     * @throws Exception 
     */
    private HttpResponse<String> parsingDeleteRequest(String url, String specToken) throws Exception{
        if (token == null) {
            System.err.println("Failed to read token");
            return null;
        }
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Private-Token", specToken)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if(response!=null && response.statusCode()==202){
            return response;
        }
        else{
            if(response!=null){
                System.out.println(response.body());
                System.out.println("HTTP Request failed, see error above.");
                throw new Exception(response.body());
            }
            else{
                throw new Exception("Connection error of http requests may happen");
            }
        }
        // try{
        //     HttpClient client = HttpClient.newHttpClient();

        //     HttpRequest request = HttpRequest.newBuilder()
        //             .uri(new URI(url))
        //             .header("Private-Token", specToken)
        //             .DELETE()
        //             .build();

        //     HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        //     return response;
        // }catch(Exception e){
        //     System.err.println(e.getMessage());
        //     e.printStackTrace();
        //     return null;
        // }
    }

    /**
     * get the groupId or the projectId by information stored in db
     * @param path the information stored in db
     * @param group true for group, false for project(repo)
     * @param token the paired token in group
     * @return Id
     * @throws Exception 
     */
    public String getIdByUrl(String path,boolean group,String token) throws Exception{
        path = path.replace("/", "%2f");
        String url = group ? "https://gitlab.oit.duke.edu/api/v4/groups/"+path :"https://gitlab.oit.duke.edu/api/v4/projects/"+path;
        HttpResponse<String> response = parsingGetRequest(url,token);
        return extractFeature(response.body(),"id").get(0);
        // if(response!=null && response.statusCode()==200){
        //     return extractFeature(response.body(),"id").get(0);
        // }
        // else{
        //     System.out.println("HTTP Request failed, see error above.");
        //     return null;
        // }
    }

    /**
     * get all subgroups in one group 
     * @param groupId the group 
     * @return the list of subgroups id 
     * @throws Exception 
     */
    public List<String> getSubgroups(String groupId) throws Exception{
        String url = "https://gitlab.oit.duke.edu/api/v4/groups/"+groupId+"/subgroups";
        HttpResponse<String> response = parsingGetRequest(url,token);
        return extractFeature(response.body(),"id");
        // if(response!=null && response.statusCode()==200){
        //     return extractFeature(response.body(),"id");
        // }
        // else{
        //     System.out.println("HTTP Request failed, see error above.");
        //     return null;
        // }
    }

   /**
     * get all group members in one group 
     * @param groupId the group 
     * @return the list of members id 
     * @throws Exception 
     */
    public List<String> getGroupMembers(String groupId) throws Exception{
        String url = "https://gitlab.oit.duke.edu/api/v4/groups/"+groupId+"/members/all";
        HttpResponse<String> response = parsingGetRequest(url,token);
        return extractFeature(response.body(),"id");
        // if(response!=null && response.statusCode()==200){
        //     return extractFeature(response.body(),"id");
        // }
        // else{
        //     System.out.println("HTTP Request failed, see error above.");
        //     return null;
        // }
    }


    /**
     * get all repos' specific feature info in one group 
     * @param groupId groupId
     * @param feature the specific feature
     * @return all repos' feature info
     * @throws Exception 
     */
    public List<String> getAllReposFeatureInGroup(String groupId, String feature) throws Exception{
        String url = "https://gitlab.oit.duke.edu/api/v4/groups/"+groupId+"/projects";
        HttpResponse<String> response = parsingGetRequest(url,token);
        return extractFeature(response.body(),feature);
        // if(response!=null && response.statusCode()==200){
        //     return extractFeature(response.body(),feature);
        // }
        // else{
        //     System.out.println("HTTP Request failed, see error above.");
        //     return null;
        // }
    }

    /**
     * get the repo's specific feature info by its name in one group 
     * @param name name of the repo
     * @param feature the required feature
     * @return the required feature info
     * @throws Exception 
     */
    public String getFeatureInProjectByName(String name,String feature) throws Exception{
        String url = "https://gitlab.oit.duke.edu/api/v4/projects?search="+name;
        HttpResponse<String> response = parsingGetRequest(url,token);
        return extractFeature(response.body(),feature).get(0);
        // if(response!=null && response.statusCode()==200){
        //     return extractFeature(response.body(),feature).get(0);
        // }
        // else{
        //     System.out.println("HTTP Request failed, see error above.");
        //     return null;
        // }
    }

    private String generateRepoName(Section section,String netId){
        // Calendar calendar = Calendar.getInstance();
        // // Get the current month (0-based index, so add 1)
        // int currentMonth = calendar.get(Calendar.MONTH) + 1;
        // int currentYear = calendar.get(Calendar.YEAR);
        // String abs = String.valueOf(currentYear).substring(2);
        // if(currentMonth<=5 && currentMonth>=1){
        //     return "sp"+abs+"_" + course.getSubject() + "_" + course.getNumber() +"_" + netId;
        // }   
        // else{
        //     return "f"+abs+"_" + course.getSubject() + "_" + course.getNumber() +"_" + netId;
        // }
        return section.getSemesterYear()+"_" + section.getCourse().getSubject() + section.getCourse().getNumber() +"_" + netId;
    }

    /**
     * create student repository in one group
     * @param course course the student enrolled
     * @param groupId  groupId of the group
     * @param id student's id
     * @return the student's repo's id
     * @throws Exception 
     */
    public String createRepo(Course course, String groupId, String id) throws Exception {
        Section section = course.getSectionList().get(course.getSectionList().size()-1);
        String projectName = generateRepoName(section,id);
        //convert README initialization to false
        String jsonData = "{" +
        "\"name\": \""+projectName+"\"," +
        "\"visibility\": \"private\"," +
        "\"initialize_with_readme\": \"false\"," +
        "\"namespace_id\": "+groupId +
        "}";
        HttpResponse<String> response = parsingPostRequest("https://gitlab.oit.duke.edu/api/v4/projects", jsonData,token);
        return extractFeature(response.body(),"id").get(0);
        // if(response!=null && response.statusCode()==201){
        //     // JsonArray jsonArray = new JsonArray();
        //     // jsonArray.add(JsonParser.parseString(response.body()).getAsJsonObject());  // Adding the whole JsonObject as an element in JsonArray
        //     return extractFeature(response.body(),"id").get(0);
        // }
        // else{
        //     System.out.println("HTTP Request failed, see error above.");
        //     return null;
        // }
    }

    /**
     * invite student to one repo by email
     * @param projectId the repo(project)Id
     * @param email the email of student
     * @throws Exception 
     */
    public void inviteStuToRepoByEmail(String projectId, String email) throws Exception {
        System.out.println(email);
        String jsonData = "{" +
        "\"email\": \""+email+"\"," +
        "\"access_level\": \"" + 30 + "\"" +
        "}";
        parsingPostRequest("https://gitlab.oit.duke.edu/api/v4/projects/"+projectId+"/invitations", jsonData,token);
    }

    /**
     * delete one group
     * @param groupId id of the group
     * @throws Exception 
     */
    public void deleteGroup(String groupId) throws Exception{
        String url = "https://gitlab.oit.duke.edu/api/v4/groups/"+groupId;
        HttpResponse<String> response = parsingDeleteRequest(url,token);
        // if(response!=null && response.statusCode()==202){
        //     return;
        // }
        // else{
        //     System.out.println("HTTP Request failed, see error above.");
        // }
    }

    /**
     * delete one project
     * @param proId id of the project
     * @throws Exception 
     */
    public void deleteRepo(String proId) throws Exception{
        String url = "https://gitlab.oit.duke.edu/api/v4/projects/"+proId;
        HttpResponse<String> response = parsingDeleteRequest(url,token);
        // if(response!=null && response.statusCode()==202){
        //     return;
        // }
        // else{
        //     System.out.println("HTTP Request failed, see error above.");
        // }
    }

    /**
     * get all initial assessments in the course
     * @param course the course
     * @return all initial assessments without prerequisite
     * @throws SQLException
     */
    public List<Assessment> initialAssessment(Course course) throws SQLException{
        List<Assessment> initials = pqDAO.getAssessmentWithoutPrereq(course);
        return initials;
    }

    private String getDeliveryFolderName(String sourceProjectId, String assn, String branch) throws Exception {
        String url = "https://gitlab.oit.duke.edu/api/v4/projects/" + sourceProjectId + "/repository/tree?path=" + assn + "&ref=" + branch;
        HttpResponse<String> response = parsingGetRequest(url, sourceToken);
        List<String> folders = extractFeature(response.body(), "name");
        String ans = null;
        for(String folder:folders){
            if(folder.toUpperCase().equals("DELIVERY")){
                ans = folder;
            }
        }
        return ans;
    }

    /**
     * get all files in the source assessment repo of one specific assessment 
     * @param sourceProjectId the source assessment's repo Id
     * @param assn the assessment's number & the title of the folder
     * @param branch the branch of the assessment 
     * @return a JsonArray of the files of the assessment 
     * @throws Exception
     */
    public JsonArray fetchFileList(String sourceProjectId, String assn, String branch) throws Exception {
        String url = "https://gitlab.oit.duke.edu/api/v4/projects/" + sourceProjectId + "/repository/tree?path=" + assn + "&ref=" + branch;
        HttpResponse<String> response = parsingGetRequest(url, sourceToken);
        return JsonParser.parseString(response.body()).getAsJsonArray();
    }

    /**
     * get the file's content in the source assessment repo of one specific assessment's one file
     * @param sourceProjectId the source assessment's repo Id
     * @param filePath the file's path in the repo
     * @param branch the branch of the assessment 
     * @return a String of the file content
     * @throws Exception
     */
    public String fetchFileContent(String sourceProjectId, String filePath, String branch) throws Exception {
        String url = "https://gitlab.oit.duke.edu/api/v4/projects/" + sourceProjectId + "/repository/files/" 
        + filePath.replace("/", "%2F") + "/raw?ref=" + branch;
        
        HttpResponse<String> response = parsingGetRequest(url, sourceToken);
        return response.body();
    }

    private String ignorePrefix(String filePath, String prefix){
        String[] split = filePath.split("/");
        List<String> resultList = new ArrayList<String>();
        for(String path:split){
            if(path.toUpperCase().equals(prefix)){
                continue;
            }
            else{
                resultList.add(path);
            }
        }
        return String.join("%2F", resultList);
    }

    /**
     * push the file content to the target student's repo, remove the 'DELIVERY' prefix
     * @param targetRepo target student's repo
     * @param filePath file path of the specific file
     * @param branch the branch of the student's repo 
     * @param content content of the file
     * @throws Exception
     */
    public void pushFileContent(String targetRepo, String filePath, String branch, String content) throws Exception {
        String newFilePath = ignorePrefix(filePath, "DELIVERY");
        String url = "https://gitlab.oit.duke.edu/api/v4/projects/" + targetRepo + "/repository/files/" + newFilePath;
        String encodedContent = Base64.getEncoder().encodeToString(content.getBytes());

        String jsonData = "{"
                + "\"branch\": \"" + branch + "\","
                + "\"content\": \"" + encodedContent + "\","
                + "\"commit_message\": \"Add " + filePath + " from source project\","
                + "\"encoding\": \"base64\""
                + "}";

        HttpResponse<String> response = parsingPostRequest(url, jsonData, token);
        System.out.println("Response code: " + response.statusCode());
        System.out.println("Response body: " + response.body());
    }

    /**
     * send the course's README from source repo to student's repo when creating the repository
     * @param targetRepo the student's repo id
     * @param sourceProjectId the course's assessment repo id
     * @param targetBranch the target branch of the student's repo
     * @param sourceBranch the source branch of the source assessment's repo
     * @throws Exception
     */
    public void sendSourceREADME(String targetRepo, String sourceProjectId, String targetBranch, String sourceBranch) throws Exception{
        String content = fetchFileContent(sourceProjectId,"README.md",sourceBranch);
        pushFileContent(targetRepo, "README.md", sourceBranch, content);
    }

    private void pushDocker(String path){
        String[] commands = readCommands(path);
        runCommand(commands);
    }

    private void sendCIYML(String targetRepo, String targetBranch, String path, String netId) throws Exception{
        String content = readResourceFile("cicd_template.txt");
        content = content.replace("%title_path", path.toLowerCase());
        content = content.replace("%testcase","p4ml");
        content = content.replace("%repo_name", "repo_"+netId);
        pushFileContent(targetRepo, ".gitlab-ci.yml", targetBranch, content);
    }

    /**
     * transfer files from one folder in source repo to the student's repo 
     * @param targetRepo target student's repoId
     * @param sourceProjectId source assessments storage repo Id
     * @param assn the assessment's number & the title of the folder
     * @param targetBranch student's branch 
     * @param sourceBranch source repo's branch
     * @throws Exception
     */
    public void transferFiles(String targetRepo, String sourceProjectId, String assn, String targetBranch, String sourceBranch) throws Exception {
        System.out.println(assn);
        JsonArray filesArray = fetchFileList(sourceProjectId, assn, sourceBranch);
        for (JsonElement fileElement : filesArray) {
            String filePath = fileElement.getAsJsonObject().get("path").getAsString();
            if (fileElement.getAsJsonObject().get("type").getAsString().equals("blob")) {
                String fileContent = fetchFileContent(sourceProjectId, filePath, sourceBranch);
                pushFileContent(targetRepo, filePath, targetBranch, fileContent);
                Thread.sleep(100); // 0.1-second delay between operations
            } else if (fileElement.getAsJsonObject().get("type").getAsString().equals("tree")) {
                // Ensure the directory exists in the target project
                String placeholderPath = filePath + "/.gitkeep";
                pushFileContent(targetRepo, placeholderPath, targetBranch, "");
                // Recursive call to handle subdirectories
                transferFiles(targetRepo, sourceProjectId, filePath, targetBranch,sourceBranch);
            }
        }
    }

    //---------------Below functions are used of local path of assessments----------------

    /**
     * get all files in the source assessment repo of one specific assessment by local path
     * @param assn the assessment's number & the title of the folder
     * @return a JsonArray of the files of the assessment 
     * @throws Exception
     */
    public List<Path> fetchFileList(String assn) throws Exception {
        // try (Stream<Path> paths = Files.walk(Paths.get(sourceRepo+"/"+assn))) {
        //     return paths.collect(Collectors.toList());
        // }

        String fullPath = sourceRepo + "/" + assn + "/DELIVERY";
        Path path = Paths.get(fullPath);
        
        if (!Files.exists(path)) {
            throw new NoSuchFileException(fullPath);
        }

        try (Stream<Path> paths = Files.walk(path)) {
            return paths
                .filter(Files::isRegularFile) // Include only regular files
                .collect(Collectors.toList());
        }
    }

    public String newRelativePath(String filePath, String assn, String prefix){
        String[] split = filePath.split("/");
        List<String> resultList = new ArrayList<String>();
        resultList.add(assn);
        Boolean afterDelivery = false;
        for(String path:split){
            if(afterDelivery){
                resultList.add(path);
            }
            else{
                if(path.toUpperCase().equals(prefix)){
                    afterDelivery = true;
                }
            }
            
        }
        return String.join("%2F", resultList);
    }

    public String fetchFileContent(Path file) throws IOException{
        return Files.readString(file);
    }

    /**
     * push the file content to the target student's repo with the new relative path shown on gitlab repo
     * @param targetRepo target student's repo
     * @param newFilePath file path of the specific file shown in gitlab
     * @param branch the branch of the student's repo 
     * @param content content of the file
     * @throws Exception
     */
    public void pushFileContentNew(String targetRepo, String branch, String newFilePath, String content) throws Exception {
        String url = "https://gitlab.oit.duke.edu/api/v4/projects/" + targetRepo + "/repository/files/" + newFilePath;
        String encodedContent = Base64.getEncoder().encodeToString(content.getBytes());

        String jsonData = "{"
                + "\"branch\": \"" + branch + "\","
                + "\"content\": \"" + encodedContent + "\","
                + "\"commit_message\": \"Add " + newFilePath.replace("%2F", "/") + " from source project\","
                + "\"encoding\": \"base64\""
                + "}";

        HttpResponse<String> response = parsingPostRequest(url, jsonData, token);
        System.out.println("Response code: " + response.statusCode());
        System.out.println("Response body: " + response.body());
    }

    /**
     * send the course's README from source repo to student's repo when creating the repository
     * @param course the course, in order to get the repo root directory
     * @param targetRepo the student's repo id
     * @param targetBranch the target branch of the student's repo
     * @throws Exception
     */
    public void sendSourceREADME(String targetRepo, String targetBranch) throws Exception{
        Path path = Paths.get(sourceRepo+"/README.md");
        if(Files.exists(path)){
            String content = fetchFileContent(path);
            pushFileContentNew(targetRepo, targetBranch, "README.md", content);
        }
    }

    /**
     * transfer files from one folder in source repo to the student's repo 
     * @param targetRepo target student's repoId
     * @param assn the assessment's number & the title of the folder
     * @param targetBranch student's branch 
     * @throws Exception
     */
    public void transferFiles(String targetRepo, String assn, String targetBranch) throws Exception {
        System.out.println(assn);
        List<Path> filesFolders = fetchFileList(assn);     
        for (Path p : filesFolders) {
            String fileContent = fetchFileContent(p);
            String newFilePath = newRelativePath(p.toString(), assn, "DELIVERY");
            pushFileContentNew(targetRepo, targetBranch, newFilePath, fileContent);
            Thread.sleep(100); // 0.1-second delay between operations
        }
    }    
    
    //---------------------------------end of the local path functions-----------------------

    /**
     * create repository of student and deliver the README & initial assessments
     * @param course the course
     * @param user the student 
     * @param delivery the delivery information 
     * @throws Exception
     */
    public void createRepoAndDeliverInitialAssn(Course course,Student user,Delivery delivery) throws Exception{
        courseDAO.load(course);
        String repoId = createRepo(course, groupId, user.getNetId());
        //=== dont need remote source repo
        //String sourceRepo = course.getRepo();
        //String sourceId = getIdByUrl(sourceRepo, false,sourceToken);
        //sendSourceREADME(repoId, sourceId, "main", "main");
        sendSourceREADME(repoId, "main");
        //=== dont need the repo yml file 
        //pushDocker(groupPath+"/repo_"+user.getNetId());
        //sendCIYML(repoId, "main", groupPath+"/repo_"+user.getNetId(),user.getNetId());

        //=== dont need to check prereq by delivery itself 
        // List<Assessment> initials = initialAssessment(course);
        // for(Assessment assn:initials){
        //     System.out.println(assn.getAssn());
        //     String deliveryName = getDeliveryFolderName(sourceId,assn.getAssn(),"main");
        //     if(deliveryName==null){
        //         throw new Exception("The sturcture inside the source assessment folder is wrong, please make sure there is a folder named DELIVERY(not sensitive to cases)");
        //     }
        //     transferFiles(repoId, sourceId, assn.getAssn()+"/"+deliveryName, "main","main");
        //     delivery.setAssessment(assn);
        //     deliverDAO.deliverAssn(delivery);
        // }
        Assessment assessment = assessmentDAO.getAssessmentByPK(course, delivery.getAssessment().getAssn());
        delivery.setAssessment(assessment);
        //transferFiles(repoId, sourceId, delivery.getAssessment().getAssn(), "main","main");
        transferFiles(repoId, assessment.getAssn(), "main");
        deliverDAO.deliverAssn(delivery);
        inviteStuToRepoByEmail(repoId, user.getEmail());
        delivery.setStatus(Status.COMPLETED);
        delivery.setLog("Created repo; Added initial assessments; Sent invitations to student.");
        
    }
    
    /**
     * check whether student has passed all the assessments 
     * @param student the student
     * @param course the course
     * @param assessments the assessments waiting to be checked
     * @return true-pass all,flase-no
     * @throws SQLException
     */
    public Boolean checkPass(Student student, Course course, List<Assessment> assessments) throws SQLException{
        for(Assessment ass:assessments){
            Grade grade = gradeDAO.getPassingGradeForStudet(student.getNetId(),course,ass.getAssn());
            if(grade==null || !assessmentDAO.pass(grade)){
                System.out.println(ass.getAssn());
                return false;
            }
        }
        return true;
    }

    /**
     * check whether an assessment can be started by date
     * @param assessment the assessment 
     * @return true-can start, false-cant
     * @throws SQLException 
     */
    public Boolean checkStart(Assessment assessment) throws SQLException{
        assessmentDAO.load(assessment);
        LocalDateTime now = LocalDateTime.now();
        if(now.isAfter(assessment.getStartDate())){
            return true;
        }
        return false;
    }

    /**
     * get a list of assessments that can be delivered to the student 
     * @param student the student 
     * @param course the course
     * @return the list of assessments that can be delivered to the student 
     * @throws Exception
     */
    public List<String> checkPossibleAssessments(Student student,Course course) throws Exception{
        List<Assessment> allAssn = assessmentDAO.getAllAssessmentsForCourse(course);
        List<String> newDelivery = new ArrayList<>();
        for(Assessment a:allAssn){
            //check if db already contains the grade, if so continue
            if(gradeDAO.getPassingGradeForStudet(student.getNetId(), course, a.getAssn())!=null){
                continue;
            }
            //check if meet all prerequisite, if not continue
            List<Assessment> assns = pqDAO.getPrereq(a);
            if(!checkPass(student, course, assns)){
                continue;
            }
            newDelivery.add(a.getAssn());
        }
        return newDelivery;
    }

    /**
     * send one assessment to the student 
     * need Course course and Assessment - it can work for resending the assessment & send next assessments
     * @param repoId the student's repo to be sent
     * @param student the student
     * @param course the course 
     * @param assn the assessment number of the assessment  
     * @param delivery the delivery data
     * @throws Exception
     */
    public void sendAssessment(String repoId, Student student, Course course, String assn, Delivery delivery) throws Exception{
        //pass the prereq? If N -> do nothing
        //pass the start date? If N -> do nothing
        courseDAO.load(course);
        String sourceRepo = course.getRepo();
        setSourceRepo(sourceRepo);
        //String sourceId = getIdByUrl(sourceRepo, false,sourceToken);
        Assessment nextAssn = assessmentDAO.getAssessmentByPK(course, assn);
        List<Assessment> assns = pqDAO.getPrereq(nextAssn);
        //dont check pass anymore
        if(checkStart(nextAssn)){
            // === no need to check sturcture right now, replace to local
            // String deliveryName = getDeliveryFolderName(sourceId,assn,"main");
            // if(deliveryName==null){
            //     throw new Exception("The sturcture inside the source assessment folder is wrong, please make sure there is a folder named DELIVERY(not sensitive to cases)");
            // }
            //transferFiles(repoId, sourceId, assn+"/"+deliveryName, "main","main");
            transferFiles(repoId, assn, "main");
            delivery.setStatus(Status.COMPLETED);
            delivery.setLog("Sent the assessment to Student");
            delivery.setAssessment(nextAssn);
            deliverDAO.deliverAssn(delivery);
            return;
        }
        // if(checkPass(student, course, assns)){
        //     if(checkStart(nextAssn)){
        //         String deliveryName = getDeliveryFolderName(sourceId,assn,"main");
        //         if(deliveryName==null){
        //             throw new Exception("The sturcture inside the source assessment folder is wrong, please make sure there is a folder named DELIVERY(not sensitive to cases)");
        //         }
        //         transferFiles(repoId, sourceId, assn+"/"+deliveryName, "main","main");
        //         delivery.setStatus(Status.COMPLETED);
        //         delivery.setLog("Sent the assessment to Student");
        //         delivery.setAssessment(nextAssn);
        //         deliverDAO.deliverAssn(delivery);
        //         return;
        //     }
        // }

        // String log = "fail to add "+assn+" to user "+student.getNetId()+" with some reason, not started or not finish all pre-requisite.";
        // Delivery delivery = new Delivery(LocalDateTime.now(), Status.ERROR, log, student, nextAssn);
        // deliverDAO.save(delivery);
        delivery.setLog("cannot send the Assessment, pls check the start date all all prerequisite");
        return;

    }

    /**
     * get the section of the student in one course 
     * If cannot find the section, turns status to ERROR
     * @param delivery delivery data, including the student's info
     * @param course the course 
     * @return the section of the student in the course
     * @throws Exception
     */
    public Section setupSection(Delivery delivery,Course course) throws Exception{
        //checkStudent's Section by Enrollment
        courseDAO.load(course);
        setSourceToken(course.getRepoToken());
        setSourceRepo(course.getRepo());
        Student student = delivery.getStudent();
        Section section = enrollmentDAO.getSection(course,student);
        if(section==null){
            delivery.setStatus(Status.ERROR);
            delivery.setLog("Cannot find the student's section, please register the gitlab group for the section first");
        }
        List<Section> secList = new ArrayList<>();
        secList.add(section);
        course.setSectionList(secList);
        return section;
    }

    /**
     * set up target group path, group Id of the section and its token
     * @param delivery delivery data
     * @param section the section
     * @throws Exception 
     */
    public void setupTargetGroupAndToken(Delivery delivery,Section section) throws Exception{
        setToken(section.getGitlab_token());
        setGroupPath(section.getGitlab_group());
        String groupId = getIdByUrl(section.getGitlab_group(), true,token);
        setGroupId(groupId);
    }

    /**
     * initial delivery, when the delivery.status == INIT. create repo&send initial assessments. 
     * If delivery not works, turns status to ERROR
     * @param delivery the delivery data
     * @throws Exception
     */
    public void initDelivery(Delivery delivery) throws Exception{
        Course course = new Course(delivery.getAssessment().getSubject(), delivery.getAssessment().getNumber());
        Student student = delivery.getStudent();
        Section section = setupSection(delivery, course);
        if(section==null){
            return;
        }
        //checkWhether Exist
        setupTargetGroupAndToken(delivery, section);
        List<String> repoNames = getAllReposFeatureInGroup(groupId,"name");
        //if not, create&sendinitialAssessment
        String repoName = generateRepoName(section, student.getNetId());
        if(repoNames.contains(repoName)){
            delivery.setStatus(Status.COMPLETED);
            delivery.setLog("already created the repo");
            return;
        }
        createRepoAndDeliverInitialAssn(course,student,delivery);
    }

    /**
     * check the repository Id of one student in the group 
     * If not exists, turns status to ERROR
     * @param delivery delivery data
     * @param student the student 
     * @return the Id of the repo
     * @throws Exception 
     */
    private String checkRepoExistence(Delivery delivery, Section section, Student student) throws Exception{
        //check whether the student's repo exists
        List<String> repoNames = getAllReposFeatureInGroup(groupId,"name");
        String id = null;
        String projectName = generateRepoName(section, student.getNetId());
        if(repoNames.contains(projectName)){
            id = getFeatureInProjectByName(projectName,"id");
            setStudentRepo(id);
        }
        else{
            delivery.setStatus(Status.ERROR);
            delivery.setLog("Cannot find the repository for the student, please check all the information");
        }
        return id;
    }

    /**
     * common delivery, when the delivery.status == DELIVER. send assessments when prereq pass
     * If delivery not works, turns status to ERROR
     * @param delivery the delivery data
     * @throws Exception
     */
    public void deliverDelivery(Delivery delivery) throws Exception{
        Course course = new Course(delivery.getAssessment().getSubject(), delivery.getAssessment().getNumber());
        Student student = delivery.getStudent();
        Section section = setupSection(delivery, course);
        Assessment assessment = delivery.getAssessment();
        if(section==null){
            return;
        }
        setupTargetGroupAndToken(delivery,section);
        String repoId = checkRepoExistence(delivery, section, student);
        if(repoId==null){
            return;
        }
        //check if meet all prerequisite, if not continue, dont check anymore
        // List<Assessment> assns = pqDAO.getPrereq(assessment);
        // if(!checkPass(student, course, assns)){
        //     //set log to...
        //     delivery.setStatus(Status.ERROR);
        //     delivery.setLog("The student has not passed all the required prerequisites");
        //     return;
        // }
        //check whether it already delivered
        if(deliverDAO.checkDelivery(assessment, student)){
            //set log to...
            delivery.setStatus(Status.COMPLETED);
            delivery.setLog("already sent the assessment");
            return;
        }
        //sendAssessment
        sendAssessment(repoId,student,course,assessment.getAssn(),delivery);
    }
}
