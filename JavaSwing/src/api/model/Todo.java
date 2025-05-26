package api.model;

import java.time.LocalDate;

public class Todo {
    private int id;
    private int userId;
    private Integer parentId;
    private int depth;
    private String title;
    private String note;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean completed;
    private int seq;

    public Todo(int id, int userId, Integer parentId, int depth, String title, String note,
                LocalDate startDate, LocalDate endDate, boolean completed, int seq) {
        this.id = id;
        this.userId = userId;
        this.parentId = parentId;
        this.depth = depth;
        this.title = title;
        this.note = note;
        this.startDate = startDate;
        this.endDate = endDate;
        this.completed = completed;
        this.seq = seq;
    }

    public Todo(int userId, Integer parentId, int depth, String title, String note,
                LocalDate startDate, LocalDate endDate) {
        this(0, userId, parentId, depth, title, note, startDate, endDate, false, 0);
    }

    // --- Getter/Setter ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public Integer getParentId() { return parentId; }
    public int getDepth() { return depth; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public int getSeq() { return seq; }
    public void setSeq(int seq) { this.seq = seq; }
}
