package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp
public class Gates extends LinearOpMode {

    // ---- Settings (Gate Test shows the right values) ----

    /**
     * The servo's full range in degrees, only for showing angles on screen.
     * Smart Servo:180
     */
    final double SR_STEP = 90.0 / 180;


    // ---- State ----

    private Servo pollen, nectar;


    @Override
    public void runOpMode(){
        double srPos = 0.0;
        pollen = hardwareMap.get(Servo.class, "pollengate");
        nectar = hardwareMap.get(Servo.class, "nectargate");

        waitForStart();

        while (opModeIsActive()){
            pollen.setPosition(srPos);
            nectar.setPosition(srPos);
        }
    }



}