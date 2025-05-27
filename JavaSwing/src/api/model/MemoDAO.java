package api.model;

import api.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemoDAO {
    
    public int insertMemo(Memo memo) throws SQLException {
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
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    
    public List<Memo> selectMemosByUser(int userId) throws SQLException {
    	String sql = "SELECT * FROM memo WHERE user_id=? ORDER BY fix_flag DESC, seq ASC, id ASC";
        List<Memo> result = new ArrayList<>();
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Memo memo = new Memo(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("content"),
                        rs.getBoolean("fix_flag"),
                        rs.getInt("seq"),
                        rs.getString("update_at"),
                        rs.getString("created_at")
                    );
                    result.add(memo);
                }
            }
        }
        return result;
    }

    
    public boolean updateMemo(Memo memo) throws SQLException {
        String sql = "UPDATE memo SET content=?, fix_flag=?, seq=?, update_at=? WHERE id=?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, memo.getContent());
            ps.setBoolean(2, memo.isFixFlag());
            ps.setInt(3, memo.getSeq());
            ps.setString(4, memo.getUpdateAt());
            ps.setInt(5, memo.getId());
            return ps.executeUpdate() > 0;
        }
    }

    
    public boolean deleteMemo(int memoId) throws SQLException {
        String sql = "DELETE FROM memo WHERE id=?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memoId);
            return ps.executeUpdate() > 0;
        }
    }
}
