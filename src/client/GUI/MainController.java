package client.GUI;

import client.Client;
import common.Match;
import common.MatchDetail;
import common.MatchDetails;
import common.Message;
import common.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.Timestamp;
import javafx.application.Platform;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class MainController {

    @FXML
    private TableColumn<Match, String> matchTimeColumn;

    @FXML
    private TableColumn<MatchDetails, String> timeColumn;

    @FXML
    private TextField searchField;
    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, Integer> pointsColumn;
    @FXML
    private TableColumn<User, String> statusColumn;
    @FXML
    private Label statusLabel;

    @FXML
    private Label userLabel;

    private Client client;
    private ObservableList<User> usersList = FXCollections.observableArrayList();

    @FXML
    private TableView<User> leaderboardTable;
    @FXML
    private TableColumn<User, String> lbNameColumn;
    @FXML
    private TableColumn<User, Integer> lbPointsColumn;

    @FXML
    private TableView<MatchDetail> historyTable2;
    @FXML
    private TableColumn<MatchDetail, Integer> idColumn;
    @FXML
    private TableColumn<MatchDetail, String> nameTrashColumn;
    @FXML
    private TableColumn<MatchDetail, String> typeTrashColumn;
    @FXML
    private TableColumn<MatchDetail, String> resultColumn;
    @FXML
    private TableColumn<MatchDetail, String> timeTrashColumn;

    @FXML
    private TableView<Match> matchesTable;
    @FXML
    private TableColumn<Match, Integer> matchIdColumn;
    @FXML
    private TableColumn<Match, String> opponentColumn;
    @FXML
    private TableColumn<Match, String> scoreMatchColumn;
    @FXML
    private TableColumn<Match, String> matchResultColumn;

    public void setClient(Client client) throws IOException {
        this.client = client;
        loadUsers();
        loadLeaderboard();
        loadUserMatches(); // Tải danh sách trận đấu
        userLabel.setText("Xin chào " + client.getUser().getUsername() + "!");
    }

    private void loadUserMatches() throws IOException {
        Message request = new Message("get_user_matches", null);
        client.sendMessage(request);
    }

    // Thêm phương thức để cập nhật bảng xếp hạng
    public void updateLeaderboard(List<User> leaderboard) {
        ObservableList<User> leaderboardList = FXCollections.observableArrayList(leaderboard);
        leaderboardTable.setItems(leaderboardList);
    }

    private void loadUsers() throws IOException {
        // Gửi yêu cầu lấy danh sách người chơi
        Message request = new Message("get_users", null);
        client.sendMessage(request);
    }

    @FXML
    private void handleLogout() throws IOException {
        client.getUser().setStatus("offline");
        // Gửi yêu cầu đăng xuất
        if (client.getUser() != null) {
            Message logoutMessage = new Message("logout", client.getUser().getId());
            client.sendMessage(logoutMessage);
            client.showLoginUI();
        }
    }

    @FXML
    private void handleFilterOnline() {
        ObservableList<User> filtered = FXCollections.observableArrayList();
        for (User user : usersList) {
            if (user.getStatus().equalsIgnoreCase("online")) {
                filtered.add(user);
            }
        }
        usersTable.setItems(filtered);
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().toLowerCase();
        if (keyword.isEmpty()) {
            usersTable.setItems(usersList);
            return;
        }
        ObservableList<User> filtered = FXCollections.observableArrayList();
        for (User user : usersList) {
            if (user.getUsername().toLowerCase().contains(keyword)) {
                filtered.add(user);
            }
        }
        usersTable.setItems(filtered);
    }

    // Cập nhật danh sách người chơi từ server
    public void updateUsersList(List<User> newUsers) {
        Platform.runLater(() -> {
            usersList.setAll(newUsers);
            usersTable.setItems(usersList);
            usersTable.refresh(); // Buộc bảng cập nhật lại
        });
    }

    // Cập nhật trạng thái người chơi
    public void updateStatus(String statusUpdate) {
        if (statusUpdate == null || statusUpdate.isEmpty()) {
            return;
        }
        String[] parts = statusUpdate.split(" ");
        if (parts.length >= 3) {
            String username = parts[0];
            String status = parts[2].replace(".", "");
            for (User user : usersList) {
                if (user.getUsername().equalsIgnoreCase(username)) {
                    user.setStatus(status);
                    usersTable.refresh();
                    break;
                }
            }
        }
    }

    // Hiển thị yêu cầu trận đấu
    public void showMatchRequest(int requesterId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Yêu Cầu Trận Đấu");
        alert.setHeaderText("Bạn nhận được yêu cầu trận đấu từ người chơi ID: " + requesterId);
        alert.setContentText("Bạn có muốn đồng ý?");

        alert.showAndWait().ifPresent(response -> {
            boolean accepted = response == ButtonType.OK;
            Object[] data = {requesterId, accepted};
            Message responseMessage = new Message("match_response", data);
            try {
                client.sendMessage(responseMessage);
            } catch (IOException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    // Xử lý phản hồi trận đấu
    public void handleMatchResponse(String response) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Trận Đấu");
        alert.setHeaderText(null);
        alert.setContentText(response);
        alert.showAndWait();
    }

    int demRole = 0;
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

    @FXML
    private void initialize() {
        //cấu hình âm thanh game background

        new Thread(() -> {
            try {
                Media media = new Media(getClass().getResource("/sound/main.mp3").toExternalForm());
                bgm = new MediaPlayer(media);
                bgm.setVolume(0.4);
                bgm.setCycleCount(MediaPlayer.INDEFINITE);
                bgm.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Cấu hình bảng người chơi
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        pointsColumn.setCellValueFactory(new PropertyValueFactory<>("points"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Custom cell factory cho statusColumn
        statusColumn.setCellFactory(column -> new TableCell<User, String>() {
            private final HBox hBox = new HBox(5);
            private final Circle circle = new Circle(5);
            private final Label label = new Label();

            {
                label.getStyleClass().add("status-label");
                label.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
                hBox.getChildren().addAll(circle, label);
            }

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Color color;
                    System.out.println(status);
                    switch (status.trim()) {
                        case "online":
                            color = Color.GREEN;
                            label.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            break;
                        case "ingame":
                            color = Color.RED;
                            label.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                        case "offline":
                            color = Color.GRAY;
                            label.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
                            break;
                        default:
                            color = Color.BLACK;
                            label.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
                            break;
                    }
                    System.out.println(status + " " + color);
                    circle.setFill(color); // Cập nhật màu của Circle
                    label.setText(status); // Cập nhật văn bản của Label
                    setGraphic(hBox); // Đặt HBox chứa Circle và Label làm đồ họa của ô
                    setText(null); // Không cần văn bản mặc định cho ô
                }
            }

        });

        // Sự kiện double click để gửi yêu cầu trận đấu
        usersTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    User clickedUser = row.getItem();
                    if (clickedUser.getId() != client.getUser().getId()) {
                        Message matchRequest = new Message("request_match", clickedUser.getId());
                        try {
                            client.sendMessage(matchRequest);
                        } catch (IOException ex) {
                            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
            return row;
        });

        // Cấu hình bảng xếp hạng
        lbNameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        lbPointsColumn.setCellValueFactory(new PropertyValueFactory<>("points"));

//         Cấu hình bảng lịch sử đấu
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameTrashColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeTrashColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("result"));
        timeTrashColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

        // Cấu hình bảng danh sách trận đấu
        matchIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        opponentColumn.setCellValueFactory(cellData -> {
            Match match = cellData.getValue();
            String opponentName = match.getOpponentName(client.getUser().getId());
            return new SimpleStringProperty(opponentName);
        });
        scoreMatchColumn.setCellValueFactory(cellData -> {
            Match match = cellData.getValue();
            String scoreMatch = match.getScoreMatch(client.getUser().getId());
            return new SimpleStringProperty(scoreMatch);
        });
        matchResultColumn.setCellValueFactory(cellData -> {
            Match match = cellData.getValue();
            String result = match.getResult(client.getUser().getId());
            return new SimpleStringProperty(result);
        });

        // Cấu hình cột thời gian cho matchesTable
        matchTimeColumn.setCellValueFactory(cellData -> {
            Timestamp time = cellData.getValue().getTime();
            return new SimpleStringProperty(time != null ? time.toString() : "");
        });

        // Sự kiện click để hiển thị chi tiết trận đấu (sửa đổi để sử dụng listener)
        matchesTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Match clickedMatch = newValue;
                try {
                    Message request = new Message("get_match_details", clickedMatch.getId());
                    System.out.println(clickedMatch.getId());
                    client.sendMessage(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void showMatchDetails2(List<MatchDetail> details) {
        ObservableList<MatchDetail> detailsList = FXCollections.observableArrayList(details);
        historyTable2.setItems(detailsList);
    }

    public void updateMatchesList(List<Match> matches) {
        ObservableList<Match> matchesList = FXCollections.observableArrayList(matches);
        matchesTable.setItems(matchesList);
    }

    private void loadLeaderboard() throws IOException {
        Message request = new Message("get_leaderboard", null);
        client.sendMessage(request);
    }

    public void updateMatchHistory(List<MatchDetail> history) {
        ObservableList<MatchDetail> historyList = FXCollections.observableArrayList(history);
        historyTable2.setItems(historyList);
    }
}
