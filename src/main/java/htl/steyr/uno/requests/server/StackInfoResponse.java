package htl.steyr.uno.requests.server;

import java.io.Serializable;

/**
 * The StackInfoResponse class is a simple data transfer object (DTO) that encapsulates information about the status of the card stack in a game of Uno.
 * Status Codes:
 * 0: The card stack is in a normal state, and players can draw cards as usual.
 * 1: The card stack is empty, and no more cards can be drawn.
 */

public record StackInfoResponse(Integer statusCode) implements Serializable {

     @Override
     public String toString() {
          return "StackInfoResponse{" +
                  "statusCode=" + statusCode +
                  '}';
     }
}
