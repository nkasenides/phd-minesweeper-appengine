package servlets;

import model.Game;
import model.StatelessGame;
import model.response.GetGameResponse;
import model.response.ListGamesResponse;
import model.response.MissingParameterResponse;
import respondx.ErrorResponse;
import respondx.SuccessResponse;
import util.APIUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class GetGameServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        APIUtils.setResponseHeader(response);

        //1 - Get parameters:
        String gameToken = request.getParameter("gameToken");

        //3 - Required params:

        if (gameToken == null) {
            response.getWriter().write(new MissingParameterResponse("gameToken").toJSON());
            return;
        }

        //5 - Process request:
        Game game;

        final List<Game> games = ofy().load().type(Game.class).filter("token", gameToken).limit(1).list();
        if (games.size() < 1) {
            response.getWriter().write(new ErrorResponse("Game not found", "The game with token '" + gameToken + "' was not found.").toJSON());
            return;
        }

        game = games.get(0);

        StatelessGame statelessGame = StatelessGame.fromGame(game);

        response.getWriter().write(new GetGameResponse(statelessGame).toJSON());



    }
}
