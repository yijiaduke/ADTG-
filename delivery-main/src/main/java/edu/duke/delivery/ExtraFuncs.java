package edu.duke.delivery;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.core.io.ResourceLoader;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ExtraFuncs {

    private String groupId; //the group for this section
    private String token; //the token for the group access level: owner
    private String studentRepo;//it will be set later for the student's project Id
    private String sourceToken;//it will be set later for the source's token
    private String sourceRepo; // it will bet set later by course.getRepo


    private List<String> extractFeature(String body, String feature){
        return null;
    }
    private HttpResponse<String> parsingGetRequest(String url, String specToken){
        return null;
    }

    private HttpResponse<String> parsingPostRequest(String url,String jsonData, String specToken){
        return null;
    }

    private HttpResponse<String> parsingDeleteRequest(String url, String specToken){
        return null;
    }

    private void readProperties(){
        Properties properties = new Properties();
        try (InputStream input = ResourceLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            // Load the properties file
            properties.load(input);

            // Access properties
            String Id = properties.getProperty("targetGroupId");
            String token = properties.getProperty("targetGroupToken");

            this.groupId = Id;
            this.token = token;
            this.sourceToken = properties.getProperty("sourceAssnToken");
            this.sourceRepo = properties.getProperty("sourceRepoId");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private List<String> getUserOverAccessLevel(String groupId,int level){
        String url = "https://gitlab.oit.duke.edu/api/v4/groups/"+groupId+"/members/all";
        HttpResponse<String> response = parsingGetRequest(url,token);
        if(response!=null && response.statusCode()==200){
            List<String> users = extractFeature(response.body(), "id");
            List<String> alevel = extractFeature(response.body(),"access_level");
            List<String> admins = new ArrayList<>();
            for(int i =0;i<users.size();i++){
                if(Integer.parseInt(alevel.get(i))>=level){
                    admins.add(users.get(i));
                }
            }
            return admins;
        }
        else{
            System.out.println("HTTP Request failed, see error above.");
            return null;
        }
    }

    private void createSubgroup(String groupId,List<String> Ids){
        List<String> admins = getUserOverAccessLevel(groupId, 40);

        for (String id : Ids) {
            String subgroupName = "sub_"+id;
            String users = String.join(",", admins) + ","+id;
            String jsonData = "{" +
                "\"path\": \""+subgroupName+"\"," +
                "\"name\": \""+subgroupName+"\"," +
                "\"visibility\": \"private\"," +
                "\"parent_id\": "+groupId+"," +
                "\"user_ids\": ["+users+"]" +
                "}";
            parsingPostRequest("https://gitlab.oit.duke.edu/api/v4/groups", jsonData,token);
        }

    }
    
    private List<String> createRepo(String groupId, List<String> Ids) {
        //List<String> admins = getUserOverAccessLevel(groupId, 40);
        List<String> repoIds = new ArrayList<>();
        for (String id : Ids) {
            String projectName = "repo_"+id;
            // String users = String.join(",", admins) + ","+id;
            // String jsonData = "{" +
            //     "\"name\": \""+projectName+"\"," +
            //     "\"visibility\": \"private\"," +
            //     "\"namespace_id\": "+groupId+"," +
            //     "\"user_ids\": ["+users+"]" +
            //     "}";
            String jsonData = "{" +
            "\"name\": \""+projectName+"\"," +
            "\"visibility\": \"private\"," +
            "\"initialize_with_readme\": \"true\"," +
            "\"namespace_id\": "+groupId +
            "}";
            HttpResponse<String> response = parsingPostRequest("https://gitlab.oit.duke.edu/api/v4/projects", jsonData,token);
            if(response!=null && response.statusCode()==201){
                JsonArray jsonArray = new JsonArray();
                jsonArray.add(JsonParser.parseString(response.body()).getAsJsonObject());  // Adding the whole JsonObject as an element in JsonArray
                repoIds.addAll(extractFeature(jsonArray.toString(),"id"));
            }
            else{
                System.out.println("HTTP Request failed, see error above.");
                return null;
            }
        }
        return repoIds;
    }

    private void updateDefaultBranch(String projectId,Integer alevel){
        String url = "https://gitlab.oit.duke.edu/api/v4/projects/"+projectId+"/protected_branches/main";
        HttpResponse<String> response = parsingDeleteRequest(url,token);
        System.out.println(response.statusCode());
        if(response!=null && response.statusCode()==204){
            String jsonData = "{" +
            "\"name\": \"main\"," +
            "\"push_access_level\": \"" + alevel + "\"," +
            "\"merge_access_level\": \"" + alevel + "\""+
            "}";
            
            parsingPostRequest("https://gitlab.oit.duke.edu/api/v4/projects/"+projectId+"/protected_branches", jsonData,token);
        }
        else{
            System.out.println("HTTP Request failed, see error above.");
            return;
        }
        try {
            Thread.sleep(1000); // Sleep for 1 second
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
