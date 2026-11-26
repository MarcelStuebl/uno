package htl.steyr.uno;

import javafx.event.ActionEvent;
import lombok.*;

public class KickPopupController {
    @Setter private Runnable onReturn;

    public void returnToLobbySelection(ActionEvent event) {
        if (onReturn == null) {
            throw new IllegalStateException("Kick popup return action has not been configured");
        }
        onReturn.run();
    }

}
