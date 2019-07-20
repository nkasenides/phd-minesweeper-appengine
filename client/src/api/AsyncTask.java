package api;

import com.sun.javafx.application.PlatformImpl;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

public abstract class AsyncTask {

    private static JFXPanel FX_PANEL_INIT;

    protected AsyncTask() {
        if (FX_PANEL_INIT == null) {
            FX_PANEL_INIT = new JFXPanel();
        }
    }

    protected abstract void onPreExecute();

    protected abstract void doInBackground();

    protected abstract void onPostExecute();

    public void execute() {
        onPreExecute();
        new Thread(new Runnable() {
            @Override
            public void run() {
                doInBackground();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        onPostExecute();
                        PlatformImpl.tkExit();
                    }
                });
            }
        }).start();
    }

}
