package de.hajnes.pomodoro;

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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.stage.Stage;

public class PomodoroPresenter implements Initializable {

    private static final long POMODORO_UNIT_MILLIS = 1000L * 60L * 25L;
    private static final long SHORT_BREAK_MILLIS = 1000L * 5L;
	private static final long LONG_BREAK_MILLIS = 1000L * 60L * 15L;

	private static final long TIMER_INTERVAL = 100L;
    private static final long REMINDER_TIMER_INTERVAL = 1000L * 60L * 3L;

	private static final double ARC_HEIGHT = 120d;
	private static final double HALF_ARC_HEIGHT = 120d / 2d;
	private static final double ARC_OFFSET_Y = 3d;
	private static final double ARROWHEAD_OFFSET_Y = 1d;

	@FXML
	private ToggleButton btStart;

	@FXML
	private ToggleButton btShortBreak;

	@FXML
	private ToggleButton btLongBreak;

    @FXML
    private ToggleButton btRemind;

    @FXML
    private ToggleButton btSound;

	@FXML
	private Label lbTimer;

	@FXML
	private TextField tfTask;

	@FXML
	private VBox pnHistory;

    @FXML
    private Canvas canvas;

    private MediaPlayer player;

	private final LongProperty timerProperty = new SimpleLongProperty(POMODORO_UNIT_MILLIS);
	private final BooleanProperty isRunningProperty = new SimpleBooleanProperty(false);
	private final StringProperty taskPropery = new SimpleStringProperty();
    private final BooleanProperty isRemindProperty = new SimpleBooleanProperty();
    private final BooleanProperty isSoundEnabledProperty = new SimpleBooleanProperty();

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

        isRemindProperty.bind(btRemind.selectedProperty());

        isSoundEnabledProperty.bind(btSound.selectedProperty());

        startUserReminderTask();
	}

    private void startUserReminderTask() {
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                if (Boolean.FALSE.equals(isRunningProperty.getValue()) && Boolean.TRUE.equals(isRemindProperty.getValue())) {
                    Platform.runLater(PomodoroPresenter.this::getUserAttention);
                }
            }
        }, REMINDER_TIMER_INTERVAL, REMINDER_TIMER_INTERVAL);
    }

    public void drawArrowsWithProgressIndication(double progress) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setLineWidth(3.0);

        Bounds btStartBounds = canvas.sceneToLocal(btStart.localToScene(btShortBreak.getBoundsInLocal()));
        Bounds btShortBreakBounds = canvas.sceneToLocal(btShortBreak.localToScene(btShortBreak.getBoundsInLocal()));

		double arcWidth = btShortBreakBounds.getMaxX() - btStartBounds.getCenterX();

        gc.setStroke(progress >= 1 && btStart.isSelected() ? Color.GREEN : Color.BLACK);

		drawHalfCircle(gc, btStartBounds.getCenterX(), btStartBounds.getMinY() - ARC_OFFSET_Y, arcWidth, 0d);
		drawArrowHead(gc, btShortBreakBounds.getMaxX(), btStartBounds.getMinY() - ARROWHEAD_OFFSET_Y, -10d);

        gc.setStroke(progress >= 1 && !btStart.isSelected() ? Color.GREEN : Color.BLACK);

		drawHalfCircle(gc, btStartBounds.getCenterX(), btStartBounds.getMaxY() + ARC_OFFSET_Y, arcWidth, 180d);
		drawArrowHead(gc, btStartBounds.getCenterX(), btStartBounds.getMaxY() + ARROWHEAD_OFFSET_Y, 10d);

        gc.setStroke(Color.GREEN);
		if (progress > 0.0 && progress < 1.0) {
			double arcExtendProgress = -180.0 * progress;
			if (btStart.isSelected()) {
				drawProgressCircle(gc, btStartBounds.getCenterX(), btStartBounds.getMinY() - ARC_OFFSET_Y, arcWidth,
						180d,
						arcExtendProgress);
			} else {
				drawProgressCircle(gc, btStartBounds.getCenterX(), btStartBounds.getMaxY() + ARC_OFFSET_Y, arcWidth,
						0d,
						arcExtendProgress);
			}
		}
    }

	private static void drawProgressCircle(GraphicsContext gc, double x, double y, double w,
			double startAngle,
			double extend) {
		gc.strokeArc(x, y - HALF_ARC_HEIGHT, w, ARC_HEIGHT, startAngle, extend,
            ArcType.OPEN);
	}

	private static void drawHalfCircle(GraphicsContext gc, double x, double y, double w, double startAngle) {
		drawProgressCircle(gc, x, y, w, startAngle, 180d);
	}

	private static void drawArrowHead(GraphicsContext gc, double x, double y, double yDirection) {
		gc.strokeLine(x, y, x + 10d, y + yDirection);
		gc.strokeLine(x, y, x - 10d, y + yDirection);
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

        getUserAttention();
    }

    private void getUserAttention() {
        Stage stage = (Stage) btStart.getScene().getWindow();
        if (!stage.isFocused()) {
            stage.hide();
        }
        stage.show();

        if (Boolean.TRUE.equals(isSoundEnabledProperty.getValue())) {
            Platform.runLater(() -> {
                Media song = new Media(getClass().getResource("ding_dong_dang.m4a").toExternalForm());
                player = new MediaPlayer(song);
                player.play();
            });
        }
    }

	private void addHistoryEntry() {
		Hyperlink hyperlink = new Hyperlink(
				MessageFormat.format("{0}h - {1}", dfHistory.format(new Date(timerTarget)), tfTask.getText()));
		hyperlink.setOnAction(e -> taskPropery.set(hyperlink.getText().substring("00:00h - ".length())));
		pnHistory.getChildren().add(hyperlink);
        pnHistory.requestLayout();
	}
}
