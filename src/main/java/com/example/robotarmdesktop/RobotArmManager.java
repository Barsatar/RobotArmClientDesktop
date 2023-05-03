package com.example.robotarmdesktop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class RobotArmManager {
    private SocketManager socketManager = null;
    private ArrayList<Module> modules = new ArrayList<>();

    public RobotArmManager() {
    }

    public void start(String ip, Integer port) {
        this.socketManager = new SocketManager(ip, port);

        Map robotArmConfiguration = this.getRobotArmConfigurationFromServer();
        Map currentModulesConfiguration = this.getCurrentModulesConfigurationFromServer();

        for (Object moduleIndex: robotArmConfiguration.keySet()) {
            Map configuration = (Map) robotArmConfiguration.get(moduleIndex);
            Map currentConfiguration = (Map) currentModulesConfiguration.get(moduleIndex);

            String id = configuration.get("id").toString();
            String type = configuration.get("type").toString();
            String minAngle = configuration.get("min_angle").toString();
            String maxAngle = configuration.get("max_angle").toString();
            String minSpeedLevel= configuration.get("min_speed_level").toString();
            String maxSpeedLevel= configuration.get("max_speed_level").toString();
            String currentAngle = currentConfiguration.get("current_angle").toString();
            String currentSpeedLevel = currentConfiguration.get("current_speed_level").toString();
            String angle = currentConfiguration.get("current_angle").toString();
            String speedLevel = currentConfiguration.get("current_speed_level").toString();

            this.modules.add(new Module(id, type, minAngle, maxAngle, minSpeedLevel, maxSpeedLevel, currentAngle, currentSpeedLevel, angle, speedLevel));
        }
    }

    public Map getRobotArmConfigurationFromServer() {
        String command = "{\"socket\":\"tcp\", \"command_type\":\"request\", \"command\":\"get_robot_arm_configuration\", \"data\":{}}";
        this.socketManager.sendDataArrayPushBackTCPSocket(command);

        String data = "";

        while (Objects.equals(data, "")) {
            data = this.socketManager.receiveDataArrayByCommandTCPSocket("answer", "get_robot_arm_configuration");
        }

        try {
            JsonMapper mapper = new JsonMapper();
            return (Map) mapper.readValue(data, Map.class).get("data");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Map getCurrentModulesConfigurationFromServer() {
        String command = "{\"socket\":\"tcp\", \"command_type\":\"request\", \"command\":\"get_modules_configuration\", \"data\": {}}";
        this.socketManager.sendDataArrayPushBackTCPSocket(command);

        String data = "";

        while (Objects.equals(data, "")) {
            data = this.socketManager.receiveDataArrayByCommandTCPSocket("answer", "get_modules_configuration");
        }

        try {
            JsonMapper mapper = new JsonMapper();
            return (Map) mapper.readValue(data, Map.class).get("data");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Map getModuleConfigurationByIdFromServer(String id) {
        String command = "{\"socket\":\"tcp\", \"command_type\":\"request\", \"command\":\"get_module_configuration_by_id\", \"data\": {\"id\": " + id + "}}";
        this.socketManager.sendDataArrayPushBackTCPSocket(command);

        String data = "";

        while (Objects.equals(data, "")) {
            data = this.socketManager.receiveDataArrayByCommandTCPSocket("answer", "get_module_configuration_by_id");
        }

        try {
            JsonMapper mapper = new JsonMapper();
            return (Map) mapper.readValue(data, Map.class).get("data");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateModule(String id, String angle, String speedLevel) {
        for (int i = 0; i < this.modules.size(); ++i) {
            if (id == this.modules.get(i).id) {
                this.modules.get(i).angle = angle;
                this.modules.get(i).speedLevel = speedLevel;

                break;
            }
        }
    }

    public ArrayList<Module> getModules() {
        return this.modules;
    }
}
