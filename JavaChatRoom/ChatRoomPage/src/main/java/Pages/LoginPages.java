package Pages;

import Common.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import Common.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;


/***********
 * This is the controller of LoginPages.
 * This mainly includes:
 *      1. Login button, send the login request.
 *      2. Register button, send the register request.
 *      3. Check user, used to check whether the user is legal.
 *      4. Send login operation.
 *      5. Get the message from the server.
 */
public class LoginPages {

    public static Socket socket;
    public static Message m;
    @FXML
    private TextField userID;

    @FXML
    private PasswordField userPwd;
    @FXML
    private TextField userPort;
    @FXML
    private Label again; // used to show try again
    @FXML
    private Label suc; // show login successfully
    private User u;
    public static User sender;

    // This method is used to send user's info and check it
    @FXML
    void LoginButton(ActionEvent event) throws IOException, ClassNotFoundException {
        // send login request
        SendLoginOperation();
        // get ID and Psw here and send it to the server. in the server place judge whether it can login 
        String ID = userID.getText();
        String pwd = userPwd.getText();
        int port = Integer.parseInt(userPort.getText());
        u = new User(ID, pwd);
        u.setPort(port);
        // judge whether it can login
        if (checkUser(u)) {
            // launch the chat page
            ChatPage chatPage = new ChatPage();
            chatPage.showThePage();
            // close the login page
            Stage LoginStage = (Stage) suc.getScene().getWindow();
            LoginStage.close();
            getMessage.start();

        } else {
            // show "please try again"
            Runnable LoginAgain = new ChatPage.NoElementFound(again);
            Thread LoginAgainShow = new Thread(LoginAgain);
            LoginAgainShow.start();
            // restart the login thread in the server
            SendLoginOperation();
        }
    }

    // open up the register page
    @FXML
    void RegisterButton(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("RegisterPage.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("Register");
        stage.setScene(scene);
        stage.show();
    }

    // this method is used to check whether the user is legal
    public boolean checkUser(User u) throws IOException, ClassNotFoundException {
        // use port 9999 to send the user object and get the message object
        boolean b = false;
        Socket socket = new Socket(InetAddress.getLocalHost(), 9999);

        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(u);
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

        Message msg = (Message) ois.readObject();

        ois.close();
        oos.close();
        socket.close();
        // set the b according to the message
        if (msg.getMsgType().equals(MessageType.MESSAGE_LOGIN_SUCCEED)) {
            b = true;
            socket = new Socket(InetAddress.getLocalHost(), 9999);
            ObjectInputStream ois2 = new ObjectInputStream(socket.getInputStream());
            sender = (User) ois2.readObject();
            try {
                Thread.sleep(20);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ois2.close();
            socket.close();
        }

        return b;
    }

    // send the login request, in order to launch the login thread in the server
    void SendLoginOperation() {
        // use port 5210 to send request
        try {
            socket = new Socket(InetAddress.getLocalHost(), 5210);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            Message m = new Message();
            m.setMsgType(MessageType.MESSAGE_LOGIN);
            oos.writeObject(m);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // this thread is used to receiving message from the server when login succussfully
    Thread getMessage = new Thread(new Runnable() {
        @Override
        public void run() {

            try {
                // send the user object and build up the connection
                Socket Keep = new Socket(InetAddress.getLocalHost(), 9876);
                ObjectOutputStream oos = new ObjectOutputStream(Keep.getOutputStream());
                oos.writeObject(u);
                Thread.sleep(100);
                while (true) {
                    ObjectInputStream ois = new ObjectInputStream(Keep.getInputStream());
                    m = (Message) ois.readObject();

                    // show the message.
                    if (m.getPicture() != null) {
                        ChatPages.addTheMessageToMessageHistory();
                    } else if (m.getContent() != null) {
                        ChatPages.addTheMessageToMessageHistory();
                    } else if (m.getVideo() != null) {
                        ChatPages.addTheMessageToMessageHistory();
                    }
                    Thread.sleep(100);
                    // edit the set of scroll pane, in order to make the scroll to the bottom when receiving a message
                    ChatPages.ChatScrollPaneUse.setVvalue(ChatPages.ChatScrollPaneUse.getVmax());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    });

}