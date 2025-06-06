package api.model;

import java.time.LocalDate;

public class Todo {
    private int id;
    private int userId;
    private String title;
    private String startDate;
    private String endDate;
    private String createdAt;
    private String updatedAt;

    public Todo() {}

    public Todo(int id, int userId, String title, String startDate, String endDate,
                String createdAt, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public void setStartDate(LocalDate date) {
        this.startDate = date.toString();
    }
    public void setEndDate(LocalDate date) {
        this.endDate = date.toString();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
