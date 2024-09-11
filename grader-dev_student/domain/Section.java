package edu.duke.adtg.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Section {

    private Integer sectionId;

    private Course course;

    private LocalDate date;

    private Instructor instructor;

    private List<Student> enrollStudents;


    public Section(Integer sectionId, Course course, Instructor instructor, List<Student> enrollStudents) {
        this.sectionId = sectionId;
        this.course = course;
        this.instructor = instructor;
        this.enrollStudents = enrollStudents;
    }

    public Section (Integer sectionId, Course course, LocalDate date, Instructor instructor){ 
        this.sectionId = sectionId;
        this.course = course;
        this.date = date;
        this.instructor = instructor;
    }

    public Section(Integer sectionId, Course course) {
        this(sectionId,course,null,new ArrayList<Student>());
    }

    public List<Student> getEnrollStudents() {
        return enrollStudents;
    }

    public void setEnrollStudents(List<Student> enrollStudents) {
        this.enrollStudents = enrollStudents;
    }

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

    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}

