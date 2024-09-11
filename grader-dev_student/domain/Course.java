package edu.duke.adtg.domain;

import java.util.ArrayList;
import java.util.List;

public class Course {

    private String subject;
    private Integer number;
    private String title;
    private String repo;
    private List<Section> sectionList;

    //private List<Assessment> assessments;

    public Course(String subject,Integer number, String title,String repo,
                  List<Section> sectionList) {
        this.subject = subject;
        this.number = number;
        this.title = title;
        this.repo = repo;
        this.sectionList = sectionList;
        this.subject = this.subject+" ".repeat(10 - subject.length());
    }

    //==============================================================================
    //============= Webapp use =====================================================
    //==============================================================================
    public Course(String subject, Integer number){
        this.subject = subject;
        this.number = number;
    }


    //==============================================================================
    //============= Webapp use =====================================================
    //==============================================================================


    public Course(String subject, Integer number,String title){
        this(subject,number,title, "gitlab@initializing", new ArrayList<Section>());
    }


    public Course(String subject, Integer number,String title,String repo){
        this(subject,number,title, repo, new ArrayList<Section>());
    }

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

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
}

