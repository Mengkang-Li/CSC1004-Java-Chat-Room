package Pages;

import Common.Message;
import Common.MessageType;
import Common.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.util.List;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/****************
 * This class is the main chat pages.
 * It mainly includes send a message, picture, and video.
 */

public class ChatPages {
    private Message m = new Message();
    private User getter = new User();
    public static Socket socket;
    public static ScrollPane ChatScrollPaneUse; // use this static in order to get the ScrollPane in the static function
    @FXML
    private Button Send;

    @FXML
    private ScrollPane ChatScrollPane;

    @FXML
    private TextArea MainTextFile;

    @FXML
    private VBox TheVBox;
    private static VBox TheVBoxTry; // the same reason

    @FXML
    private Button closeChat;

    @FXML
    private Label noGetterFound;
    public static List<Object> history;


    public void initialize() {
        getter = ChatPage.getter;
        TheVBoxTry = TheVBox;
        ChatScrollPaneUse = ChatScrollPane;
        TheVBoxTry.setMaxHeight(Double.MAX_VALUE);
        ChatScrollPaneUse.setFitToHeight(true);
        ChatScrollPaneUse.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        ChatScrollPaneUse.setFitToWidth(true);

    }

    // the function of click close the close the window
    @FXML
    void CloseTheWindow(ActionEvent event) {
        Stage stage = (Stage) closeChat.getScene().getWindow();
        stage.close();
        System.exit(0);
    }

    // the function of look up message history
    @FXML
    void MessageHistory(ActionEvent event) throws Exception {
        // set the type as message history look up.
        m.setMsgType(MessageType.MESSAGE_HISTORY);
        // the message is sent to users themselves
        m.setGetter(LoginPages.sender);
        m.setSender(LoginPages.sender);
        // noting that they are requesting for history
        m.setContent("Request for message history...");
        // send the message
        SendTheMessage(event);
    }

    // this method is used to send a picture
    @FXML
    void SendAPicture(ActionEvent event) throws Exception {
        // choose a picture
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif");
        fileChooser.getExtensionFilters().add(extFilter);
        File chosenFile = fileChooser.showOpenDialog(null);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(chosenFile));
        byte[] bytes = ChatPage.streamToByteArray(bis);
        bis.close();
        if (chosenFile != null) {
            m.setMsgType(MessageType.MESSAGE_SENDANIMAGE);
            m.setPicture(bytes);
            // not null and send
            SendTheMessage(event);
        }
    }

    // this method is used to send a video
    @FXML
    void SendAVideo(ActionEvent event) throws Exception {
        // choose a video
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mov", "*.wmv", "*.flv", "*.mkv");
        fileChooser.getExtensionFilters().add(extFilter);
        File chosenFile = fileChooser.showOpenDialog(null);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(chosenFile));
        byte[] bytes = ChatPage.streamToByteArray(bis);
        bis.close();
        if (chosenFile != null) {
            m.setMsgType(MessageType.MESSAGE_SENDAVIDEO);
            m.setVideo(bytes);
            SendTheMessage(event);
        }
    }

    // This is the main method in the file. This is used to send a message, including the launch of the history page.
    @FXML
    void SendTheMessage(ActionEvent event) throws Exception {
        // If not profile picture and message history, the main text can't be empty.
        if (!m.getMsgType().equals(MessageType.MESSAGE_PROFILEPICTURE) && !m.getMsgType().equals(MessageType.MESSAGE_HISTORY)) {
            if (MainTextFile.getText().isEmpty() && m.getPicture() == null && m.getVideo() == null) {
                noGetterFound.setText("Please set the content.");
                Runnable runnable = new ChatPage.NoElementFound(noGetterFound);
                Thread NoContentFound = new Thread(runnable);
                NoContentFound.start();
                return;
            }
        }
        // send the message and clear the main text
        socket = new Socket(InetAddress.getLocalHost(), LoginPages.sender.getPort());
        socket.setKeepAlive(false);
        // if not message history, the content and other things should be set.
        if (!m.getMsgType().equals(MessageType.MESSAGE_HISTORY)) {
            m.setGetter(getter);
            m.setSender(LoginPages.sender);
            if (m.getMsgType().equals(MessageType.MESSAGE_DEFAULT))
                m.setMsgType(MessageType.MESSAGE_CONTENT);
            m.setContent(MainTextFile.getText());
        }
        // set time format
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        m.setSendTime(formattedDateTime);
        MainTextFile.clear();
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(m);
        System.out.println(socket.isClosed());
        oos.close();
        socket.close();
        // reset the message
        m = new Message();
    }

    // this method is used to show the message on the screen
    static void addTheMessageToMessageHistory() throws IOException {
        // show the message
        // label is context picture is picture video is media
        Message message = LoginPages.m;
        String sender = "sender is: " + message.getSender().getUserName();
        String sendTime = "send time: " + message.getSendTime();
        String content = message.getContent();
        String item = sender + "\n" + sendTime + "\n" + content + "\n" + "\n";
        Label label = new Label();
        label.setText(item);
        label.setTextFill(Color.BLUE);
        // if there's any picture, save it to the desktop
        Platform.runLater(() -> {
            if (LoginPages.m.getPicture() != null) {
                UUID uuid = UUID.randomUUID();
                String FileName = "./JavaChatRoom/ChatRoomPage/images/" + uuid + ".png";
                byte[] b = LoginPages.m.getPicture();
                try {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(FileName));
                    bos.write(b);
                    bos.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // update fx elements in the fx thread
                TheVBoxTry.getChildren().add(label);
                // if picture not null, show picture
                if (message.getPicture() != null) {
                    Image image = new Image(new ByteArrayInputStream(message.getPicture()), 300, 200, true, true);
                    // click the picture, show the picture
                    ImageView imageView = new ImageView(image);
                    imageView.setOnMouseClicked(mouseEvent -> {
                        File file = new File(FileName);
                        Desktop desktop = Desktop.getDesktop();
                        try {
                            desktop.open(file);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    TheVBoxTry.getChildren().add(imageView);
                    Label label1 = new Label("\n");
                    TheVBoxTry.getChildren().add(label1);
                }
            } else if (LoginPages.m.getVideo() != null) {
                // save the video
                UUID uuid = UUID.randomUUID();
                String FileName = "./JavaChatRoom/ChatRoomPage/images/" + uuid + ".mp4";
                byte[] b = LoginPages.m.getVideo();
                try {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(FileName));
                    bos.write(b);
                    bos.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                TheVBoxTry.getChildren().add(label);
                // add the media to the fx elements
                Media media = new Media(new File(FileName).toURI().toString());
                MediaPlayer player = new MediaPlayer(media);
                MediaView mediaView = new MediaView(player);
                mediaView.setOnMouseClicked(mouseEvent -> {
                    Desktop desktop = Desktop.getDesktop();
                    try {
                        desktop.open(new File(FileName));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                TheVBoxTry.getChildren().add(mediaView);
                Label label1 = new Label("\n");
                TheVBoxTry.getChildren().add(label1);
            } else {
                TheVBoxTry.getChildren().add(label);
            }
        });
        // if there exists history message, show the history page
        if (message.getMsgType().equals(MessageType.MESSAGE_HISTORY)) {
            new Thread(() -> {
                history = message.getHistory();
                Platform.runLater(() -> {
                    try {
                        FXMLLoader fxmlLoader = new FXMLLoader(MainFunc.class.getResource("HistoryPage.fxml"));
                        Scene scene = new Scene(fxmlLoader.load());
                        Stage stage = new Stage();
                        stage.setScene(scene);
                        stage.show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }).start();
        }
    }

    // This method is used to set enter to send the message.
    @FXML
    void EnterToSendTheMessage(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && event.isControlDown()) {
            MainTextFile.appendText("\n");
        } else if (event.getCode() == KeyCode.ENTER) {
            Send.fire();
        }
    }
}
