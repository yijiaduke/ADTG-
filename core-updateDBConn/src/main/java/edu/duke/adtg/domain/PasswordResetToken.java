package edu.duke.adtg.domain;

import java.time.LocalDateTime;

public class PasswordResetToken {
    private String netId;
    private String token;
    private LocalDateTime expirationTime;
    private char used;

    public PasswordResetToken() {
    }

    public PasswordResetToken(String netId, String token, LocalDateTime expirationTime, char used) {
        this.netId = netId;
        this.token = token;
        this.expirationTime = expirationTime;
        this.used = used;
    }

    public String getNetId() {
        return netId;
    }

    public void setNetId(String netId) {
        this.netId = netId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    public char getUsed() {
        return used;
    }

    public void setUsed(char used) {
        this.used = used;
    }
}
