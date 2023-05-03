package com.example.robotarmdesktop;

public class Module {
    public String id = "";
    public String type = "";
    public String minAngel = "";
    public String maxAngel = "";
    public String minSpeedLevel = "";
    public String maxSpeedLevel = "";
    public String currentAngle = "";
    public String currentSpeedLevel = "";
    public String angle = "";
    public String speedLevel = "";

    public Module(
            String id, String type,
            String minAngele, String maxAngele,
            String minSpeedLevel, String maxSpeedLevel,
            String currentAngle, String currentSpeedLevel,
            String angle, String speedLevel
    ) {
        this.id = id;
        this.type = type;
        this.minAngel = minAngele;
        this.maxAngel = maxAngele;
        this.minSpeedLevel = minSpeedLevel;
        this.maxSpeedLevel = maxSpeedLevel;
        this.currentAngle = currentAngle;
        this.currentSpeedLevel = currentSpeedLevel;
        this.angle = angle;
        this.speedLevel = speedLevel;
    }
}
