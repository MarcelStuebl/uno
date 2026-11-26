package htl.steyr.uno.server.serverconnection;

import lombok.*;

import java.sql.Timestamp;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordForgotten {

    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED) private Integer code;
    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED) private Timestamp requestTime;
}
