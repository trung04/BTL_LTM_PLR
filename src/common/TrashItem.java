/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common;

import java.io.Serializable;
import javafx.fxml.FXML;

/**
 *
 * @author Dung
 */
public class TrashItem implements Serializable {

    private int id;
    private String name;
    private String type;
    private String imageUrl;
    //trạng thái này  để biết đã được phân loại hay chưa được phân loại
    private String status;
    //người chơi nào đã phân loại
    private int userId;

    public TrashItem(String name, String type, String imageUrl) {
        this.name = name;
        this.type = type;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
}
