package client;

import client.GUI.GameUIController;
import client.GUI.ResultMatchController;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Loading GameUI.fxml...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/GUI/ResultMatcUI.fxml"));
            Parent root = loader.load();

            // Lấy controller
            ResultMatchController gameUIController = loader.getController();
            gameUIController.setResultMatch("hòa 100 100");
//            gameUIController.setClient(client);
            if (gameUIController == null) {
                System.err.println("Controller is null for GameUI.fxml");
                return;
            }

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Phân Loại Rác - Game");
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args); // Khởi động JavaFX Application
    }
}
