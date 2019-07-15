package servlets;

import com.googlecode.objectify.Key;
import model.Game;
import model.PartialStatePreference;
import model.Session;
import model.response.InvalidParameterResponse;
import model.response.JoinedGameResponse;
import model.response.MissingParameterResponse;
import respondx.ErrorResponse;
import util.APIUtils;
import util.InputValidator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class JoinGameServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        APIUtils.setResponseHeader(response);

        //1 - Get parameters:
        String gameToken = request.getParameter("gameToken");
        String playerName = request.getParameter("playerName");
        String partialStateWidthStr = request.getParameter("partialStateWidth");
        String partialStateHeightStr = request.getParameter("partialStateHeight");

        //3 - Required params:

        if (gameToken == null) {
            response.getWriter().write(new MissingParameterResponse("gameToken").toJSON());
            return;
        }

        if (playerName == null) {
            response.getWriter().write(new MissingParameterResponse("playerName").toJSON());
            return;
        }

        if (partialStateWidthStr == null) {
            response.getWriter().write(new MissingParameterResponse("partialStateWidth").toJSON());
            return;
        }

        if (partialStateHeightStr == null) {
            response.getWriter().write(new MissingParameterResponse("partialStateHeight").toJSON());
            return;
        }

        //4 - Validate params:
        int partialStateWidth;
        int partialStateHeight;

        try {
            partialStateWidth = Integer.parseInt(partialStateWidthStr);
            if (partialStateWidth < 5) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            response.getWriter().write(new InvalidParameterResponse("Parameter 'partialStateWidth' is invalid. Expected integer >= 5, found '" + partialStateWidthStr + "'.").toJSON());
            return;
        }

        try {
            partialStateHeight = Integer.parseInt(partialStateHeightStr);
            if (partialStateHeight < 5) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            response.getWriter().write(new InvalidParameterResponse("Parameter 'partialStateHeight' is invalid. Expected integer >=5, found '" + partialStateHeightStr + "'.").toJSON());
            return;
        }

        final List<Game> games = ofy().load().type(Game.class).filter("token", gameToken).limit(1).list();
        if (games.size() < 1) {
            response.getWriter().write(new ErrorResponse("Game not found", "The game with token '" + gameToken + "' was not found.").toJSON());
            return;
        }

        final Game referencedGame = games.get(0);

        //check player name
        if (!InputValidator.validateStringAlNumOnly(playerName)) {
            response.getWriter().write(new ErrorResponse("Invalid player name", "The player name must contain alphanumeric characters only.").toJSON());
            return;
        }

        if (playerName.length() > 255 || playerName.length() < 5) {
            response.getWriter().write(new ErrorResponse("Invalid player name", "The player name must be between 5 and 255 characters long.").toJSON());
            return;
        }

        //check if game exists
        final List<Session> sessions = ofy().load().type(Session.class).filter("gameToken", gameToken).filter("playerName", playerName).list();
        if (sessions.size() > 0) {
            response.getWriter().write(new ErrorResponse("Player already exists", "The player with name '" + playerName + "' already exists in game with token '" + gameToken + "'.").toJSON());
            return;
        }

        //check for max players
        final List<Session> sessionsInGame = ofy().load().type(Session.class).filter("gameToken", gameToken).list();
        if (sessionsInGame.size() >= referencedGame.getGameSpecification().getMaxPlayers()) {
            response.getWriter().write(new ErrorResponse("Game full", "Game with token '" + gameToken + "' is already full (" + sessionsInGame.size() + "/" + referencedGame.getGameSpecification().getMaxPlayers() + ").").toJSON());
            return;
        }

        //check for partial state sizes:
        if (partialStateWidth > referencedGame.getGameSpecification().getWidth()) {
            response.getWriter().write(new ErrorResponse("Invalid partial state width", "The partial state width cannot be more than " + referencedGame.getGameSpecification().getWidth() + ".").toJSON());
            return;
        }

        if (partialStateHeight > referencedGame.getGameSpecification().getHeight()) {
            response.getWriter().write(new ErrorResponse("Invalid partial state height", "The partial state height cannot be more than " + referencedGame.getGameSpecification().getHeight() + ".").toJSON());
            return;
        }


        //5 - Process request:
        final Session session = new Session(new PartialStatePreference(partialStateWidth, partialStateHeight), playerName, gameToken, false);
        session.setGameKey(referencedGame.getKey());

        Key<Session> sessionKey = ofy().save().entity(session).now();
        if (sessionKey == null) {
            response.getWriter().write(new ErrorResponse("Failed to join", "Failed to join game (unknown error).").toJSON());
        } else {
            response.getWriter().write(new JoinedGameResponse(gameToken, session.getSessionID()).toJSON());
        }

    }


}
