package edu.duke.adtg.domain;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Section {

    private Integer sectionId;

    private Course course;

    private LocalDate date;

    private Instructor instructor;

    private String gitlab_group;

    private String gitlab_token;

    private List<Student> enrollStudents;
    
    //Constructor

    public Section(){}

    public Section(Integer sectionId, Course course) {
       this.sectionId = sectionId;
       this.course = course;
    }

    public Section (Integer sectionId, Course course, String gitlab_group){ 
        this.sectionId = sectionId;
        this.course = course;
        this.gitlab_group = gitlab_group;
    }

    public Section (Integer sectionId, Course course, LocalDate date, Instructor instructor){ 
        this.sectionId = sectionId;
        this.course = course;
        this.date = date;
        this.instructor = instructor;
    }

    public Section(Integer sectionId, Course course, Instructor instructor, String gitlab_group, List<Student> enrollStudents) {
        this.sectionId = sectionId;
        this.course = course;
        this.instructor = instructor;
        this.gitlab_group = gitlab_group;
        this.enrollStudents = enrollStudents;
    }

    public Section(Integer sectionId, Course course, LocalDate date, Instructor instructor, String gitlab_group, String gitlab_token) {
        this.sectionId = sectionId;
        this.course = course;
        this.date = date;
        this.instructor = instructor;
        this.gitlab_group = gitlab_group;
        this.gitlab_token = gitlab_token;
    }

    //Getter and Setter
    public Integer getSectionId() {
        return sectionId;
    }

    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    public List<Student> getEnrollStudents() {
        return enrollStudents;
    }

    public void setEnrollStudents(List<Student> enrollStudents) {
        this.enrollStudents = enrollStudents;
    }

    public String getGitlab_group() {
        return gitlab_group;
    }

    public void setGitlab_group(String gitlab_group) {
        this.gitlab_group = gitlab_group;
    }

    public String getGitlab_token() {
        return gitlab_token;
    }

    public void setGitlab_token(String gitlab_token) {
        this.gitlab_token = gitlab_token;
    }
    public String getSemesterYear() {
        String year = date.format(DateTimeFormatter.ofPattern("yy"));
        int month = date.getMonthValue();

        String semester;
        if (month >= 1 && month <= 4) {
            semester = "s";
        } else if (month >= 5 && month <= 7) {
            semester = "su";
        } else {
            semester = "f";
        }
        return semester + year;
    }
}

