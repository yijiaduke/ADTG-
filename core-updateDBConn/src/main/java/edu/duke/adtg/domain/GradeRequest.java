package edu.duke.adtg.domain;

import java.time.LocalDateTime;

public class GradeRequest {

    private Long id;
    private Assessment assessment; //(c_subject, c_number, assn)
    private Student student; //(netid)
    private LocalDateTime requestTime;
    private String status;
    private LocalDateTime expiresTime;
    private String owner;
    private String commitID;
    private char applyPenalty;
    private String logText;

    // Default constructor
    public GradeRequest() {
    }

    // full constructor
    public GradeRequest(Long id, Assessment assessment, Student student, LocalDateTime requestTime, String status, LocalDateTime expiresTime, String owner, String logText, String commitID, char applyPenalty) {
        this.id = id;
        this.assessment = assessment;
        this.student = student;
        this.requestTime = requestTime;
        this.status = status;
        this.expiresTime = expiresTime;
        this.owner = owner;
        this.logText = logText;
        this.commitID = commitID;
        this.applyPenalty = applyPenalty;
    }


    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCommitID() {
        return commitID;
    }

    public void setCommitID(String commitID) {
        this.commitID = commitID;
    }

    public LocalDateTime getExpiresTime() {
        return expiresTime;
    }

    public void setExpiresTime(LocalDateTime expiresTime) {
        this.expiresTime = expiresTime;
    }

    public char getApplyPenalty() {
        return applyPenalty;
    }

    public void setApplyPenalty(char applyPenalty) {
        this.applyPenalty = applyPenalty;
    }

    public String getLogText() {
        return logText;
    }

    public void setLogText(String logText) {
        this.logText = logText;
    }

    // toString method for better readability
    @Override
    public String toString() {
        return "GradeRequest{" +
                "id=" + id +
                ", assessment=" + assessment +
                ", student=" + student +
                ", requestTime=" + requestTime +
                ", status='" + status + '\'' +
                ", owner='" + owner + '\'' +
                ", commitID='" + commitID + '\'' +
                ", expiresTime=" + expiresTime +
                ", applyPenalty=" + applyPenalty +
                ", logText='" + logText + '\'' +
                '}';
    }
}
