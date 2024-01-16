package Pages;

import Common.Message;
import Common.MessageType;
import Common.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/********
 * This page is for the launch of chat pages.
 * It mainly includes
 *      1. Change socket window
 *      2. Logout (waiting for implements)
 *      3. Set the profile picture.
 *      4. Change the getter.
 *      5. Show the chat pages.
 */
public class ChatPage {

    public static Socket socket;
    private Message m = new Message();

    public static User getter = new User();
    public static int port = 9876; // Default chat port, in order to send messages
    @FXML
    private Label UserName;
    @FXML
    private ImageView ProfilePicture; // profile picture show

    @FXML
    private Label noGetterFound;

    @FXML
    private TextField getterText;

    @FXML
    private TextField socketText;


    public ChatPage() throws IOException {
    }


    // change the port as you want
    @FXML
    void ChangeSocketWindow(ActionEvent event) {

        port = Integer.parseInt(socketText.getText());
    }

    //waiting for log out
    @FXML
    void LogOut(ActionEvent event) {
        // 登出ID 实质是断开和服务器的连接

    }

    // set the profile picture if there is
    public void initialize() {
        if (this.ProfilePicture != null) {
            if (LoginPages.sender.getProfilePicture() != null) {
                Image profilePicture = new Image(new ByteArrayInputStream(LoginPages.sender.getProfilePicture()), 150, 180, true, true);
                ProfilePicture.setImage(profilePicture);
            }
            if (LoginPages.sender.getUserName() != null) {
                UserName.setText(LoginPages.sender.getUserName());
            }
        }
    }

    // this method is used to set a profile picture
    @FXML
    void setTheProfilePicture(ActionEvent event) throws Exception {
        // In this process, profile picture is set on the chat page and the server
        // so next time when the user login, the profile picture show on the chat page
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif");
        fileChooser.getExtensionFilters().add(extFilter);
        File chosenFile = fileChooser.showOpenDialog(null);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(chosenFile));
        byte[] bytes = streamToByteArray(bis);
        Image image = new Image(new ByteArrayInputStream(bytes), 150, 180, true, true);
        ProfilePicture.setImage(image);
        m.setPicture(bytes);
        m.setMsgType(MessageType.MESSAGE_PROFILEPICTURE);
        SendTheMessage(event);
    }

    // this method is used to send a message
    @FXML
    void SendTheMessage(ActionEvent event) throws Exception {
        if (!m.getMsgType().equals(MessageType.MESSAGE_PROFILEPICTURE)) {
            // note that "Please set the getter" if the getter is not set
            if (getter.getUserID() == null || getter.getUserID().equals("")) {
                noGetterFound.setText("Please set the getter.");
                Runnable runnable = new NoElementFound(noGetterFound);
                Thread NoGetterFound = new Thread(runnable);
                NoGetterFound.start();
                return;
            }
        }
        // use socket to send the message and clear the main text
        socket = new Socket(InetAddress.getLocalHost(), LoginPages.sender.getPort());
        socket.setKeepAlive(false);
        m.setGetter(getter);
        m.setSender(LoginPages.sender);
        if (m.getMsgType().equals(MessageType.MESSAGE_DEFAULT))
            m.setMsgType(MessageType.MESSAGE_CONTENT);

        // set the time format
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        m.setSendTime(formattedDateTime);
        m.setContent("");
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(m);
        System.out.println(socket.isClosed());
        oos.close();
        socket.close();
        // reset the message
        m = new Message();
    }

    // this method is used to change the getter
    @FXML
    void changeTheGetter(ActionEvent event) throws IOException {
        getter.setUserID(getterText.getText());
        // open up the chat pages and start chatting
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ChatPages.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("Chat With " + getterText.getText());
        stage.setScene(scene);
        stage.show();
    }

    // this method is used to show the chat page
    public void showThePage() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatPage.class.getResource("ChatPage.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setOnCloseRequest(windowEvent -> {
            System.exit(0);
        });
        stage.setTitle("JavaChatRoom");
        stage.setScene(scene);
        stage.show();
    }

    // this class is used to show the no element found notation
    public static class NoElementFound implements Runnable {
        private Label label;

        public NoElementFound(Label l) {
            this.label = l;
        }

        @Override
        public void run() {
            if (!label.isVisible())
                label.setVisible(true);
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            label.setVisible(false);
        }
    }


    // this is used to show the decoration picture of no element found
    public static class NoElementFoundImage implements Runnable {

        private ImageView imageView;

        public NoElementFoundImage(ImageView i) {
            this.imageView = i;
        }

        // 逆天错误 当一个进程关闭时会影响其他进程的label 所以加入三秒内按了两次 那么第三秒就会消失
        @Override
        public void run() {
            if (imageView.getOpacity() != 1)
                imageView.setOpacity(1);
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            imageView.setOpacity(0);
        }
    }

    // change the stream to byte array in order to send picture and video
    public static byte[] streamToByteArray(InputStream is) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int len;
        while ((len = is.read(b)) != -1) {
            bos.write(b, 0, len);
        }
        byte[] array = bos.toByteArray();
        bos.close();
        return array;
    }


}

