package de.haynes.pomodoro;

import java.io.IOException;
import com.airhacks.afterburner.injection.Injector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        PomodoroView pomodoroView = new PomodoroView();
        Scene scene = new Scene(pomodoroView.getView());
        stage.setScene(scene);
        stage.setTitle("Pomodoro-Timer");
		stage.setOnCloseRequest(e -> {
			Platform.exit();
			System.exit(0);
		});
        stage.show();
        ((PomodoroPresenter) pomodoroView.getPresenter()).drawArrowsWithProgressIndication(0.0d);
    }

    @Override
    public void stop() throws Exception {
        Injector.forgetAll();
    }

    public static void main(String[] args) {
        launch(args);
    }

}