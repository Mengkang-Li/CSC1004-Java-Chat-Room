package Pages;

import Common.Message;
import Common.MessageType;
import Common.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import Pages.LoginPages;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**********
 * This class is the controller of the RegisterPage.
 * This mainly includes
 *      1. Send register request.
 *      2. Confirm register.
 */

public class RegisterPage {
    @FXML
    private Label againLabel;

    @FXML
    private TextField ID;

    @FXML
    private TextField Password;

    @FXML
    private TextField PhoneNumber;

    @FXML
    private Button Register;

    @FXML
    private TextField UserName;
    @FXML
    private ImageView againImage;
    @FXML
    private TextField Address;
    @FXML
    private TextField Age;

    // This is used to confirm registering the id, including send the user to the server.
    @FXML
    void ConfirmRegister(ActionEvent event) throws IOException, ClassNotFoundException {
        // send register request
        SendRegisterOperation();
        User u = new User(ID.getText(), UserName.getText(), Password.getText(), PhoneNumber.getText());
        u.setAge(Age.getText());
        u.setAddress(Address.getText());
        // send the user information to the server
        Message msg = connectWithServer(u);
        // register successfully and close the window
        if (msg.getMsgType().equals(MessageType.MESSAGE_LOGIN_SUCCEED)) {
            Stage stage = (Stage) Register.getScene().getWindow();
            stage.close();
        } else {
            // show the "show again" label
            Runnable RegisterAgain = new ChatPage.NoElementFound(againLabel);
            Thread RegisterAgainShow = new Thread(RegisterAgain);
            Runnable RegisterAgainImage = new ChatPage.NoElementFoundImage(againImage);
            Thread RegisterAgainShowImage = new Thread(RegisterAgainImage);
            RegisterAgainShow.start();
            RegisterAgainShowImage.start();
        }
    }

    Message connectWithServer(User u) throws IOException, ClassNotFoundException {
        // use 9998 to send the user object.
        LoginPages.socket = new Socket(InetAddress.getLocalHost(), 9998);
        ObjectOutputStream oos = new ObjectOutputStream(LoginPages.socket.getOutputStream());
        oos.writeObject(u);
        ObjectInputStream ois = new ObjectInputStream(LoginPages.socket.getInputStream());
        Message msg = (Message) ois.readObject();
        return msg;
    }

    void SendRegisterOperation() {
        // open up the server's register operation.
        try {
            LoginPages.socket = new Socket(InetAddress.getLocalHost(), 5210);
            ObjectOutputStream oos = new ObjectOutputStream(LoginPages.socket.getOutputStream());
            Message m = new Message();
            m.setMsgType(MessageType.MESSAGE_REGISTER);
            oos.writeObject(m);
            oos.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
