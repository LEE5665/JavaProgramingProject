package api.model;

import java.time.LocalDateTime;

public class Memo {
	public int id;
	public int userId;
	public String content;
	public boolean fixFlag;
	public int orderIndex;
	public LocalDateTime updateAt;
	public LocalDateTime createdAt;

	public Memo(int id, int userId, String content, boolean fixFlag, int orderIndex, LocalDateTime updateAt,
			LocalDateTime createdAt) {
		this.id = id;
		this.userId = userId;
		this.content = content;
		this.fixFlag = fixFlag;
		this.orderIndex = orderIndex;
		this.updateAt = updateAt;
		this.createdAt = createdAt;
	}

	public int getId() {
		return id;
	}

	public int getUserId() {
		return userId;
	}

	public String getContent() {
		return content;
	}

	public boolean isFixFlag() {
		return fixFlag;
	}

	public int getOrderIndex() {
		return orderIndex;
	}

	public LocalDateTime getUpdateAt() {
		return updateAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setFixFlag(boolean fixFlag) {
		this.fixFlag = fixFlag;
	}

	public void setOrderIndex(int orderIndex) {
		this.orderIndex = orderIndex;
	}

	public void setUpdateAt(LocalDateTime updateAt) {
		this.updateAt = updateAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}