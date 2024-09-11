package edu.duke.adtg;

public class App {
    private static final Integer sleepTime = 10; // seconds 
    public static void main(String[] args) {
        TaskProcessor taskProcessor = new TaskProcessor(sleepTime);
        taskProcessor.startOneThread();
    }
}
