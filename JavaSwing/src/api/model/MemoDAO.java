package api.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import api.DB;

public class MemoDAO {

	// 메모 추가
	public static int insertMemo(Memo memo) throws SQLException {
		String sql = "INSERT INTO memo (user_id, content, fix_flag, seq, update_at, created_at) VALUES (?, ?, ?, ?, ?, ?)";
		try (Connection conn = DB.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, memo.getUserId());
			ps.setString(2, memo.getContent());
			ps.setBoolean(3, memo.isFixFlag());
			ps.setInt(4, memo.getSeq());
			ps.setString(5, memo.getUpdateAt());
			ps.setString(6, memo.getCreatedAt());
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next())
					return rs.getInt(1);
			}
		}
		return -1;
	}

	// 메모 목록 리스트로 반환
	public static List<Memo> selectMemosByUser(int userId) throws SQLException {
		String sql = "SELECT * FROM memo WHERE user_id=? ORDER BY fix_flag DESC, update_at DESC, id DESC";
		List<Memo> result = new ArrayList<>();
		try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Memo memo = new Memo(rs.getInt("id"), rs.getInt("user_id"), rs.getString("content"),
							rs.getBoolean("fix_flag"), rs.getInt("seq"), rs.getString("update_at"),
							rs.getString("created_at"));
					result.add(memo);
				}
			}
		}
		return result;
	}

	// 메모 내용 수정
	public static boolean updateMemo(Memo memo) throws SQLException {
		String sql = "UPDATE memo SET content = ?, fix_flag = ?, seq = ?, update_at = datetime('now','localtime') WHERE id = ?";

		try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, memo.getContent());
			ps.setBoolean(2, memo.isFixFlag());
			ps.setInt(3, memo.getSeq());
			ps.setInt(4, memo.getId());
			return ps.executeUpdate() > 0;
		}
	}

	// 메모 제거
	public static boolean deleteMemo(int memoId) throws SQLException {
		String sql = "DELETE FROM memo WHERE id=?";
		try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, memoId);
			return ps.executeUpdate() > 0;
		}
	}
}
