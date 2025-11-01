/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package client.GUI;

import client.Client;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * FXML Controller class
 *
 * @author Dung
 */
public class ResultMatchController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private Label score;
    @FXML
    private Label result;
    private String resultMatch;
    private Client client;
    @FXML
    private Button btnReturn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
//        String[] word = resultMatch.trim().split("\\s+");
//        result.setText(String.valueOf(word[0]));
//        myScore.setText(String.valueOf(word[1]));
//        opponentScore.setText(String.valueOf(word[2]));

    }

    public void setResultMatch(String m) {
        String[] word = m.trim().split("\\s+");
        result.setText("YOU : " + String.valueOf(word[0]));
        score.setText(String.valueOf(word[1] + " " + String.valueOf(word[2])));
    }

    public void setClient(Client client) {
        this.client = client;

    }

    public void goToLobby() {
        client.showMainUI();
    }

}
