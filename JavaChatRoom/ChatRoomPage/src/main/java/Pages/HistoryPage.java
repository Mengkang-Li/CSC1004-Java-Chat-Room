package Pages;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.UUID;

/********
 * This page is the history page.
 * It mainly includes one method, show the elements in the fx elements.
 */
public class HistoryPage {
    @FXML
    private VBox historyVBox;
    // set as initialize, in order to show the elements as soon as it is launched
    public void initialize() throws IOException {
        List<Object> history = ChatPages.history;
        for (int i = 0; i < (history.size() + 1) / 5; i++) {
            // one message is used for context information
            String oneMessage = "";
            Label message = new Label();
            ImageView imageView = new ImageView();
            MediaView mediaView = new MediaView();
            for (int j = 0; j < 5; j++) {
                if (j < 3) {
                    oneMessage = oneMessage + history.get(i * 5 + j) + "\n";
                } else if (j == 3 && history.get(i * 5 + j) != null) {
                    // show the picture
                    ByteArrayInputStream inputStream = new ByteArrayInputStream((byte[]) history.get(i * 5 + j));
                    Image image = new Image(inputStream);
                    imageView.setImage(image);
                    UUID uuid = UUID.randomUUID();
                    String fileName = "./JavaChatRoom/ChatRoomPage/images/" + uuid + ".png";
                    try {
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileName));
                        bos.write((byte[]) history.get(i * 5 +j));
                        bos.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    imageView.setOnMouseClicked(mouseEvent -> {
                        File file = new File(fileName);
                        Desktop desktop = Desktop.getDesktop();
                        try {
                            desktop.open(file);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else if (j == 4 && history.get(i * 5 + j) != null) {
                    // store and show the media
                    UUID uuid = UUID.randomUUID();
                    String fileName = "./JavaChatRoom/ChatRoomPage/images/" + uuid + ".mp4";
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileName));
                    bos.write((byte[]) history.get(i * 5 + j));
                    bos.close();
                    Media media = new Media(new File(fileName).toURI().toString());
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    mediaView = new MediaView(mediaPlayer);
                    mediaView.setOnMouseClicked(mouseEvent -> {
                        Desktop desktop = Desktop.getDesktop();
                        try {
                            desktop.open(new File(fileName));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
            message.setText(oneMessage);
            // add to the element VBox
            historyVBox.getChildren().add(message);
            if (imageView.getImage() != null) {
                historyVBox.getChildren().add(imageView);
            }
            if (mediaView.getMediaPlayer() != null) {
                historyVBox.getChildren().add(mediaView);
            }
        }
    }
}
