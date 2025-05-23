package api.model;

import java.time.LocalDate;

public class Memo {
    public int id;
    public int userId;
    public String content;
    public int Index;
    public LocalDate createdAt;

    public Memo(int id, int userId, String content, int Index, LocalDate createdAt) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.Index = Index;
        this.createdAt = createdAt;
    }
}