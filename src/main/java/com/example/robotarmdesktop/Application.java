package com.example.robotarmdesktop;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ResourceBundle;

public class Application extends javafx.application.Application {
    private static Application applicationInstance;
    private TCPSocket tcpSocket;

    @Override
    public void init() {
        applicationInstance = this;
    }

    @Override
    public void start(Stage stage) throws IOException {
        PropertiesManager propertiesManager = new PropertiesManager();
        ResourceBundle bundleLocalization = ResourceBundle.getBundle("localization_" + propertiesManager.getValue("application_settings.properties", "language"));

        FXMLLoader loader = new FXMLLoader();
        loader.setController("ConnectionSceneController");
        Parent root = loader.load(getClass().getClassLoader().getResource("connection_scene_view.fxml"), bundleLocalization);

        Scene scene = new Scene(root, 600, 400);
        stage.setTitle(bundleLocalization.getString("applicationStageTitle"));
        stage.setScene(scene);
        stage.show();
    }

    public void createTCPSocket(String ip, String port) {
        this.tcpSocket = new TCPSocket(ip, port);
    }

    public static Application getApplicationInstance() {
        return applicationInstance;
    }

    public static void main(String[] args) {
        launch();
    }
}