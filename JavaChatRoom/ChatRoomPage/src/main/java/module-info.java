module Pages {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.media;
    opens Pages to javafx.fxml;
    exports Pages;
}
