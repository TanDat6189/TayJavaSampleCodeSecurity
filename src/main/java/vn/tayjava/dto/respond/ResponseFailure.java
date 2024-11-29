package vn.tayjava.dto.respond;

import org.springframework.http.HttpStatusCode;

public class ResponseFailure extends ResponseSuccess {
    public ResponseFailure(HttpStatusCode status, String message) {
        super(status, message);
    }
}
