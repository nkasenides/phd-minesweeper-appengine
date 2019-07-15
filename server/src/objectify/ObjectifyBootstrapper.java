package objectify;

import com.googlecode.objectify.ObjectifyService;
import model.Game;
import model.Session;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ObjectifyBootstrapper implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
            ObjectifyService.init();
            ObjectifyService.register(Game.class);
            ObjectifyService.register(Session.class);
            // etc...
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

}
