package api.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Todo {
	private int id;
	private int userId;
	private Integer parentId;
	private int depth;
	private String title;
	private String note;
	private LocalDate date;
	private LocalTime startTime;
	private LocalTime endTime;
	private boolean completed;
	private int seq;

	public Todo(int id, int userId, Integer parentId, int depth, String title, String note, LocalDate date,
			LocalTime startTime, LocalTime endTime, boolean completed, int seq) {
		this.id = id;
		this.userId = userId;
		this.parentId = parentId;
		this.depth = depth;
		this.title = title;
		this.note = note;
		this.date = date;
		this.startTime = startTime;
		this.endTime = endTime;
		this.completed = completed;
		this.seq = seq;
	}

	public Todo(int userId, Integer parentId, int depth, String title, String note, LocalDate date, LocalTime startTime,
			LocalTime endTime) {
		this(0, userId, parentId, depth, title, note, date, startTime, endTime, false, 0);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public Integer getParentId() {
		return parentId;
	}

	public int getDepth() {
		return depth;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String t) {
		this.title = t;
	}

	public String getNote() {
		return note;
	}

	public LocalDate getDate() {
		return date;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean c) {
		this.completed = c;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int s) {
		this.seq = s;
	}
}
