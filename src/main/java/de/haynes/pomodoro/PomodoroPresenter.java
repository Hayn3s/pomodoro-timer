package de.haynes.pomodoro;

import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.stage.Stage;

public class PomodoroPresenter implements Initializable {

    private static final long POMODORO_UNIT_MILLIS = 1000L * 60L * 25L;
    private static final long SHORT_BREAK_MILLIS = 1000L * 60L * 5L;
	private static final long LONG_BREAK_MILLIS = 1000L * 60L * 15L;
	private static final long TIMER_INTERVAL = 100L;

	@FXML
	private ToggleButton btStart;

	@FXML
	private ToggleButton btShortBreak;

	@FXML
	private ToggleButton btLongBreak;

	@FXML
	private Label lbTimer;

	@FXML
	private TextField tfTask;

	@FXML
	private VBox pnHistory;

    @FXML
    private Canvas canvas;

	private final LongProperty timerProperty = new SimpleLongProperty(POMODORO_UNIT_MILLIS);
	private final BooleanProperty isRunningProperty = new SimpleBooleanProperty(false);
	private final StringProperty taskPropery = new SimpleStringProperty();

	private final DateFormat dfCountDown = new SimpleDateFormat("mm:ss");
	private final DateFormat dfHistory = new SimpleDateFormat("HH:mm");
	private long timerTarget;
    private long currentTimerUnit;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		timerProperty.addListener(o -> lbTimer.setText(dfCountDown.format(new Date(timerProperty.get()))));
		tfTask.textProperty().bindBidirectional(taskPropery);

		btStart.disableProperty().bind(isRunningProperty.or(taskPropery.isEmpty()));
		btShortBreak.disableProperty().bind(isRunningProperty.or(taskPropery.isEmpty()));
		btLongBreak.disableProperty().bind(isRunningProperty.or(taskPropery.isEmpty()));
	}

    public void drawArrowsWithProgressIndication(double progress) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setLineWidth(3.0);

        Bounds btStartBounds = canvas.sceneToLocal(btStart.localToScene(btShortBreak.getBoundsInLocal()));
        Bounds btShortBreakBounds = canvas.sceneToLocal(btShortBreak.localToScene(btShortBreak.getBoundsInLocal()));

        double arcHeight = 120d;
        double arcOffsetY = 3d;
        double arrowOffsetY = 1d;

        gc.setStroke(progress >= 1 && btStart.isSelected() ? Color.GREEN : Color.BLACK);
        
        gc.strokeArc(btStartBounds.getCenterX(), btStartBounds.getMinY() - (arcHeight / 2.0) - arcOffsetY,
            btShortBreakBounds.getMaxX() - btStartBounds.getCenterX(), arcHeight, 0.0, 180.0,
            ArcType.OPEN);

        gc.strokeLine(btShortBreakBounds.getMaxX(), btStartBounds.getMinY() - arrowOffsetY,
            btShortBreakBounds.getMaxX() + 10d, btStartBounds.getMinY() - arrowOffsetY - 10d);

        gc.strokeLine(btShortBreakBounds.getMaxX(), btStartBounds.getMinY() - arrowOffsetY,
            btShortBreakBounds.getMaxX() - 10d, btStartBounds.getMinY() - arrowOffsetY - 10d);

        gc.setStroke(progress >= 1 && !btStart.isSelected() ? Color.GREEN : Color.BLACK);

        gc.strokeArc(btStartBounds.getCenterX(), btStartBounds.getMaxY() - (arcHeight / 2.0) + arcOffsetY,
            btShortBreakBounds.getMaxX() - btStartBounds.getCenterX(), arcHeight, 180.0, 180.0,
            ArcType.OPEN);

        gc.strokeLine(btStartBounds.getCenterX(), btStartBounds.getMaxY() + arrowOffsetY,
            btStartBounds.getCenterX() - 10d, btStartBounds.getMaxY() + arrowOffsetY + 10d);

        gc.strokeLine(btStartBounds.getCenterX(), btStartBounds.getMaxY() + arrowOffsetY,
            btStartBounds.getCenterX() + 10d, btStartBounds.getMaxY() + arrowOffsetY + 10d);

        gc.setStroke(Color.GREEN);
        if (progress > 0.0)
         {
             if (btStart.isSelected())
             {
                gc.strokeArc(btStartBounds.getCenterX(), btStartBounds.getMinY() - (arcHeight / 2.0) - arcOffsetY,
                     btShortBreakBounds.getMaxX() - btStartBounds.getCenterX(), arcHeight, 180.0, -180.0 * progress,
                     ArcType.OPEN);
             } else {
                 gc.strokeArc(btStartBounds.getCenterX(), btStartBounds.getMaxY() - (arcHeight / 2.0) + arcOffsetY,
                     btShortBreakBounds.getMaxX() - btStartBounds.getCenterX(), arcHeight, 0, -180.0 * progress,
                     ArcType.OPEN);
            }
        }
    }

    @FXML
	private void startAction() {
		startCountdown(POMODORO_UNIT_MILLIS);

	}

	@FXML
	private void shortBreakAction() {
		startCountdown(SHORT_BREAK_MILLIS);
	}

	@FXML
	private void longBreakAction() {
		startCountdown(LONG_BREAK_MILLIS);
	}

	private void startCountdown(long timeInMillis) {
		isRunningProperty.set(true);
		timerProperty.set(timeInMillis);
		timerTarget = System.currentTimeMillis() + timerProperty.get();
        currentTimerUnit = timerProperty.get();
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				long millisLeft = timerTarget - System.currentTimeMillis();
				if (millisLeft < 0L) {
                    Platform.runLater(() -> endCountdown());

					timer.cancel();
				} else {
					Platform.runLater(() -> tick(millisLeft));
				}
			}
		}, TIMER_INTERVAL, TIMER_INTERVAL);
	}

    private void tick(long millisLeft) {
        timerProperty.set(millisLeft);

        double progress = ((double) currentTimerUnit - millisLeft) / currentTimerUnit;

        drawArrowsWithProgressIndication(progress);
    }

    private void endCountdown() {
        tick(0L);
        isRunningProperty.set(false);
        if (btStart.isSelected()) {
            addHistoryEntry();
        }
        btStart.setSelected(false);
        btShortBreak.setSelected(false);
        btLongBreak.setSelected(false);

        Stage stage = (Stage) btStart.getScene().getWindow();
        if (!stage.isFocused()) {
            stage.hide();
        }
        stage.show();
    }

	private void addHistoryEntry() {
		Hyperlink hyperlink = new Hyperlink(
				MessageFormat.format("{0}h - {1}", dfHistory.format(new Date(timerTarget)), tfTask.getText()));
		hyperlink.setOnAction(e -> taskPropery.set(hyperlink.getText().substring("00:00h - ".length())));
		pnHistory.getChildren().add(hyperlink);
        pnHistory.requestLayout();
	}
}
