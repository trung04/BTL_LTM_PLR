/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common;

/**
 *
 * @author Dung
 */
public class TrashBin {

    private String type;
    private String imageUrl;
    private double x;
    private double y;

    public TrashBin(String type, String imageUrl, double x, double y) {
        this.type = type;
        this.imageUrl = imageUrl;
        this.x = x;
        this.y = y;
    }

    public String getType() {
        return type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "TrashBin{"
                + "type='" + type + '\''
                + ", x=" + x
                + ", y=" + y
                + '}';
    }
}
