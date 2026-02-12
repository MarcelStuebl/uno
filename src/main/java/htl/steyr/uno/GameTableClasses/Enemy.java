package htl.steyr.uno.GameTableClasses;

public class Enemy {

    private String username;
    private boolean isCurrentTurn;
    private int cardCount;



    public Enemy(String username, boolean isCurrentTurn, int cardCount) {
        this.username = username;
        this.isCurrentTurn = isCurrentTurn;
        this.cardCount = cardCount;
    }



    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isCurrentTurn() {
        return isCurrentTurn;
    }
    public void setCurrentTurn(boolean currentTurn) {
        isCurrentTurn = currentTurn;
    }

    public int getCardCount() {
        return cardCount;
    }
    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }
    public void incrementCardCount(int count) {
        this.cardCount += count;
    }
    public void decrementCardCount(int count) {
        this.cardCount -= count;
    }


}




