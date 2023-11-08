package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class OgHardwareMap {
    HardwareMap hdwMap = null;

    public DcMotor motor1 = null;
    public DcMotor motor2 = null;
    public DcMotor motor3 = null;
    public DcMotor motor4 = null;

    public void init(HardwareMap ahwMap){
        hdwMap = ahwMap;

        motor1 = hdwMap.get(DcMotor.class, "motor1");
        motor2 = hdwMap.get(DcMotor.class, "motor2");
        motor3 = hdwMap.get(DcMotor.class, "motor3");
        motor4 = hdwMap.get(DcMotor.class, "motor4");


    }
}