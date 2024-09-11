package edu.duke.adtg.domain;

public class Prerequisite {
    private Assessment assn;
    private Assessment preAssn;

    // Constructors
    public Prerequisite() {}

    public Prerequisite(Assessment assn, Assessment preAssn) {
        this.assn = assn;
        this.preAssn = preAssn;
    }

    // Getters and Setters
    public Assessment getAssn() {
        return assn;
    }

    public void setAssn(Assessment assn) {
        this.assn = assn;
    }

    public Assessment getPreAssn() {
        return preAssn;
    }

    public void setPreAssn(Assessment preAssn) {
        this.preAssn = preAssn;
    }
}
