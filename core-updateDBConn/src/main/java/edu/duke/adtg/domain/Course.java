package edu.duke.adtg.domain;

import java.util.ArrayList;
import java.util.List;

public class Course {

    private String subject;
    private Integer number;
    private String title;
    private String repo;
    private String repoToken;
    private List<Section> sectionList;

    //private List<Assessment> assessments;

    //Constructor
    public Course(){
    }

    public Course(String subject,Integer number, String title,String repo,String token,
                  List<Section> sectionList) {
        this.subject = subject;
        this.number = number;
        this.title = title;
        this.repo = repo;
        this.sectionList = sectionList;
        // this.subject = this.subject+" ".repeat(10 - subject.length());
        this.repoToken = token;
    }

    public Course(String subject, Integer number){
        this.subject = subject;
        this.number = number;
    }

    public Course(String subject, Integer number,String title){
        this.subject = subject;
        this.number = number;
        this.title = title;
    }

    public Course(String subject, Integer number,String title,String repo,String token){
        this(subject,number,title, repo, token,new ArrayList<Section>());
    }
    
    
    //Setter and Getter
    public List<Section> getSectionList() {
        return sectionList;
    }

    public void setSectionList(List<Section> sectionList) {
        this.sectionList = sectionList;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getRepoToken() {
        return repoToken;
    }

    public void setRepoToken(String repoToken) {
        this.repoToken = repoToken;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
}

