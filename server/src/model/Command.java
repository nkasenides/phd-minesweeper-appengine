package model;

import com.google.gson.JsonObject;

public class Command {

    private final CommandType commandType;
    private final String endpointName;
    private final JsonObject payload;

    public Command(CommandType commandType, String endpointName, JsonObject payload) {
        this.commandType = commandType;
        this.endpointName = endpointName;
        this.payload = payload;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public JsonObject getPayload() {
        return payload;
    }

}
