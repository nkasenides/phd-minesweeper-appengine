package model.response;

import com.google.gson.JsonObject;
import respondx.ErrorResponse;

public class MissingParameterResponse extends ErrorResponse {

    public MissingParameterResponse(String parameterName) {
        super("Missing parameter(s)", "Parameter '" + parameterName + "' is missing.");
    }

}
