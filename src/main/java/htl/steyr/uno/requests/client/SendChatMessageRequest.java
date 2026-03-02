package htl.steyr.uno.requests.client;

import htl.steyr.uno.User;

import java.io.Serializable;

public class SendChatMessageRequest implements Serializable {

        private String message;
        private User user;

        public SendChatMessageRequest(String message, User user) {
            this.message = message;
            this.user = user;
        }

        @Override
        public String toString() {
            return "sendChatMessage{" +
                    "username='" + user.getUsername() + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
}
