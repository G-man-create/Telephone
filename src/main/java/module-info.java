module program.telephone {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.logging.log4j;
    opens program.telephone to javafx.fxml;
    exports program.telephone;
}