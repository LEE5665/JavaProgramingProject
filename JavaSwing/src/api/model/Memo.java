package api.model;

public class Memo {
    private int id;
    private int userId;
    private String content;
    private boolean fixFlag;
    private int seq;
    private String updateAt;
    private String createdAt;

    public Memo() {}

    public Memo(int id, int userId, String content, boolean fixFlag, int seq, String updateAt, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.fixFlag = fixFlag;
        this.seq = seq;
        this.updateAt = updateAt;
        this.createdAt = createdAt;
    }

    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isFixFlag() { return fixFlag; }
    public void setFixFlag(boolean fixFlag) { this.fixFlag = fixFlag; }

    public int getSeq() { return seq; }
    public void setSeq(int seq) { this.seq = seq; }

    public String getUpdateAt() { return updateAt; }
    public void setUpdateAt(String updateAt) { this.updateAt = updateAt; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
