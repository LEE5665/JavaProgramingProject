package api.model;

import api.DB;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class UserDAO {

    
    public static boolean registerUser(String username, String plainPassword) {
        String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hashed);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    
    public static int login(String username, String plainPassword) {
        String sql = "SELECT id, password FROM users WHERE username = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (BCrypt.checkpw(plainPassword, storedHash)) {
                        return rs.getInt("id"); 
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; 
    }
}
