package org.firstinspires.ftc.teamcode.tests;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.AutonomousOpMode;

@Autonomous(name="AutonomousOpTest", group="Tests")
public class AutonomousOpTest extends AutonomousOpMode {

    double[][] path = {
            {0, 400, 0},
            {400, 400, 0},
            {400, 0, 0},
            {0, 0, 0}
    };
    @Override
    public double[][] getPath() {
        return path;
    }
}
