module de.haynes.pomodoro {
    requires javafx.controls;
    requires javafx.fxml;
	requires javafx.base;
    requires afterburner.fx;
    requires java.annotation;

    opens de.haynes.pomodoro to javafx.fxml;
}
