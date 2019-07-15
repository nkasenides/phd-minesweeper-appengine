package model.response;

import respondx.ErrorResponse;

public class InvalidParameterResponse extends ErrorResponse {

    public InvalidParameterResponse(String message) {
        super("Invalid parameter", message);
    }

}
