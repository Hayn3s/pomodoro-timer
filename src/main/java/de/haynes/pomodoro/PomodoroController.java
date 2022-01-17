package de.haynes.pomodoro;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

public class PomodoroController implements Initializable {

	private static final long POMODORO_UNIT_MILLIS = 1000L * 60L * 25L;
	private static final long SHORT_BREAK_MILLIS = 1000L * 60L * 5L;
	private static final long LONG_BREAK_MILLIS = 1000L * 60L * 15L;
	private static final long TIMER_INTERVAL = 100L;

	@FXML
	private Button btStart;

	@FXML
	private RadioButton rbPomodoro;

	@FXML
	private RadioButton rbShortBreak;

	@FXML
	private RadioButton rbLongBreak;

	@FXML
	private Label lbTimer;

	@FXML
	private TextField tfTask;

	private ToggleGroup toggleGroup = new ToggleGroup();

	private LongProperty timerProperty = new SimpleLongProperty(POMODORO_UNIT_MILLIS);
	private BooleanProperty isRunningProperty = new SimpleBooleanProperty(false);

	private DateFormat df = new SimpleDateFormat("mm:ss");
	private long timerTarget;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		timerProperty.addListener(o -> lbTimer.setText(df.format(new Date(timerProperty.get()))));

		btStart.disableProperty().bind(isRunningProperty);
		
		rbPomodoro.setToggleGroup(toggleGroup);
		rbShortBreak.setToggleGroup(toggleGroup);
		rbLongBreak.setToggleGroup(toggleGroup);

		toggleGroup.selectedToggleProperty().addListener(o -> toggleMode());

		rbPomodoro.setSelected(true);

		rbPomodoro.disableProperty().bind(isRunningProperty);
		rbShortBreak.disableProperty().bind(isRunningProperty);
		rbLongBreak.disableProperty().bind(isRunningProperty);
		tfTask.disableProperty().bind(timerProperty.isEqualTo(POMODORO_UNIT_MILLIS).not());

	}

	private void toggleMode() {
		if (rbPomodoro.isSelected()) {
			timerProperty.set(POMODORO_UNIT_MILLIS);
		} else if (rbShortBreak.isSelected()) {
			timerProperty.set(SHORT_BREAK_MILLIS);
		} else if (rbLongBreak.isSelected()) {
			timerProperty.set(LONG_BREAK_MILLIS);
		}
	}

	@FXML
	private void startAction() {
		isRunningProperty.set(true);
		timerTarget = System.currentTimeMillis() + timerProperty.get();
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				long millisLeft = timerTarget - System.currentTimeMillis();
				if (millisLeft < 0L) {
					Platform.runLater(() -> isRunningProperty.set(false));
					timer.cancel();
				} else {
					Platform.runLater(() -> timerProperty.set(millisLeft));
				}
			}
		}, TIMER_INTERVAL, TIMER_INTERVAL);
    }
}
