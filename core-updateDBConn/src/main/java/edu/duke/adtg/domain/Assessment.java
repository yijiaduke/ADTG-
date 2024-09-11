package edu.duke.adtg.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Assessment {
    

    private String assn;
    
    private String title;

    private LocalDateTime startDate;

    private LocalDateTime dueDate;

//    private LocalDateTime hardDeadline;


    private BigDecimal maxScore;

    private BigDecimal passScore;

    private boolean extraCredit;

    private String testCmd;

    private List<Assessment> prereq;

    private Category category;

    private Course course;

    private Integer tokenReq;


    //Constructors
    
    public Assessment(){}


    // Full constructor
    public Assessment(Course course, String assn, String title, LocalDateTime startDate, LocalDateTime dueDate, Integer tokenReq, Category category,BigDecimal maxScore,BigDecimal passScore, boolean extraCredit, String testCmd, List<Assessment> prereq) {
        this.course = course;
        this.assn = assn;
        this.title = title;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.tokenReq = tokenReq;
        this.category = category;
        this.maxScore = maxScore;
        this.passScore = passScore;
        this.extraCredit = extraCredit;
        this.testCmd = testCmd;
        this.prereq = prereq;
    }


    public Assessment(Course course, String assn){
            this.course = course;
            this.assn = assn;
    }
    
    public Assessment(Course course, String assn, String title){
        this.course = course;
        this.assn = assn;
        this.title = title;
}

    // Based on Assessment table
    public Assessment(Course course, String assn, String title, LocalDateTime startDate, LocalDateTime dueDate, Integer tokenReq, Category category, BigDecimal maxScore, BigDecimal passScore, boolean extraCredit, String testCmd ){
        this(course, assn, title, startDate, dueDate, tokenReq, category, maxScore, passScore, extraCredit, testCmd, null);
    }


    // Getters and setters
    public String getTitle() {
        return title;
    }
    public LocalDateTime getStartDate() {
        return startDate;
    }
    public LocalDateTime getDueDate() {
        return dueDate;
    }
//    public LocalDateTime getHardDeadline() {
//        return hardDeadline;
//    }
    public BigDecimal getMaxScore() {
        return maxScore;
    }
    public BigDecimal getPassScore() {
        return passScore;
    }
    public boolean isExtraCredit() {
        return extraCredit;
    }
    public String getTestCmd() {
        return testCmd;
    }
    public List<Assessment> getPrereq() {
        return prereq;
    }
    public Category getCategory() {
        return category;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
//    public void setHardDeadline(LocalDateTime hardDeadline) {
//        this.hardDeadline = hardDeadline;
//    }
    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }
    public void setPassScore(BigDecimal passScore) {
        this.passScore = passScore;
    }
    public void setExtraCredit(boolean extraCredit) {
        this.extraCredit = extraCredit;
    }
    public void setTestCmd(String testCmd) {
        this.testCmd = testCmd;
    }
    public void setPrereq(List<Assessment> prereq) {
        this.prereq = prereq;
    }
    public void setCategory(Category category) {
        this.category = category;
    }
    public String getSubject() {
        return course.getSubject();
    }
    public Integer getNumber() {
        return course.getNumber();
    }
    public String getAssn() {
        return assn;
    }
    public void setAssn(String assn) {
        this.assn = assn;
    }
    public Course getCourse() {
        return course;
    }
    public void setCourse(Course course){
        this.course = course;
    }
    public Integer getTokenReq() {
        return tokenReq;
    }
    public void setTokenReq(Integer tokenReq) {
        this.tokenReq = tokenReq;
    }

    public String getPenaltyFormula() {
        return category.getPenaltyFormula();
    }
    public String toString(){
        return course.getSubject() + " " + course.getNumber() + " " + assn;
    }
}


