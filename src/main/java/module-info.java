module htl.steyr.uno {
    requires javafx.controls;
    requires javafx.fxml;


    opens htl.steyr.uno to javafx.fxml;
    exports htl.steyr.uno;
}