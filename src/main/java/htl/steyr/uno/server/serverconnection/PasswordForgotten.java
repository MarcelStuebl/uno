package htl.steyr.uno.server.serverconnection;

import java.sql.Timestamp;

public class PasswordForgotten {

    private Integer code;
    private Timestamp requestTime;


    protected PasswordForgotten(Integer code, Timestamp requestTime) {
        setCode(code);
        setRequestTime(requestTime);
    }

    protected void setCode(Integer codes) {
        code = codes;
    }
    protected Integer getCode() {
        return code;
    }

    protected void setRequestTime(Timestamp requestTime) {
        this.requestTime = requestTime;
    }
    protected Timestamp getRequestTime() {
        return requestTime;
    }
}
