package com.example.robotarmdesktop;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SoftwareControlSceneController {
    @FXML
    private MenuButton softwareSceneManualControlModuleMenuButton;
    @FXML
    private Label softwareSceneManualControlAngleLabel;
    @FXML
    private Label softwareSceneManualControlAngleMinLabel;
    @FXML
    private Label softwareSceneManualControlAngleMaxLabel;
    @FXML
    private Slider softwareSceneManualControlAngleSlider;
    @FXML
    private Label softwareSceneManualControlSpeedLevelLabel;
    @FXML
    private Label softwareSceneManualControlSpeedLevelMinLabel;
    @FXML
    private Label softwareSceneManualControlSpeedLevelMaxLabel;
    @FXML
    private Slider softwareSceneManualControlSpeedLevelSlider;
    @FXML
    private Label  softwareSceneManualControlConstructionTypeLabel;
    @FXML
    private ImageView softwareControlManualControlConstructionTypeImageView;
    private Module currentModule;

    @FXML
    void initialize() {
        PropertiesManager propertiesManager = new PropertiesManager();
        ResourceBundle bundleLocalization = ResourceBundle.getBundle("localization_" + propertiesManager.getValue("application_settings.properties", "language"));

        ArrayList<Module> modules = Application.getRobotArmManager().getModules();

        for (Module module: modules) {
            MenuItem menuItem = new MenuItem(bundleLocalization.getString("softwareControlSceneManualControlModuleMenuButton") + " " + module.id);
            menuItem.setId("softwareSceneManualControlModule" + module.id + "MenuItem");
            menuItem.setOnAction(this::menuItemOnClick);
            this.softwareSceneManualControlModuleMenuButton.getItems().add(menuItem);
        }

        this.softwareSceneManualControlAngleSlider.setOnMouseDragged(this::angleSliderOnDrag);
        this.softwareSceneManualControlSpeedLevelSlider.setOnMouseDragged(this::speedLevelSliderOnDrag);

        if (modules.size() > 0) {
            this.updateManualControlUI(modules.get(0).id);
        }
    }

    @FXML
    public void menuItemOnClick(ActionEvent event) {
        String id = event.getSource().toString().replace("MenuItem[id=softwareSceneManualControlModule", "");
        id = id.replace("MenuItem, styleClass=[menu-item]]", "");

        this.updateCurrentModule();
        this.updateManualControlUI(id);
    }

    public void updateManualControlUI(String id) {
        PropertiesManager propertiesManager = new PropertiesManager();
        ResourceBundle bundleLocalization = ResourceBundle.getBundle("localization_" + propertiesManager.getValue("application_settings.properties", "language"));

        Module module = null;
        ArrayList<Module> modules = Application.getRobotArmManager().getModules();

        for (int i = 0; i < modules.size(); ++i) {
            if (id.equals(modules.get(i).id)) {
                module = modules.get(i);
                break;
            }
        }

        this.currentModule = module;

        this.softwareSceneManualControlModuleMenuButton.setText(bundleLocalization.getString("softwareControlSceneManualControlModuleMenuButton") + " " + this.currentModule.id);
        this.softwareSceneManualControlAngleLabel.setText(bundleLocalization.getString("softwareControlSceneManualControlAngleLabel") + " " + this.currentModule.angle);
        this.softwareSceneManualControlAngleMinLabel.setText(this.currentModule.minAngel);
        this.softwareSceneManualControlAngleMaxLabel.setText(this.currentModule.maxAngel);
        this.softwareSceneManualControlSpeedLevelLabel.setText(bundleLocalization.getString("softwareControlSceneManualControlSpeedLevelLabel") + " " + this.currentModule.speedLevel);
        this.softwareSceneManualControlSpeedLevelMinLabel.setText(this.currentModule.minSpeedLevel);
        this.softwareSceneManualControlSpeedLevelMaxLabel.setText(this.currentModule.maxSpeedLevel);
        this.softwareSceneManualControlConstructionTypeLabel.setText(bundleLocalization.getString("softwareControlSceneManualControlConstructionTypeLabel") + " " + this.currentModule.type.replace("type_", ""));

        File file = new File("src/main/resources/images/" + this.currentModule.type + ".png");
        Image constructionType = new Image(file.toURI().toString());
        this.softwareControlManualControlConstructionTypeImageView.setImage(constructionType);

        this.softwareSceneManualControlAngleSlider.setMin(Double.parseDouble(this.currentModule.minAngel));
        this.softwareSceneManualControlAngleSlider.setMax(Double.parseDouble(this.currentModule.maxAngel));
        this.softwareSceneManualControlAngleSlider.setValue(Double.parseDouble(this.currentModule.angle));

        this.softwareSceneManualControlSpeedLevelSlider.setMin(Double.parseDouble(this.currentModule.minSpeedLevel));
        this.softwareSceneManualControlSpeedLevelSlider.setMax(Double.parseDouble(this.currentModule.maxSpeedLevel));
        this.softwareSceneManualControlSpeedLevelSlider.setValue(Double.parseDouble(this.currentModule.speedLevel));
    }

    public void updateCurrentModule() {
        this.currentModule.angle = String.valueOf(Math.round(this.softwareSceneManualControlAngleSlider.getValue()));
        this.currentModule.speedLevel = String.valueOf(Math.round(this.softwareSceneManualControlSpeedLevelSlider.getValue()));

        Application.getRobotArmManager().updateModule(this.currentModule.id, this.currentModule.angle, this.currentModule.speedLevel);
    }

    @FXML
    public void angleSliderOnDrag(MouseEvent mouseEvent) {
        PropertiesManager propertiesManager = new PropertiesManager();
        ResourceBundle bundleLocalization = ResourceBundle.getBundle("localization_" + propertiesManager.getValue("application_settings.properties", "language"));

        this.softwareSceneManualControlAngleLabel.setText(bundleLocalization.getString("softwareControlSceneManualControlAngleLabel") + " " + Math.round(this.softwareSceneManualControlAngleSlider.getValue()));
    }

    public void speedLevelSliderOnDrag(MouseEvent mouseEvent) {
        PropertiesManager propertiesManager = new PropertiesManager();
        ResourceBundle bundleLocalization = ResourceBundle.getBundle("localization_" + propertiesManager.getValue("application_settings.properties", "language"));

        this.softwareSceneManualControlSpeedLevelLabel.setText(bundleLocalization.getString("softwareControlSceneManualControlSpeedLevelLabel") + " " + Math.round(this.softwareSceneManualControlSpeedLevelSlider.getValue()));
    }
}
