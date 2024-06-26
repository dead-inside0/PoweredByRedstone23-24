package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvInternalCamera2;
import org.openftc.easyopencv.OpenCvPipeline;

public class AutonomousOpMode_REDUNDANT extends LinearOpMode{

    final private ElapsedTime runtime = new ElapsedTime();

    public final double tile = 610;

    static double[][] path;

    double posX = 0;
    double posY = 0;
    double robotRotation = 0;
    OpenCvCamera phoneCam;

    Scalar highColorRed = new Scalar(10, 255, 255);
    Scalar lowColorRed = new Scalar(0, 150, 20);

    Scalar highColorBlue = new Scalar(120, 255, 255);
    Scalar lowColorBlue = new Scalar(110, 150, 20);

    /*  PATH:
            0-xPos
            1-yPos
            2-rot
            3-place motor
            4-place servo
            5-linear mechanism
            6-wait time
     */
    public Scalar[] colorByIndex(char color) {
        switch (color) {
            case 'r':
                return new Scalar[] {highColorRed,lowColorRed};
            case 'b':
                return new Scalar[] {highColorBlue,lowColorBlue};
        }
        return new Scalar[] {};
    }

    //Dummy functions which we override
    public double[][] getPath() {return new double[][]{{}};}

    public Scalar[] getColorBounds() {return new Scalar[]{};}

    public double[] getPlacementPosition(int elementLocation) {return new double[]{};}

    public void runOpMode() {
        runtime.reset();
        MyHardwareMap hMap = new MyHardwareMap(hardwareMap);

        DcMotor backLeftMotor = hMap.backLeftMotor;
        DcMotor backRightMotor = hMap.backRightMotor;
        DcMotor frontLeftMotor = hMap.frontLeftMotor;
        DcMotor frontRightMotor = hMap.frontRightMotor;

        Servo placeServo = hMap.placeServo;

        DcMotor linearMechanismMotor = hMap.linearMechanismMotor;
        linearMechanismMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        linearMechanismMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        DcMotor pickupMotor = hMap.pickUpMotor;

        DcMotor leftOdo = hMap.leftOdo;
        DcMotor middleOdo = hMap.middleOdo;
        DcMotor rightOdo = hMap.rightOdo;
        leftOdo.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        middleOdo.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightOdo.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftOdo.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        middleOdo.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightOdo.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        int passedContactsRightOdo = rightOdo.getCurrentPosition();
        int passedContactsLeftOdo = leftOdo.getCurrentPosition();
        int passedContactsMiddleOdo = middleOdo.getCurrentPosition();


        //OPENCV STUFFS
        //Get camera with live preview
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        phoneCam = OpenCvCameraFactory.getInstance().createInternalCamera2(OpenCvInternalCamera2.CameraDirection.BACK, cameraMonitorViewId);

        Scalar[] colorBounds = getColorBounds();

        //Set pipeline for frame processing
        ColorDetectionPipeline pipeline = new ColorDetectionPipeline(colorBounds[0], colorBounds[1]);
        phoneCam.setPipeline(pipeline);

        path = getPath();
        //Open camera
        phoneCam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                //Start streaming
                phoneCam.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT);
                telemetry.addLine("Camera initialized, ready to start");
                telemetry.update();
            }

            @Override
            public void onError(int errorCode) {
                telemetry.addLine("ERROR: Camera could not be accessed");
                telemetry.update();
            }
        });


        waitForStart();
        runtime.reset();

        while (runtime.seconds() <= 10) {
            telemetry.addData("Waiting:%d", 10 - runtime.seconds());
            telemetry.update();
        }


        int elementLocation;
        double runningSum = 0;
        int framesProcessed = 0;
        runtime.reset();
        //Average the pixel location over 1 second
        while (runtime.seconds() < 1 && opModeIsActive()) {
            runningSum += pipeline.getLastResult();
            framesProcessed++;
        }
        elementLocation = (int) Math.round(runningSum / framesProcessed);

        //Stop the camera
        phoneCam.stopStreaming();

        //Get first movement based on where pixel is
        path[0] = getPlacementPosition(elementLocation);

        //loop over path
        for (int i = 0; i < path.length; i++) {
            //get current point
            double[] point = path[i];

            telemetry.addData("Next point: ", "X: %f, Y: %f, R: %f", point[0], point[1], point[2] / Math.PI);
            telemetry.addData("Current position: ", "X: %f, Y: %f, R: %f", posX, posY, robotRotation / Math.PI);
            telemetry.update();

            if (point[5] == 1) {
                placeServo.setPosition(0.65);
                while (linearMechanismMotor.getCurrentPosition() < 0 && opModeIsActive()) {
                    linearMechanismMotor.setPower(1);
                }
                placeServo.setPosition(1);
            }

            //Go to position specified in point
            while (!(ToolBox.pythagoras(point[0] - posX, point[1] - posY) < ToolBox.movementTolerance && Math.abs(point[2] - robotRotation) < ToolBox.rotateTolerance) && opModeIsActive()) {
                int deltaContactsLeftOdo = leftOdo.getCurrentPosition() - passedContactsLeftOdo;
                int deltaContactsRightOdo = rightOdo.getCurrentPosition() - passedContactsRightOdo;
                int deltaContactsMiddleOdo = middleOdo.getCurrentPosition() - passedContactsMiddleOdo;

                //Update passed odo contacts
                passedContactsRightOdo += deltaContactsRightOdo;
                passedContactsLeftOdo += deltaContactsLeftOdo;
                passedContactsMiddleOdo += deltaContactsMiddleOdo;

                //Get position change
                double[] positionChange = Odometry.getPositionChange(-deltaContactsRightOdo, deltaContactsLeftOdo, -deltaContactsMiddleOdo, robotRotation);
                double deltaX = positionChange[0];
                double deltaY = positionChange[1];
                double deltaRotation = positionChange[2];

                //Update position
                posX += deltaX;
                posY += deltaY;
                robotRotation = ToolBox.scaleAngle(deltaRotation);

                //Go to position specified in point
                double[] motorPowers = ToolBox.getMotorPowersToPoint(posX, posY, point[0], point[1], robotRotation, point[2], 0.7);
                backLeftMotor.setPower(motorPowers[0]);
                backRightMotor.setPower(motorPowers[1]);
                frontLeftMotor.setPower(motorPowers[2]);
                frontRightMotor.setPower(motorPowers[3]);


                telemetry.addData("Next point: ", "X: %f, Y: %f, R: %f", point[0], point[1], point[2]);
                telemetry.addData("Current position: ", "X: %f, Y: %f, R: %f", posX, posY, robotRotation);
                telemetry.addData("%d", point[3]);
                telemetry.update();
            }

            //Move linear mechanism
            if (point[5] == -1) {
                placeServo.setPosition(0.65);
                while (linearMechanismMotor.getCurrentPosition() > -2000 && opModeIsActive()) {
                    linearMechanismMotor.setPower(-1);
                }
                placeServo.setPosition(0);
            }

            //Place pixel on ground
            //pickupMotor.setPower(point[3]!=0?1:0);
            pickupMotor.setPower(point[3]);

            //If wait - rerun the current position over and over
            double waitStart = runtime.milliseconds();
            while (runtime.milliseconds() < waitStart + point[6] * 1000 && opModeIsActive()) {
                telemetry.addData("waiting for", runtime.seconds() - waitStart);
                telemetry.update();
            }
        }
    }
}
