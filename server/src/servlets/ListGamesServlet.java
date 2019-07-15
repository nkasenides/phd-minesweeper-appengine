package servlets;

import model.Game;
import model.GameSpecification;
import model.GameState;
import model.StatelessGame;
import model.response.ListGamesResponse;
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

public class ListGamesServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        APIUtils.setResponseHeader(response);

        //1 - Get parameters:
        String startedOnlyStr = request.getParameter("startedOnly");

        boolean startedOnly = startedOnlyStr != null;

        //5 - Process request:
        List<Game> games;
        ArrayList<StatelessGame> statelessGames = new ArrayList<>();
        if (startedOnly) {
            games = ofy().load().type(Game.class).filter("gameState", "STARTED").list();
        }
        else {
            games = ofy().load().type(Game.class).list();
        }

        for (Game g : games) {
            statelessGames.add(new StatelessGame(g.getToken(), g.getGameSpecification(), g.getGameState()));
        }

        if (statelessGames.size() < 1) {
            response.getWriter().write(new SuccessResponse("Games fetched", "No games found").toJSON());
        }
        else {
            response.getWriter().write(new ListGamesResponse(statelessGames).toJSON());
        }

    }

}
