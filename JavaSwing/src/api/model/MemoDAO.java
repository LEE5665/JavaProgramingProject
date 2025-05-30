package api.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import api.DB;
import gui.main.FileSystemImageHandler;

public class MemoDAO {
	public int insertMemo(Memo memo) {
		String sql = "INSERT INTO memo (user_id, content, fix_flag, seq, update_at, created_at) VALUES (?, ?, ?, ?, ?, ?)";
		String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		try (Connection conn = DB.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, memo.getUserId());
			ps.setString(2, memo.getContent());
			ps.setBoolean(3, memo.isFixFlag());
			ps.setInt(4, memo.getSeq());
			ps.setString(5, now);
			ps.setString(6, now);
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next())
					return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public List<Memo> selectMemosByUser(int userId) throws SQLException {
		String sql = "SELECT * FROM memo WHERE user_id=? ORDER BY fix_flag DESC, update_at DESC, seq ASC, id ASC";
		List<Memo> list = new ArrayList<>();
		try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(new Memo(rs.getInt("id"), rs.getInt("user_id"), rs.getString("content"),
							rs.getBoolean("fix_flag"), rs.getInt("seq"), rs.getString("update_at"),
							rs.getString("created_at")));
				}
			}
		}
		return list;
	}

	public boolean updateMemo(Memo memo) throws Exception {
		String selectSql = "SELECT content FROM memo WHERE id=?";
		String updateSql = "UPDATE memo SET content=?, fix_flag=?, seq=?, update_at=? WHERE id=?";
		String oldHtml = "";

		try (Connection conn = DB.getConnection()) {
			conn.setAutoCommit(false);

			try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
				ps.setInt(1, memo.getId());
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						oldHtml = rs.getString("content");
				}
			}

			FileSystemImageHandler imgHandler = new FileSystemImageHandler();
			imgHandler.deleteImagesInMarkdown(oldHtml);

			try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
				ps.setString(1, memo.getContent());
				ps.setBoolean(2, memo.isFixFlag());
				ps.setInt(3, memo.getSeq());
				String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
				ps.setString(4, now);
				ps.setInt(5, memo.getId());
				boolean updated = ps.executeUpdate() > 0;
				conn.commit();
				return updated;
			}
		}
	}

	public boolean deleteMemo(int memoId) throws Exception {
		String selectSql = "SELECT content FROM memo WHERE id = ?";
		String deleteSql = "DELETE FROM memo WHERE id = ?";
		String html = "";

		try (Connection conn = DB.getConnection()) {
			conn.setAutoCommit(false);

			try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
				ps.setInt(1, memoId);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						html = rs.getString("content");
				}
			}

			FileSystemImageHandler imgHandler = new FileSystemImageHandler();
			imgHandler.deleteImagesInMarkdown(html);

			try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
				ps.setInt(1, memoId);
				boolean deleted = ps.executeUpdate() > 0;
				conn.commit();
				return deleted;
			}
		}
	}
}
