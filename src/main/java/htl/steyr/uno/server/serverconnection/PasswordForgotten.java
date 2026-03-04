package htl.steyr.uno.server.serverconnection;

import java.sql.Timestamp;

public class PasswordForgotten {

    private Integer code;
    private Timestamp requestTime;;

        public PasswordForgotten(Integer code, Timestamp requestTime) {
            setCode(code);
            setRequestTime(requestTime);
        }

    public void setCode(Integer codes) {
        code = codes;
    }
    public Integer getCode() {
        return code;
    }

    public void setRequestTime(Timestamp requestTime) {
        this.requestTime = requestTime;
    }
    public Timestamp getRequestTime() {
        return requestTime;
    }
}
