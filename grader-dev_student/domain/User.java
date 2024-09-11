package edu.duke.adtg.domain;

import java.util.Set;

public class User {

    private String netId;

    private String name;

    private String email;

    private String passwordHash;
    
    private Set<String> roles;

    public User(String netId){
        this.netId = netId;
    }
    public User(String netId, String name, String email) {
        this.netId = netId;
        this.name = name;
        this.email = email;
    }
    public User(String netId, String name, String email, String passwordHash) {
        this.netId = netId;
        this.name = name;
        this.email = email;
    }

    public User(String netId, String name, String email, String passwordHash, Set<String> roles) {
        this.netId = netId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = roles;
    }
    
    public String getNetId() {
        return netId;
    }

    public void setNetId(String netId) {
        this.netId = netId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
