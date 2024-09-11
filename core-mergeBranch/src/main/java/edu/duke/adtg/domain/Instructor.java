package edu.duke.adtg.domain;
import java.util.ArrayList;
import java.util.List;

public class Instructor extends User{

    public Instructor(){}
    
    public Instructor(String netId, String name, String email) {
        super(netId, name, email);
    }

    public Instructor(String netId){
        super(netId);
    }

    public String getEmail() {
        return super.getEmail();
    }
    public String getName() {
        return super.getName();
    }
    public void setEmail(String email) {
        super.setEmail(email);
    }
    public void setName(String name) {
        super.setName(name);
    }
}

