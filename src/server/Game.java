/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import common.Message;
import common.TrashItem;
import java.sql.SQLException;
import java.util.Random;

/**
 *
 * @author Dung
 */
public class Game {

    // là người thách đấu
    private Server server;
    private ClientHandler player1;
    // là người bị thách đấu
    private ClientHandler player2;
    private DatabaseManager dbManager;
    private int matchId;
    private int score_player1 = 0;
    private int score_player2 = 0;
    private boolean player1Finished = false;
    private boolean player2Finished = false;
    //check xem đã gọi hàm cập nhập trận đấu chưa
    private boolean matchUpdated = false;
    String resultForPlayer1, resultForPlayer2;

    public Game() {
    }

    public Game(Server server, ClientHandler player1, ClientHandler player2, DatabaseManager dbManager) throws SQLException {
        this.server = server;
        this.player1 = player1;
        this.player2 = player2;
        this.dbManager = dbManager;
        this.matchId = dbManager.saveMatch(player1.getUser().getId(), player2.getUser().getId(), 0);
    }

    public void startMatch() {
        try {
            System.out.println("đã đến startMatch");
            // update ingame status for both player
            player1.getUser().setStatus("ingame");
            player2.getUser().setStatus("ingame");
            player1.sendMessage(new Message("match_start", null));
            player2.sendMessage(new Message("match_start", null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void updatePoint(Message m) throws SQLException {
        TrashItem trash = (TrashItem) m.getContent();
        String userId = String.valueOf(trash.getUserId());
        ClientHandler player = server.getClientById(Integer.parseInt(userId));
        try {
            dbManager.saveDetailResult(matchId, trash.getUserId(), trash.getId(), trash.getStatus());
        } catch (Exception e) {
            System.out.println("Không thể lưu lịch chi tiết trận đấu");
        }
        if (player.getUser().getId() == player1.getUser().getId()) {
            score_player1 += 10;
        } else if (player.getUser().getId() == player2.getUser().getId()) {
            score_player2 += 10;
        }
        String mess1 = score_player1 + " " + score_player2;
        String mess2 = score_player2 + " " + score_player1;
        player1.sendMessage(new Message("update_point_response", mess1));
        player2.sendMessage(new Message("update_point_response", mess2));
    }

    //xem cả 2 đã hoàn thành trận đấu hay chưa
    public synchronized void playerFinished(ClientHandler handler) throws SQLException {
        if (handler.getUser().getId() == player1.getUser().getId()) {
            player1Finished = true;
        } else if (handler.getUser().getId() == player2.getUser().getId()) {
            player2Finished = true;
        }
        //  cả 2 đều xong thì cập nhập người chiến thắng
        if (player1Finished == true && player2Finished == true && !matchUpdated == true) {
            matchUpdated = true;
            updateMatchWinner();
        } else {
            System.out.println("cả 2 chưa sẵn sàng");
        }
    }

    //cập nhập người chiến thắng
    public synchronized void updateMatchWinner() throws SQLException {

        if (score_player1 > score_player2) {
            resultForPlayer1 = "thắng " + score_player1 + " " + score_player2;
            resultForPlayer2 = "thua " + score_player2 + " " + score_player1;
            dbManager.updateMatchWinner(matchId, score_player1, score_player2, player1.getUser().getId(), "normal");
        } else if (score_player1 < score_player2) {
            dbManager.updateMatchWinner(matchId, score_player1, score_player2, player2.getUser().getId(), "normal");
            resultForPlayer1 = "thua " + score_player1 + " " + score_player2;
            resultForPlayer2 = "thắng " + score_player2 + " " + score_player1;
        } else {
            resultForPlayer1 = "hòa " + score_player1 + " " + score_player2;
            resultForPlayer2 = "hòa " + score_player2 + " " + score_player1;
            dbManager.updateMatchWinner(matchId, score_player1, score_player2, 0, "normal");
        }
        player1.getUser().setStatus("online");
        player2.getUser().setStatus("online");
        dbManager.updateUserStatus(player1.getUser().getId(), "online");
        dbManager.updateUserStatus(player2.getUser().getId(), "online");
        server.broadcast(new Message("status_update", player1.getUser().getUsername() + " is online"));
        server.broadcast(new Message("status_update", player2.getUser().getUsername() + " is online"));
        //gửi kết quả cho 2 người chơi
        player1.sendMessage(new Message("finish_match_response", resultForPlayer1));
        player2.sendMessage(new Message("finish_match_response", resultForPlayer2));
        //cập nhập trạng thái cho 2 người chơi

    }

}
