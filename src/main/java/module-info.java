module com.kingartur1000.javacourseproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    // Apache POI
    requires org.apache.poi.ooxml;
    requires org.apache.poi.poi;
    requires org.apache.commons.collections4;
    requires org.apache.commons.compress;
    requires org.apache.commons.io;
    requires org.apache.xmlbeans;

    opens com.kingartur1000.javacourseproject to javafx.fxml;
    exports com.kingartur1000.javacourseproject;
}