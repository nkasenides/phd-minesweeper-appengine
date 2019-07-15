package servlets;

import model.Game;
import model.Move;
import model.Session;
import model.response.MissingParameterResponse;
import respondx.ErrorResponse;
import util.APIUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class PlayServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        APIUtils.setResponseHeader(response);

        //1 - Get parameters:
        String sessionID = request.getParameter("sessionID");
        String moveStr = request.getParameter("move");
        String rowStr = request.getParameter("row");
        String colStr = request.getParameter("col");

        //3 - Required params:
        if (sessionID == null) {
            response.getWriter().write(new MissingParameterResponse("sessionID").toJSON());
            return;
        }

        if (moveStr == null) {
            response.getWriter().write(new MissingParameterResponse("move").toJSON());
            return;
        }

        if (rowStr == null) {
            response.getWriter().write(new MissingParameterResponse("row").toJSON());
            return;
        }

        if (colStr == null) {
            response.getWriter().write(new MissingParameterResponse("col").toJSON());
            return;
        }

        //Find session:
        final List<Session> sessionsList = ofy().load().type(Session.class).filter("sessionID", sessionID).limit(1).list();
        if (sessionsList.size() < 1) {
            response.getWriter().write(new ErrorResponse("Invalid session", "The session with ID '" + sessionID + "' does not exist.").toJSON());
            return;
        }

        final Session session = sessionsList.get(0);
        final Game game = ofy().load().key(session.getGameKey()).now();

        if (game == null) {
            response.getWriter().write(new ErrorResponse("Invalid game", "The game referenced by this session does not exist. It may have been deleted.").toJSON());
            return;
        }

        Move move;
        try {
            move = Move.valueOf(moveStr);
        } catch (IllegalArgumentException e) {
            response.getWriter().write(new ErrorResponse("Invalid move", "The move '" + moveStr + "' is not valid.").toJSON());
            return;
        }

        int row;
        int col;

        try {
            row = Integer.parseInt(rowStr);
            if (row < 0 || row > game.getGameSpecification().getHeight()) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            response.getWriter().write(new ErrorResponse("Invalid row", "The row '" + rowStr + "' is not valid. Rows must be within 0 and the game's height.").toJSON());
            return;
        }

        try {
            col = Integer.parseInt(colStr);
            if (col < 0 || col > game.getGameSpecification().getWidth()) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            response.getWriter().write(new ErrorResponse("Invalid column", "The column'" + rowStr + "' is not valid. Rows must be within 0 and the game's width.").toJSON());
            return;
        }

        switch (move) {
            case REVEAL:
                //TODO
                break;
            case FLAG_UNFLAG:
                //TODO
                break;
            case SHIFT:
                //TODO
                break;
        }


    }

}
