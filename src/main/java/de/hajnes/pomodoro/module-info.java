module de.haynes.pomodoro {
    requires javafx.controls;
    requires javafx.fxml;
	requires javafx.base;
    requires afterburner.fx;
    requires java.annotation;
    requires javafx.media;

    opens de.hajnes.pomodoro to javafx.fxml;
}
