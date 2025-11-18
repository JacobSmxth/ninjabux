package com.example.NinjaBux.dto;

import com.example.NinjaBux.domain.NinjaLoginLog;

import java.time.LocalDateTime;

public class NinjaLoginLogDTO {
    private Long id;
    private NinjaBasicDTO ninja;
    private LocalDateTime loginTime;
    private String ipAddress;
    private String userAgent;
    private Boolean successful;

    public NinjaLoginLogDTO() {}

    public NinjaLoginLogDTO(NinjaLoginLog log) {
        this.id = log.getId();
        this.ninja = new NinjaBasicDTO(log.getNinja());
        this.loginTime = log.getLoginTime();
        this.ipAddress = log.getIpAddress();
        this.userAgent = log.getUserAgent();
        this.successful = log.getSuccessful();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NinjaBasicDTO getNinja() {
        return ninja;
    }

    public void setNinja(NinjaBasicDTO ninja) {
        this.ninja = ninja;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

    public static class NinjaBasicDTO {
        private Long id;
        private String firstName;
        private String lastName;
        private String username;

        public NinjaBasicDTO() {}

        public NinjaBasicDTO(com.example.NinjaBux.domain.Ninja ninja) {
            this.id = ninja.getId();
            this.firstName = ninja.getFirstName();
            this.lastName = ninja.getLastName();
            this.username = ninja.getUsername();
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}
