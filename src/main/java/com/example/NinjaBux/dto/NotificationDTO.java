package com.example.NinjaBux.dto;

import java.time.LocalDateTime;

public class NotificationDTO {
    private String type;
    private String title;
    private String message;
    private Long ninjaId;
    private LocalDateTime timestamp;
    private Object data; // additional stuff

    public NotificationDTO() {
        this.timestamp = LocalDateTime.now();
    }

    public NotificationDTO(String type, String title, String message, Long ninjaId) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.ninjaId = ninjaId;
        this.timestamp = LocalDateTime.now();
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public Long getNinjaId() {
        return ninjaId;
    }
    public void setNinjaId(Long ninjaId) {
        this.ninjaId = ninjaId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }
}

