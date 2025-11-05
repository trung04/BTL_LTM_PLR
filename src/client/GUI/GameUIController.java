package client.GUI;

import client.Client;
import common.Message;
import common.TrashBin;
import common.TrashItem;
import java.io.IOException;
import java.util.Random;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.Button;
import server.ClientHandler;
import server.DatabaseManager;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import static javafx.scene.input.KeyCode.DIGIT1;
import static javafx.scene.input.KeyCode.DIGIT2;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class GameUIController implements Initializable {

    @FXML
    private AnchorPane root;
    private int dem = 0;
    private Client client;
    private final Random random = new Random();
    @FXML
    private Label scoreLabel;
    @FXML
    private VBox scoreBox;
    @FXML
    private Button btnStart;
    @FXML
    private Button btnExit;
    @FXML
    private Label clockLabel;
    private int timeLeft = 30; // 15 giây

    @FXML
    private Label opponentScore;
    @FXML
    private Label myScore;
    @FXML
    private ImageView scoreBoard;
    private int miss = 0;

    // Biến lưu rác hiện đang được chọn
    private ImageView selectedTrash = null;
    private final List<TrashBin> bins = List.of(
            new TrashBin("compost", "@../../assets/compost_bin.png", 10, 366),
            new TrashBin("plastic", "@../../assets/plastic_bin.png", 150, 366),
            new TrashBin("metal", "@../../assets/metal_bin.png", 320, 366),
            new TrashBin("paper", "@../../assets/paper_bin.png", 490, 366)
    );
    private final List<TrashItem> trashList = List.of(
            new TrashItem(1, "Vỏ chuối", "compost", "@../../assets/banana.png"),
            new TrashItem(2, "Túi nilon", "plastic", "@../../assets/nilon.png"),
            new TrashItem(3, "Lon bia", "metal", "@../../assets/can.png"),
            new TrashItem(4, "Vỏ chai nước", "plastic", "@../../assets/bottle.png"),
            new TrashItem(5, "Đinh", "metal", "@../../assets/nail.png")
    );
    private MediaPlayer bgm;
// 🛑 Gọi hàm này khi thoát giao diện
    //Ngững phát nhạc

    public void stopBackgroundMusic() {
        if (bgm != null) {
            bgm.stop();       // dừng phát
            bgm.dispose();    // giải phóng tài nguyên
            bgm = null;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Tạo các thùng rác từ danh sách TrashBin
        new Thread(() -> {
            try {
                Media media = new Media(getClass().getResource("/sound/game.mp3").toExternalForm());
                bgm = new MediaPlayer(media);
                bgm.setVolume(0.4);
                bgm.setCycleCount(MediaPlayer.INDEFINITE);
                bgm.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        for (TrashBin bin : bins) {
            ImageView binImage = new ImageView(new Image(bin.getImageUrl()));
            binImage.setFitWidth(111);
            binImage.setFitHeight(130);
            binImage.setLayoutX(bin.getX());
            binImage.setLayoutY(bin.getY());
            binImage.setUserData(bin);
            root.getChildren().add(binImage);
        }
        // setup bắn sự kiện bàn phím
        startFallingTrash();
        Platform.runLater(() -> {
            Scene scene = root.getScene();   // Lấy Scene hiện tại
            setupKeyHandler(scene);          // ✅ Truyền Scene vào
        });

        Platform.runLater(() -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                handlePlayerExit();
            });
        });
        //set up đồng hồ
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    timeLeft--;
                    clockLabel.setText("Thời gian: " + String.valueOf(timeLeft) + " s");
                    // Khi hết giờ
                    if (timeLeft <= 0) {
                        try {
                            System.out.println("Đã gửi finish_game");
                            client.sendMessage(new Message("finish_game", ""));
                        } catch (IOException ex) {
                            Logger.getLogger(GameUIController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        clockLabel.setText("Time’s up!");
                    }
                })
        );

        timeline.setCycleCount(timeLeft); // chạy đúng 15 lần
        timeline.play();
    }

    //làm rác rơi ngẫu nhiên
    private void startFallingTrash() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            TrashItem trash = trashList.get(random.nextInt(trashList.size()));
            ImageView rac = new ImageView(new Image(trash.getImageUrl()));
            rac.setFitWidth(60);
            rac.setFitHeight(60);
            rac.setLayoutX(random.nextInt(560));
            rac.setLayoutY(-60);
            rac.setUserData(trash);

            root.getChildren().add(rac);
            //xử lý phân loại rác
            sortTrash(rac);
            //thả rơi rác  
            TranslateTransition fall = new TranslateTransition(Duration.seconds(10 + random.nextDouble() * 2), rac);
            fall.setFromY(0);
            fall.setToY(420);
            // Khi rác rơi xong mà chưa được thả → tính là MISS
            fall.setOnFinished(ev -> {
                if (root.getChildren().contains(rac)) {
                    this.miss += 1;
                    System.out.println("💨 Miss! Rác rơi xuống đất:  " + this.miss + trash.getName());
                    root.getChildren().remove(rac);
                }
            });
            fall.play();
        }));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        // Sau 15 giây, dừng việc tạo rác
        PauseTransition stopAfter15s = new PauseTransition(Duration.seconds(timeLeft - 5));
        stopAfter15s.setOnFinished(event -> {
            timeline.stop();
//            showEndGame();
            try {
//                client.sendMessage(new Message("finish_game", ""));
            } catch (Exception e) {
            }
            System.out.println("Đã end game rồi");
        });
        stopAfter15s.play();
    }

    //xử lý phân loại rác
    public void sortTrash(ImageView rac) {
        rac.setOnMousePressed(ev -> {
            TrashItem item = (TrashItem) rac.getUserData();
            System.out.println("🧹 Thu gom: " + item.getName());

            // Nếu đang có rác khác được chọn → bỏ chọn nó
            if (selectedTrash != null && selectedTrash != rac) {
                selectedTrash.setEffect(null);
            }

            // 🌟 Thêm viền sáng quanh rác khi ấn vào
            DropShadow glow = new DropShadow();
            glow.setColor(Color.LIME); // Màu viền sáng (xanh lá)
            glow.setRadius(25);        // Độ lan của ánh sáng
            glow.setSpread(0.6);       // Mức độ đậm của ánh sáng
            rac.setEffect(glow);       // Áp hiệu ứng cho ImageView            
            // Hiệu ứng biến mất
//            FadeTransition fade = new FadeTransition(Duration.millis(200), rac);
//            fade.setToValue(0);
//            fade.setOnFinished(evt -> root.getChildren().remove(rac));
//            fade.play();
            // Ghi nhớ rác đang được chọn
            selectedTrash = rac;
        });

    }

    //hàm xử lý phân loại rác bằng bàn phím
    public void setupKeyHandler(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (selectedTrash != null) { // Có rác đang được chọn
                TrashItem item = (TrashItem) selectedTrash.getUserData();
                switch (e.getCode()) {
                    case DIGIT1: // Phím số 1
                        handleTrashType(item, "compost", Color.LIMEGREEN);
                        break;
                    case DIGIT2:
                        handleTrashType(item, "plastic", Color.LIMEGREEN);
                        break;
                    case DIGIT3:
                        handleTrashType(item, "metal", Color.LIMEGREEN);
                        break;
                    case DIGIT4:
                        handleTrashType(item, "paper", Color.LIMEGREEN);
                        break;
                    default:
                        System.out.println("❌ Phím khác được nhấn — không xóa rác");
                        break;
                }
            }
        });
    }

    // Hàm xử lý logic khi người chơi ấn phím loại rác
    private void handleTrashType(TrashItem item, String type, Color glowColor) {
        System.out.println("Phân loại: " + item.getName() + "  " + type);

        // Kiểm tra đúng loại rác
        if (item.getType().equalsIgnoreCase(type)) {
            item.setStatus("classified");
            item.setUserId(client.getUser().getId());

            System.out.println("Đúng loại! +10 điểm");
            try {
                client.sendMessage(new Message("update_point", item));
            } catch (Exception e) {
                System.out.println("Lỗi gửi đồng bộ điểm!");
            }
            // Hiệu ứng sáng màu loại rác (ví dụ: xanh lá, vàng,...)
            DropShadow glow = new DropShadow();
            glow.setColor(glowColor);
            glow.setRadius(25);
            glow.setSpread(0.6);
            selectedTrash.setEffect(glow);
            //thêm âm thanh báo đúng
            AudioClip correctSound = new AudioClip(getClass().getResource("/sound/correct.wav").toExternalForm());
            correctSound.play();
            // Rác biến mất sau khi xử lý (đúng loại)
            FadeTransition fade = new FadeTransition(Duration.millis(300), selectedTrash);
            fade.setToValue(0);
            fade.setOnFinished(evt -> {
                root.getChildren().remove(selectedTrash);
                selectedTrash = null; // reset lựa chọn
            });
            fade.play();
        } else {
            // Hiệu ứng sáng đỏ khi sai
            DropShadow wrongGlow = new DropShadow();
            wrongGlow.setColor(Color.RED);
            wrongGlow.setRadius(30);
            wrongGlow.setSpread(0.8);
            selectedTrash.setEffect(wrongGlow);
            //thêm âm thanh báo sai
            AudioClip wrongSound = new AudioClip(getClass().getResource("/sound/error.wav").toExternalForm());

            wrongSound.play();
            // Có thể thêm hiệu ứng “rung” nhẹ để báo sai
            TranslateTransition shake = new TranslateTransition(Duration.millis(80), selectedTrash);
            shake.setByX(10);
            shake.setCycleCount(4);
            shake.setAutoReverse(true);
            shake.play();

        }

    }

    public void handlePlayerExit() {

    }

    @FXML
    private void handleQuitGame() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Thoát Trò Chơi");
            alert.setHeaderText(null);
            alert.setContentText("Bạn có chắc chắn muốn thoát trò chơi không?");
            ButtonType yesButton = new ButtonType("Có", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("Không", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(yesButton, noButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == yesButton) {
                Message quitMessage = new Message("quit_game", null);
                try {
                    client.sendMessage(quitMessage);
                    // Quay về màn hình chính
                    client.showMainUI();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //
    public void updateScore(Message mess) {
        Platform.runLater(() -> {
//            myScore.setText(score);
            String m = (String) mess.getContent();
            String[] scores = m.trim().split("\\s+");
            myScore.setText("YOU : " + scores[0]);
            opponentScore.setText("OPPONENT : " + scores[1]);

        });

    }

    public void endMatch(String result) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Kết Thúc Trận Đấu");
            alert.setHeaderText(null);
            alert.setContentText(result);
            alert.show(); // Thay vì showAndWait()
            // Chuyển về màn hình chính sau một khoảng thời gian
            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(event -> {
                try {
                    client.showMainUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            delay.play();

        });
    }

    public void setClient(Client client) {
        this.client = client;
    }

}
