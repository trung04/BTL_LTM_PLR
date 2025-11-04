package server;

import common.Match;
import common.User;
import common.MatchDetails;
import common.TrashItem;
import common.MatchDetail;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;

public class DatabaseManager {

    private static final String URL = "jdbc:mysql://localhost:3306/blt_ltm";
    private static final String USER = "root"; // Thay bằng user MySQL của bạn
    private static final String PASSWORD = ""; // Thay bằng password MySQL của bạn

    private Connection conn;

    public DatabaseManager() throws SQLException {
        conn = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Phương thức đăng nhập
    public Pair<User, Boolean> authenticate(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, username);
        stmt.setString(2, password); // Trong thực tế, nên mã hóa mật khẩu trước khi so sánh
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            User authenticatedUser = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getInt("points"),
                    rs.getString("status"));
            Boolean isOffline = rs.getString("status").equals("offline");
            return new Pair<>(authenticatedUser, isOffline);

        }
        return new Pair<>(null, null);
    }

    // Cập nhật trạng thái người dùng
    public void updateUserStatus(int userId, String status) throws SQLException {
        String query = "UPDATE users SET status = ? WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, status);
        stmt.setInt(2, userId);
        stmt.executeUpdate();
    }

    // Lấy danh sách người chơi
    public List<User> getUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            users.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getInt("points"),
                    rs.getString("status")));
        }
        return users;
    }

    // Lưu lịch sử đấu
    public int saveMatch(int player1Id, int player2Id, int winnerId) throws SQLException {
        String query = "INSERT INTO matches (player1_id, player2_id, winner_id) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, player1Id);
        stmt.setInt(2, player2Id);
        if (winnerId > 0) {
            stmt.setInt(3, winnerId);
        } else {
            stmt.setNull(3, Types.INTEGER);
        }
        stmt.executeUpdate();
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return -1;
    }

    // Cập nhật người chiến thắng vào lịch sử đấu
    public void updateMatchWinner(int matchId, int player1_score, int player2_score, int winnerId, String endReason) throws SQLException {
        String query = "UPDATE matches SET player1_score = ?,player2_score=?,winner_id = ?, end_reason = ? WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, player1_score);
        stmt.setInt(2, player2_score);
        stmt.setInt(3, winnerId);
        stmt.setString(4, endReason);
        stmt.setInt(5, matchId);
        stmt.executeUpdate();
    }

    // Cập nhật điểm số
    public void updateUserPoints(int userId, int points) throws SQLException {
        String query = "UPDATE users SET points = points + ? WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, points);
        stmt.setInt(2, userId);
        stmt.executeUpdate();
    }

    //Phương thức lưu chi tiết trận đấu phân loại rác
    // Lấy lịch sử đấu theo match ID
    public List<MatchDetails> getMatchDetails(int matchId) throws SQLException {
        List<MatchDetails> detailsList = new ArrayList<>();
        String query = "SELECT *, timestamp AS time FROM match_details WHERE match_id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, matchId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            detailsList.add(new MatchDetails(
                    rs.getInt("id"),
                    rs.getInt("match_id"),
                    rs.getInt("round"),
                    rs.getInt("shooter_id"),
                    rs.getInt("goalkeeper_id"),
                    rs.getString("shooter_direction"),
                    rs.getString("goalkeeper_direction"),
                    rs.getString("result"),
                    rs.getTimestamp("time")));
        }
        if (detailsList.isEmpty()) {
            // Kiểm tra lý do kết thúc trận đấu
            String matchQuery = "SELECT winner_id, player1_id, player2_id, end_reason FROM matches WHERE id = ?";
            PreparedStatement matchStmt = conn.prepareStatement(matchQuery);
            matchStmt.setInt(1, matchId);
            ResultSet matchRs = matchStmt.executeQuery();
            if (matchRs.next()) {
                String endReason = matchRs.getString("end_reason");
                int winnerId = matchRs.getInt("winner_id");
                int player1Id = matchRs.getInt("player1_id");
                int player2Id = matchRs.getInt("player2_id");
                if ("player_quit".equals(endReason)) {
                    // Tạo MatchDetails để hiển thị lý do
                    int quitterId = (winnerId == player1Id) ? player2Id : player1Id;
                    detailsList.add(new MatchDetails(
                            0, // id
                            matchId,
                            0, // round
                            quitterId,
                            0, // goalkeeperId
                            null,
                            null,
                            "Player quit",
                            null));
                }
            }
        }
        return detailsList;
    }

    public List<MatchDetail> getMatchDetail(int matchId) throws SQLException {
        List<MatchDetail> detailsList = new ArrayList<>();
        try {
            String query = """
        SELECT 
            m.id as id,
            m.match_id as matchId,
            t.name as name,
            t.type as type,
            m.result as result,
            m.created_at as time
        FROM math_detail_test AS m
        INNER JOIN trash_items AS t
            ON m.trash_item_id = t.id
        WHERE m.match_id = ?;
        """;

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, matchId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                detailsList.add(new MatchDetail(
                        rs.getInt("id"),
                        rs.getInt("matchId"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getString("result"),
                        rs.getTimestamp("time")));
            }

        } catch (Exception e) {
            System.out.println("Lỗi try vấn sql");
        }

        return detailsList;
    }

    // Các phương thức khác như lấy lịch sử đấu, bảng xếp hạng, v.v.
    public List<User> getLeaderboard() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users ORDER BY points DESC";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            users.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getInt("points"),
                    rs.getString("status")));
        }
        return users;
    }

    // Lấy lịch sử đấu chi tiết theo UserID
    public List<MatchDetail> getUserMatchHistory(int userId) throws SQLException {
        List<MatchDetail> history = new ArrayList<>();
        try {
            String query = """
        SELECT 
            m.id as id,
            m.match_id as matchId,
            t.name as name,
            t.type as type,
            m.result as result,
            m.created_at as time
        FROM math_detail_test AS m
        INNER JOIN trash_items AS t
            ON m.trash_item_id = t.id
        WHERE m.user_id = ?;
        """;
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                history.add(new MatchDetail(
                        rs.getInt("id"),
                        rs.getInt("matchId"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getString("result"),
                        rs.getTimestamp("time")));
            }

        } catch (Exception e) {
            System.out.println("Lỗi truy vấn lấy chi tiết trận đấu theo userId");
        }

        return history;
    }

    // Lấy lịch sử đấu theo UserID
    public List<Match> getUserMatches(int userId) throws SQLException {
        List<Match> matches = new ArrayList<>();
        String query = "SELECT m.*, m.timestamp AS time, u1.username AS player1_name, u2.username AS player2_name FROM matches m "
                + "JOIN users u1 ON m.player1_id = u1.id "
                + "JOIN users u2 ON m.player2_id = u2.id "
                + "WHERE m.player1_id = ? OR m.player2_id = ? ORDER BY m.id DESC";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, userId);
        stmt.setInt(2, userId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            matches.add(new Match(
                    rs.getInt("id"),
                    rs.getInt("player1_id"),
                    rs.getInt("player2_id"),
                    rs.getInt("player1_score"),
                    rs.getInt("player2_score"),
                    rs.getObject("winner_id") != null ? rs.getInt("winner_id") : null,
                    rs.getString("player1_name"),
                    rs.getString("player2_name"),
                    rs.getTimestamp("time"),
                    rs.getString("end_reason") // Thêm end_reason
            ));
        }
        return matches;
    }

    //bài làm của nguyễn trung
    //Lưu chi tiết từng item 1
    // Lưu lịch sử đấu
    public int saveDetailResult(int match_id, int user_id, int trash_item_id, String result) throws SQLException {
        String sql = "INSERT INTO math_detail_test(match_id, user_id, trash_item_id, result, created_at) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, match_id);
            stmt.setInt(2, user_id);
            stmt.setInt(3, trash_item_id);
            stmt.setString(4, result);
            int rows = stmt.executeUpdate();
            return rows > 0 ? 1 : 0;
        } catch (SQLException ex) {
            System.err.println("SQL Error: " + ex.getMessage());
            return -1;
        }
    }

    //lay chi ds rac thai
    public List<TrashItem> getTrashItems() throws SQLException {
        List<TrashItem> trash_items = new ArrayList<>();
        String query = "select * from trash_items";
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            trash_items.add(new TrashItem(rs.getInt("id"), rs.getString("name"), rs.getString("type"), rs.getString("image_url")));
        }
        return trash_items;
    }

}
