package model.response;

import respondx.ErrorResponse;

public class UnknownFailureResponse extends ErrorResponse {

    public UnknownFailureResponse(String message) {
        super("Unknown failure", message);
    }

}
