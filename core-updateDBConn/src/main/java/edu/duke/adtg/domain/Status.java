package edu.duke.adtg.domain;


public enum Status {
    COMPLETED, INIT, DELIVER, ERROR;

    public static Status fromString(String status) {
        return Status.valueOf(status.toUpperCase());
    }
}


