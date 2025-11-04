package common;

import java.io.Serializable;
import java.sql.Timestamp;

public class Match implements Serializable {

    private int id;
    private int player1Id;
    private int player2Id;
    private Integer winnerId; // Có thể NULL
    private int player1Score;
    private int player2Score;
    private String player1Name;
    private String player2Name;
    private Timestamp time; // Thêm thuộc tính thời gian
    private String endReason; // Thêm thuộc tính endReason

    public Match(int id, int player1Id, int player2Id, int player1Score, int player2Score, Integer winnerId, String player1Name, String player2Name, Timestamp time, String endReason) {
        this.id = id;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.player1Score = player1Score;
        this.player2Score = player2Score;
        this.winnerId = winnerId;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.time = time;
        this.endReason = endReason;
    }

    // Getters và Setters
    public Timestamp getTime() {
        return time;
    }

    public int getPlayer1Score() {
        return player1Score;
    }

    public int getPlayer2Score() {
        return player2Score;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPlayer1Id(int player1Id) {
        this.player1Id = player1Id;
    }

    public void setPlayer2Id(int player2Id) {
        this.player2Id = player2Id;
    }

    public void setWinnerId(Integer winnerId) {
        this.winnerId = winnerId;
    }

    public void setPlayer1Score(int player1Score) {
        this.player1Score = player1Score;
    }

    public void setPlayer2Score(int player2Score) {
        this.player2Score = player2Score;
    }

    public void setPlayer1Name(String player1Name) {
        this.player1Name = player1Name;
    }

    public void setPlayer2Name(String player2Name) {
        this.player2Name = player2Name;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public void setEndReason(String endReason) {
        this.endReason = endReason;
    }

    public int getId() {
        return id;
    }

    public int getPlayer1Id() {
        return player1Id;
    }

    public int getPlayer2Id() {
        return player2Id;
    }

    public Integer getWinnerId() {
        return winnerId;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public String getEndReason() {
        return endReason;
    }

    public String getResult(int userId) {
        if (endReason != null && endReason.equals("player_quit")) {
            if (winnerId == userId) {
                return "Đối thủ đã thoát";
            } else {
                return "Bạn đã thoát";
            }
        } else {
            if (winnerId == null || winnerId == 0) {
                return "Thắng";
            } else if (winnerId == userId) {
                return "Thắng";
            } else {
                return "Thua";
            }
        }
    }

    public String getOpponentName(int userId) {
        return (player1Id == userId) ? player2Name : player1Name;
    }

    public String getScoreMatch(int userId) {
        if (player1Id == userId) {
            return this.player1Score + "-" + this.player2Score;
        } else {
            return this.player2Score + "-" + this.player1Score;
        }
    }
}
