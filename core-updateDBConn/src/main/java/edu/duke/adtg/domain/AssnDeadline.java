package edu.duke.adtg.domain;

import java.time.LocalDateTime;

public class AssnDeadline {

    private Assessment assessment; // (c_subject, c_number, assn)
    private Student student; //(netid)
    private LocalDateTime due;
    // private char assnToGrade;


    //Constructor

    public AssnDeadline(){}


    public AssnDeadline(Assessment assessment, Student student, LocalDateTime due){
        this.assessment = assessment;
        this.student = student;
        this.due = due;
    }

    // public AssnDeadline(Assessment assessment, Student student, LocalDateTime due, char assnToGrade){
    //     this.assessment = assessment;
    //     this.student = student;
    //     this.due = due;
    //     this.assnToGrade = assnToGrade;
    // }


    //Getters and Setters
    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public LocalDateTime getDue() {
        return due;
    }

    public void setDue(LocalDateTime due) {
        this.due = due;
    }
}
