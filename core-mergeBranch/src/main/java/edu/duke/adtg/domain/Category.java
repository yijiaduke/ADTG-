package edu.duke.adtg.domain;

public class Category {
    private String name;
    private String penaltyFormula;
    // Constructor
    public Category(String name, String penaltyFormula) {
        this.name = name;
        this.penaltyFormula = penaltyFormula;
    }

    //Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getPenaltyFormula() {
        return penaltyFormula;
    }
    public void setPenaltyFormula(String penaltyFormula) {
        this.penaltyFormula = penaltyFormula;
    }
}
