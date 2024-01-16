package Pages;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**********
 * This is the main function of the project, which means the entrance.
 * It is used to launch the login pages
 */
public class MainFunc extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // open up the page
        FXMLLoader fxmlLoader = new FXMLLoader(MainFunc.class.getResource("LoginPages.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) throws IOException {
        launch();
    }
}
