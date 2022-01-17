module de.haynes.pomodoro {
    requires javafx.controls;
    requires javafx.fxml;
	requires javafx.base;

    opens de.haynes.pomodoro to javafx.fxml;
    exports de.haynes.pomodoro;
}
