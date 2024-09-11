package edu.duke.adtg.domain;

import java.time.LocalDateTime;

public class Delivery {

    //should add c_subject and c_number which are cols in db?

    private LocalDateTime time;

    private Status status;//may change

    private String log;

    private Student student;

    private Assessment assessment;



    public Delivery(LocalDateTime time, Status status, String log, Student student, Assessment assessment) {
        this.time = time;
        this.status = status;
        this.log = log;
        this.student = student;
        this.assessment = assessment;
    }
    

    public LocalDateTime getTime() {
        return time;
    }


    public Status getStatus() {
        return status;
    }


    public String getLog() {
        return log;
    }


    public Student getStudent() {
        return student;
    }


    public Assessment getAssessment() {
        return assessment;
    }
}
