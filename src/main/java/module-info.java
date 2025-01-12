module com.svalero.ps_aa1 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires javafx.swing;

    opens com.svalero.ps_aa1 to javafx.fxml;
    exports com.svalero.ps_aa1;
    exports com.svalero.ps_aa1.controller;
    opens com.svalero.ps_aa1.controller to javafx.fxml;
}