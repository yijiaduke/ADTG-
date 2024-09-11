package edu.duke.adtg.domain;

public class Enrollment {
   
    private Section section; //(c_subject, c_number, sec_id)
    private String studentNetId;
    private Integer tokensAvail;

    public Enrollment(Section section, String studentNetId, Integer tokensAvail) {
        this.section = section;
        this.studentNetId = studentNetId;
        this.tokensAvail = tokensAvail;
    }

    // Getters and setters
    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public String getStudentNetId() {
        return studentNetId;
    }

    public void setStudentNetId(String studentNetId) {
        this.studentNetId = studentNetId;
    }

    public Integer getTokensAvail() {
        return tokensAvail;
    }

    public void setTokensAvail(Integer tokensAvail) {
        this.tokensAvail = tokensAvail;
    }
}
