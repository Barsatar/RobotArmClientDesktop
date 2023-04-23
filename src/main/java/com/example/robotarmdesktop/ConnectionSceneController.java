package com.example.robotarmdesktop;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import java.io.IOException;
import java.util.ResourceBundle;

public class ConnectionSceneController {
    @FXML
    private Button connectionSceneConnectButton;
    @FXML
    private TextField connectionSceneIPAddressField;
    @FXML
    private TextField connectionScenePortField;
    private boolean isSaveData = false;

    @FXML
    void initialize() {
        PropertiesManager propertiesManager = new PropertiesManager();

        this.connectionSceneIPAddressField.setText(propertiesManager.getValue("connection_settings.properties", "ip"));
        this.connectionScenePortField.setText(propertiesManager.getValue("connection_settings.properties", "port"));
    }

    @FXML
    protected void updateScene() throws IOException {
        PropertiesManager propertiesManager = new PropertiesManager();
        ResourceBundle bundleLocalization = ResourceBundle.getBundle("localization_" + propertiesManager.getValue("application_settings.properties", "language"));

        FXMLLoader loader = new FXMLLoader();
        loader.setController("ConnectionSceneController");
        Parent root = loader.load(getClass().getClassLoader().getResource("connection_scene_view.fxml"), bundleLocalization);
        Stage stage = (Stage)this.connectionSceneConnectButton.getScene().getWindow();
        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void setLanguageRuButtonClick()  throws IOException {
        PropertiesManager propertiesManager = new PropertiesManager();
        propertiesManager.setValue("application_settings.properties", "language", "ru");

        this.updateScene();
    }

    @FXML
    protected void setLanguageEnButtonClick()  throws IOException {
        PropertiesManager propertiesManager = new PropertiesManager();
        propertiesManager.setValue("application_settings.properties", "language", "en");

        this.updateScene();
    }

    @FXML
    protected void connectButtonOnClick() throws IOException {
        if (this.isSaveData) {
            PropertiesManager propertiesManager = new PropertiesManager();
            propertiesManager.setValue("connection_settings.properties", "ip", this.connectionSceneIPAddressField.getText());
            propertiesManager.setValue("connection_settings.properties", "port", this.connectionScenePortField.getText());
        }
    }

    @FXML
    protected void saveDataCheckboxOnClick() throws IOException {
        this.isSaveData = !this.isSaveData;
    }
}
