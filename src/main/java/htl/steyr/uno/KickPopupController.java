package htl.steyr.uno;

import javafx.event.ActionEvent;

public class KickPopupController {
    private Runnable onReturn;

    public void returnToLobbySelection(ActionEvent event) {
        if (onReturn == null) {
            throw new IllegalStateException("Kick popup return action has not been configured");
        }
        onReturn.run();
    }

    public void setOnReturn(Runnable onReturn) {
        this.onReturn = onReturn;
    }
}
