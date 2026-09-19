package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

///------------MOTOR CONFIGS-------------
import com.qualcomm.robotcore.hardware.Servo;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

///-----------APRIL TAG CONFIGS----------
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

import java.util.List;

@TeleOp(name = "mergeTest")
public class mergeTest extends LinearOpMode{
    private Limelight3A webcam1;
    private IMU imu;

    //MECHANICS
    // --- SERVOS ---

    private Servo SR2;     // Positional servo (0..180 deg)

    // --- MOTORS ---
    private DcMotorEx INTAKE;
    private DcMotorEx LN, LN2;
    private DcMotor RL, RR, FL, FR;
    private DcMotor SR;
    // --- SR2 (+75 / -75 degree control) ---
    final double SR2_STEP = 75.0 / 180.0;     // ≈ 0.4167
    private double turnSpeed;
    // --- TURN SLOWDOWN (rotation only) ---
    private static double TURN_SCALE = 0.5; // 0.25 slower, 0.5 medium, 1.0 original
    @Override
    public void runOpMode(){
        double sr2Pos = 0.0;    // start at ~0°
        // --- DRIVE MOTORS ---
        RL = hardwareMap.get(DcMotor.class, "RL");
        RR = hardwareMap.get(DcMotor.class, "RR");
        FL = hardwareMap.get(DcMotor.class, "FL");
        FR = hardwareMap.get(DcMotor.class, "FR");

        FL.setDirection(DcMotor.Direction.REVERSE);
        RL.setDirection(DcMotor.Direction.REVERSE);

        RL.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        RR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        FL.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        FR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // --- LAUNCHER MOTORS ---
        LN = hardwareMap.get(DcMotorEx.class, "LN");
        LN2 = hardwareMap.get(DcMotorEx.class, "LN2");

        LN.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        LN2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        LN.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        LN2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        LN.setDirection(DcMotorSimple.Direction.FORWARD);
        LN2.setDirection(DcMotorSimple.Direction.REVERSE);

        // --- INTAKE ---
        INTAKE = hardwareMap.get(DcMotorEx.class, "INTAKE");
        INTAKE.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        INTAKE.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        INTAKE.setDirection(DcMotorSimple.Direction.FORWARD);

        // --- SERVOS ---
        SR2 = hardwareMap.get(Servo.class, "SR2");
        SR = hardwareMap.get(DcMotor.class, "SR");

        SR.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        SR.setDirection(DcMotor.Direction.FORWARD);
        SR2.setPosition(sr2Pos);   // start at ~90°

        telemetry.addData("Status", "Ready");
        telemetry.update();

        //LIMELIGHT INIT (help)
        webcam1 = hardwareMap.get(Limelight3A.class, "webcam1");
        webcam1.pipelineSwitch(8);
        imu = hardwareMap.get(IMU.class, "imu");
        RevHubOrientationOnRobot revHubOrientationOnRobot = new RevHubOrientationOnRobot(RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD);
        imu.initialize(new IMU.Parameters(revHubOrientationOnRobot));

        waitForStart();
        while (opModeIsActive()) {
            webcam1.start();
            // --- MECANUM DRIVE ---
            double y = -gamepad1.left_stick_y;
            double x = gamepad1.left_stick_x;

            // rotation scaled down ONLY (drive speed unchanged)+
            double r = gamepad1.right_stick_x * TURN_SCALE;

            //------TURN SPEED------
            //DO NOT HOLD DOWN FOR TOO LONG
            if (gamepad1.dpadLeftWasPressed()){
                TURN_SCALE = 0.15;
            }
            else if (gamepad1.dpadLeftWasReleased()){
                TURN_SCALE = 0.5;
            }


            // (optional but recommended) normalize so powers stay within [-1, 1]
            double fl = y + x + r;
            double fr = y - x - r;
            double rl = y - x + r;
            double rr = y + x - r;

            double max = Math.max(Math.abs(fl),
                    Math.max(Math.abs(fr),
                            Math.max(Math.abs(rl), Math.abs(rr))));

            if (max > 1.0) {
                fl /= max; fr /= max; rl /= max; rr /= max;
            }

            FL.setPower(fl);
            FR.setPower(fr);
            RL.setPower(rl);
            RR.setPower(rr);

            // --- INTAKE ---
            if (gamepad1.a) {
                INTAKE.setPower(0.7);
            } else {
                INTAKE.setPower(0);
            }

            //------------ INTAKE JAM FIX -------------------
            if (gamepad1.left_bumper){
                INTAKE.setDirection(DcMotorSimple.Direction.REVERSE);
                INTAKE.setPower(0.7);
            }
            else {
                INTAKE.setDirection(DcMotorSimple.Direction.FORWARD);
            }

            // -------- JAM FIX (reverse launcher direction) -------------
            boolean jamFix = gamepad2.right_bumper;
            if (jamFix) {
                LN.setDirection(DcMotorSimple.Direction.REVERSE);
                LN2.setDirection(DcMotorSimple.Direction.FORWARD);
            } else {
                LN.setDirection(DcMotorSimple.Direction.FORWARD);
                LN2.setDirection(DcMotorSimple.Direction.REVERSE);
            }

            // --- LAUNCHER (two controllers) ---
            boolean launchGp1 = gamepad1.right_trigger > 0.5; // main driver
            boolean launchGp2 = gamepad2.right_trigger > 0.5; // second controller (R2)
            boolean Gp3 = gamepad1.left_trigger  > 0.5; //first controller

            if (launchGp2) {
                LN.setPower(0.7);
                LN2.setPower(0.7);
            } else if (launchGp1) {
                LN.setPower(0.65);
                LN2.setPower(0.65);
            } else {
                LN.setPower(0);
                LN2.setPower(0);
            }

            // ---- SR (JAM FIX MOTOR) ---
            if (Gp3) {
                SR.setDirection(DcMotor.Direction.FORWARD);
                SR.setPower(1.0);
            } else if (gamepad1.b) {
                SR.setDirection(DcMotor.Direction.REVERSE);
                SR.setPower(1.0);
            } else {
                SR.setPower(0.0);
            }

            // --- SR2 (POSITION SERVO ±75°) ---
            boolean ltPressed = gamepad2.left_trigger > 0.5; // (note: also used for jamFix)

            if (ltPressed) {
                sr2Pos = Math.min(1.0, sr2Pos + SR2_STEP);
            }

            if (gamepad2.left_bumper) {
                sr2Pos = Math.max(0.0, sr2Pos - SR2_STEP);
            }

            SR2.setPosition(sr2Pos);

            // --- TELEMETRY ---
            /*
            telemetry.addData("Turn Scale", TURN_SCALE);
            telemetry.addData("Launcher GP1", launchGp1);
            telemetry.addData("Launcher GP2", launchGp2);
            telemetry.addData("JamFix", jamFix);
            telemetry.addData("SR1 Power", SR.getPower());
            telemetry.addData("SR2 Position", sr2Pos);
            telemetry.addData("SR2 Degrees", sr2Pos * 180.0);
            telemetry.update();
             */

            //PROCESSOR
            YawPitchRollAngles orientation = imu.getRobotYawPitchRollAngles();
            webcam1.updateRobotOrientation(orientation.getYaw());
            LLResult llResult = webcam1.getLatestResult();
            if (llResult != null && llResult.isValid()) {
                Pose3D botPose = llResult.getBotpose_MT2();
                telemetry.addData("Tx", llResult.getTx()); //minimum tx is -22.75; max is 23.35
                //range: 2.0-2.0
                telemetry.addData("Ty", llResult.getTy());// max is 13.4; min is 4.7 (lowest i could possibly make it)
                //range: 7.2-5.2
                telemetry.addData("Ta", llResult.getTa());// max is 8.64; min is 0.63
                telemetry.addData("Distance", llResult.getBotposeAvgDist());
                //range: 3.0-4.0
                List<LLResultTypes.FiducialResult> fiducials = llResult.getFiducialResults();
                if (fiducials.isEmpty()) {
                    telemetry.addLine("No AprilTags");
                }
                else {
                    for (LLResultTypes.FiducialResult tag : fiducials) {
                        telemetry.addData("Target Space", tag.getCameraPoseTargetSpace());//tells the space based on the april tag
                        telemetry.addData("Tag ID", tag.getFiducialId());
                        if ((llResult.getTx() >= -10.0 && llResult.getTx() <= 10.0) && (llResult.getTy() <= 8.0 && llResult.getTy() >= 5.2) && (llResult.getTa() >= 0.0 && llResult.getTa() <= 4.0)) {
                            telemetry.addData("Shooting Status", "Safe to shoot");
                        }
                        else{
                            telemetry.addData("Shooting Status", "Not safe to shoot");
                        }


                        /*if (gamepad1.yWasPressed()) {
                            TURN_SCALE = 0.5 * llResult.getTx();
                        }
                        else if (gamepad1.yWasReleased()){
                            TURN_SCALE = 0.5;
                        }

                         */
                        //------------SELF FIX---------------
                        /*
                        if (gamepad1.xWasPressed()){
                            if ((llResult.getTx() <= -10.0)){
                                left();
                                sleep(500);
                                stop();
                            }
                            if ((llResult.getTx() >= 10.0)){
                                right();
                                sleep(500);
                                stop();
                            }
                            if ((llResult.getTy() <= 5.2)){
                                reverse();
                                sleep(500);
                                stop();
                            }
                            if(llResult.getTy() >= 8.0){
                                forward();
                                sleep(500);
                                stop();
                            }
                        }

                         */
                        fiducials.clear();
                    }
                }
            }
        }

    }
    private void reverse() {
        FL.setPower(-0.5);
        RL.setPower(-0.5);
        FR.setPower(-0.5);
        RR.setPower(-0.5);
    }
    private void forward() {
        FL.setPower(0.5);
        RL.setPower(0.5);
        FR.setPower(0.5);
        RR.setPower(0.5);
    }

    private void right() {
        FL.setPower(0.5);
        RL.setPower(0.5);
        FR.setPower(-0.5);
        RR.setPower(-0.5);
    }

    private void left() {
        FL.setPower(-0.5);
        RL.setPower(-0.5);
        FR.setPower(0.5);
        RR.setPower(0.5);
    }
    private void robotStop(){
        FL.setPower(0.0);
        RL.setPower(0.0);
        FR.setPower(0.0);
        RR.setPower(0.0);
    }
}