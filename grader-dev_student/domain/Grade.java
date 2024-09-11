package edu.duke.adtg.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
public class Grade {
    private Assessment assessment;  // (c_subject, c_number, assn)
    private Student student;        // (netid)
    private LocalDateTime gradeTime;
    private BigDecimal assignmentGrade; // Without penalty
    private BigDecimal penalty;
    private BigDecimal finalGrade;
    private String logText;

    public Grade(Assessment assessment, Student student, LocalDateTime gradeTime, 
                 BigDecimal assignmentGrade, BigDecimal penalty, BigDecimal finalGrade, String logText) {
        this.assessment = assessment;
        this.student = student;
        this.gradeTime = gradeTime;
        this.assignmentGrade = assignmentGrade;
        this.penalty = penalty;
        this.finalGrade = finalGrade;
        this.logText = logText;
    }

    // Getters
    public Assessment getAssessment() {
        return assessment;
    }

    public Student getStudent() {
        return student;
    }

    public LocalDateTime getGradeTime() {
        return gradeTime;
    }

    public BigDecimal getAssignmentGrade() {
        return assignmentGrade;
    }

    public BigDecimal getPenalty() {
        return penalty;
    }

    public BigDecimal getFinalGrade() {
        return finalGrade;
    }

    public String getLogText() {
        return logText;
    }

    // Setters
    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public void setGradeTime(LocalDateTime gradeTime) {
        this.gradeTime = gradeTime;
    }

    public void setAssignmentGrade(BigDecimal assignmentGrade) {
        this.assignmentGrade = assignmentGrade;
    }

    public void setPenalty(BigDecimal penalty) {
        this.penalty = penalty;
    }

    public void setFinalGrade(BigDecimal finalGrade) {
        this.finalGrade = finalGrade;
    }

    public void setLogText(String logText) {
        this.logText = logText;
    }
}
