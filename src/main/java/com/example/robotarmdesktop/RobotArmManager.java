package com.example.robotarmdesktop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class RobotArmManager {
    private SocketManager socketManager = null;
    private ArrayList<Module> modules = new ArrayList<>();
    private Boolean isRecording = false;
    private ArrayList<String> recordingCommands = new ArrayList<>();

    public RobotArmManager() {
    }

    public void start(String ip, Integer port) {
        this.socketManager = new SocketManager(ip, port);

        Map robotArmConfiguration = this.getModulesConfiguration();
        Map currentModulesConfiguration = this.getCurrentModulesConfiguration();

        for (Object moduleIndex: robotArmConfiguration.keySet()) {
            Map configuration = (Map) robotArmConfiguration.get(moduleIndex);
            Map currentConfiguration = (Map) currentModulesConfiguration.get(moduleIndex);

            String id = configuration.get("id").toString();
            String type = configuration.get("type").toString();
            String minAngle = configuration.get("min_angle").toString();
            String maxAngle = configuration.get("max_angle").toString();
            String minSpeedLevel= configuration.get("min_speed_level").toString();
            String maxSpeedLevel= configuration.get("max_speed_level").toString();
            String currentAngle = currentConfiguration.get("angle").toString();
            String currentSpeedLevel = currentConfiguration.get("speed_level").toString();
            String angle = currentConfiguration.get("angle").toString();
            String speedLevel = currentConfiguration.get("speed_level").toString();

            this.modules.add(new Module(id, type, minAngle, maxAngle, minSpeedLevel, maxSpeedLevel, currentAngle, currentSpeedLevel, angle, speedLevel));
        }
    }

    public void sendManualControlCommand() {
        String modulesConfiguration = "";

        for (Module module: this.modules) {
            modulesConfiguration += "\"" + module.id + "\": {\"id\":" + module.id + ", \"angle\":" + module.angle + ", \"speed_level\":" + module.speedLevel + "}, ";
        }

        String command = "{\"socket\":\"tcp\", \"command_type\":\"control\", \"command\":\"manual_control\", \"data\":{" + modulesConfiguration.substring(0, modulesConfiguration.length() - 2) + "}}";

        if (this.isRecording) {
            String recordingCommand = "{\"command\": \"manual_control\", \"data\":{" + modulesConfiguration.substring(0, modulesConfiguration.length() - 2) + "}}";
            this.recordingCommands.add(recordingCommand);
        }

        this.socketManager.sendDataArrayPushBackTCPSocket(command);
    }

    public void sendNeuralNetworkControlCommand(String x, String y, String z, String alpha, String theta, String psi) {
        String command = "{\"socket\":\"tcp\", \"command_type\":\"control\", \"command\":\"neural_network_control\", \"data\":{\"x\":" + x + ", \"y\":" + y + ", \"z\":" + z + ", \"alpha\":" + alpha + ", \"theta\":" + theta + ", \"psi\":" + psi + "}}";

        if (this.isRecording) {
            String recordingCommand = "{\"command\": \"neural_network_control\", \"data\":{\"x\":" + x + ", \"y\":" + y + ", \"z\":" + z + ", \"alpha\":" + alpha + ", \"theta\":" + theta + ", \"psi\":" + psi + "}}";
            this.recordingCommands.add(recordingCommand);
        }

        this.socketManager.sendDataArrayPushBackTCPSocket(command);
    }

    public Map getModulesConfiguration() {
        String command = "{\"socket\":\"tcp\", \"command_type\":\"request\", \"command\":\"get_modules_configuration\", \"data\":{}}";
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

    public Map getCurrentModulesConfiguration() {
        String command = "{\"socket\":\"tcp\", \"command_type\":\"request\", \"command\":\"get_current_modules_configuration\", \"data\": {}}";
        this.socketManager.sendDataArrayPushBackTCPSocket(command);

        String data = "";

        while (Objects.equals(data, "")) {
            data = this.socketManager.receiveDataArrayByCommandTCPSocket("answer", "get_current_modules_configuration");
        }

        try {
            JsonMapper mapper = new JsonMapper();
            return (Map) mapper.readValue(data, Map.class).get("data");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Map getCurrentModuleConfigurationById(String id) {
        String command = "{\"socket\":\"tcp\", \"command_type\":\"request\", \"command\":\"get_current_module_configuration_by_id\", \"data\": {\"id\": " + id + "}}";
        this.socketManager.sendDataArrayPushBackTCPSocket(command);

        String data = "";

        while (Objects.equals(data, "")) {
            data = this.socketManager.receiveDataArrayByCommandTCPSocket("answer", "get_current_module_configuration_by_id");
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

    public void startRecordingScript() {
        this.isRecording = true;
        this.recordingCommands.clear();
    }

    public void finishRecordingScript() {
        this.isRecording = false;
    }

    public void runScript() {
        if (this.recordingCommands.size() > 0 && !this.isRecording) {
            String recordingCommands = "";

            for (int i = 0; i < this.recordingCommands.size(); ++i) {
                recordingCommands += "\"" + i + "\": " + this.recordingCommands.get(i) + ", ";
            }

            String command = "{\"socket\":\"tcp\", \"command_type\":\"control\", \"command\":\"script\", \"data\":{" + recordingCommands.substring(0, recordingCommands.length() - 2) + "}}";

            this.socketManager.sendDataArrayPushBackTCPSocket(command);
        }
    }

    public Boolean getTCPSocketState() {
        return this.socketManager.isTCPSocketWorkState();
    }

    public Boolean getUDPSocketState() {
        return this.socketManager.isUDPSocketWorkState();
    }
}