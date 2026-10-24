package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.pedropathing.util.Timer;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp
public class pathingTest extends OpMode{
    private Follower follower;
    private Timer pathTimer, opModeTimer;

    public enum PathState {
        //START POSITION_END POSITION
        //DRIVE > MOVEMENT STATE
        //SHOOT > ATTEMPT TO SCORE THE ARTIFACT
        DRIVE_STARTPOS_SHOOT_POS,
        SHOOT_PRELOAD

    }

    PathState pathState;

    private final Pose startpose = new Pose(56, 8, Math.toRadians(90));
    private final Pose shootpose = new Pose(55.59690799396682, 33.7609822775264, Math.toRadians(90));

    private PathChain driveStartPosShootPos;
    //https://www.youtube.com/watch?v=gdkefs_VL-w&list=PLRHdgFNRLyaMYGJtBMSFEpxNdMvuJOai6&index=3
    //TIMESTAMP: 11:03

    @Override
    public void init(){

    }

    @Override
    public void loop(){

    }
}
