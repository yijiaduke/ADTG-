package edu.duke.adtg.domain;

import java.util.ArrayList;
import java.util.List;

public class Student extends User{

    private List<Grade> grades;
    
    public Student() {
        super();
    }
    
    public Student(String netId){
        super(netId);
    }
    
    public Student(String netId, String name, String email,List<Grade> grades) {
        super(netId, name, email);
        this.grades = grades;
    }

    public Student(String netId, String name, String email) {
        this(netId, name, email, new ArrayList<Grade>());
    }

    public void setEmail(String email) {
        super.setEmail(email);
    }
    public void setName(String name) {
        super.setName(name);
    }
 
    public String getEmail() {
        return super.getEmail();
    }   
    public String getName() {
        return super.getName();
    }
    public List<Grade> getGrades() {
        return grades;
    }
}
