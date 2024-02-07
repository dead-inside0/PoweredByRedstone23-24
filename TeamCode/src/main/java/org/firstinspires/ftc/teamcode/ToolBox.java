package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.util.Range;

public class ToolBox {
    //converts the joystick angle (global) to the angle needed to move the robot (local) in that direction
    public static double joystickToRobot(double joystickAngle, double robotAngle){
        double localAngle = joystickAngle - robotAngle; //we MIGHT be fucked
        if(localAngle < 0){
            localAngle += 2*Math.PI;
        }
        return localAngle;
    }


    //returns motor powers needed to go in a specific angle
    public static double[] getMotorPowersByDirection(double targetDirectionAngle){
        targetDirectionAngle -= 90; //

        double[] motorPowers = {
                Math.sin(targetDirectionAngle - Math.PI / 4), //backleft
                Math.sin(targetDirectionAngle + Math.PI / 4), //backright
                Math.sin(targetDirectionAngle + Math.PI / 4), //frontleft
                Math.sin(targetDirectionAngle - Math.PI / 4)//frontright
        };
        return motorPowers;
    }

    //pythagoras theorem
    public static double pythagoras(double x, double y){
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    //returns the angle between two points
    public static double getAngleToPoint(double selfX, double selfY, double targetX, double targetY){
        return Math.atan2(selfX-targetX, selfY-targetY);
    }

    //returns motor powers to get to a certain point
    public static double[] getMotorPowersToPoint(double selfX, double selfY, double targetX, double targetY){
        return getMotorPowersByDirection(getAngleToPoint(selfX, selfY, targetX, targetY));
    }
}
