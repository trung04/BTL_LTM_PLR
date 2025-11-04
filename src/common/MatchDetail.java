/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 *
 * @author Dung
 */
public class MatchDetail implements Serializable {

    private int id;
    private int matchId;
    private String name;
    private String type;
    private String result;
    private Timestamp time;

    public MatchDetail() {
    }

    public MatchDetail(int id, int matchId, String name, String type, String result, Timestamp time) {
        this.id = id;
        this.matchId = matchId;
        this.name = name;
        this.type = type;
        this.result = result;
        this.time = time;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public int getMatchId() {
        return matchId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Timestamp getTime() {
        return time;
    }

}
