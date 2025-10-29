/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common;

import java.sql.Timestamp;

/**
 *
 * @author Dung
 */
public class MatchDetail {
    private int id;
    private int matchId;
    private int playerId;
    private int totalScore;
    private int correct;
    private int wrong;
    private int miss;
    private int combo;
    private Timestamp time;

    public MatchDetail() {
    }

    public MatchDetail(int id, int matchId, int playerId, int totalScore, int correct, int wrong, int miss, int combo, Timestamp time) {
        this.id = id;
        this.matchId = matchId;
        this.playerId = playerId;
        this.totalScore = totalScore;
        this.correct = correct;
        this.wrong = wrong;
        this.miss = miss;
        this.combo = combo;
        this.time = time;
    }
     

}
