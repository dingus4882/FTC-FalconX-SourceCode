package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.pedropathing.util.Timer;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

@TeleOp
public class pathingTest extends OpMode{
    private Follower follower;
    private Timer pathTimer, opModeTimer;

    public enum PathState {
        //START POSITION_END POSITION
        //DRIVE > MOVEMENT STATE
        //SHOOT > ATTEMPT TO SCORE THE ARTIFACT
        DRIVE_STARTPOS_SHOOT_POS,
        SHOOT_PRELOAD,
        DRIVE_SHOOTPOS_TURNPOS,
        DRIVE_TURNPOS_MOVEPOS,
        DRIVE_MOVEPOS_PARKPOS

    }

    PathState pathState;

    private final Pose startpose = new Pose(56, 8, Math.toRadians(90));
    private final Pose shootpose = new Pose(55.59690799396682, 33.7609822775264, Math.toRadians(90));
    private final Pose turnPose = new Pose(7.7, 18.181042609351426, Math. toRadians(180));

    private final Pose movepose = new Pose(7.576546003016591, 99.92236990950225, Math.toRadians(90));
    private final Pose parkPose = new Pose(8.14328808446456, 99.62443438914025, Math.toRadians(0));

    private PathChain driveStartPosShootPos, driveShootPosTurnPos, driveTurnPosMovePos, driveMovePosParkPos;

    public void buildPaths(){
        //put in coordinates for starting pose > ending pose
        driveStartPosShootPos = follower.pathBuilder()
                .addPath(new BezierLine(startpose, shootpose))
                .setLinearHeadingInterpolation(startpose.getHeading(), shootpose.getHeading())
                .build();
        driveShootPosTurnPos = follower.pathBuilder()
                .addPath(new BezierLine(shootpose, turnPose))
                .setLinearHeadingInterpolation(shootpose.getHeading(), turnPose.getHeading())
                .build();
                //TIMESTAMP: 25:26
        driveTurnPosMovePos = follower.pathBuilder()
                .addPath(new BezierLine(turnPose, movepose))
                .setLinearHeadingInterpolation(turnPose.getHeading(), movepose.getHeading())
                .build();
        driveMovePosParkPos = follower.pathBuilder()
                .addPath(new BezierLine(movepose, parkPose))
                .setLinearHeadingInterpolation(movepose.getHeading(), parkPose.getHeading())
                .build();

    }

    public void statePathUpdate(){
        switch(pathState){
            case DRIVE_STARTPOS_SHOOT_POS:
                follower.followPath(driveStartPosShootPos, true);
                setPathState(PathState.SHOOT_PRELOAD); //resets the timer & makes new state
                break;
            case SHOOT_PRELOAD:
                //check if follower did its path
                //check if 5 seconds have elapsed
                if (!follower.isBusy() && pathTimer.getElapsedTimeSeconds() > 5){
                    //TODO add logic to shooter
                    follower.followPath(driveShootPosTurnPos,true);
                    setPathState(PathState.DRIVE_SHOOTPOS_TURNPOS);
                    telemetry.addLine("Done Path 1");
                    //transition to next state
                }
                break;
            case DRIVE_SHOOTPOS_TURNPOS:
                //TURN
                if (!follower.isBusy()){
                    setPathState(PathState.DRIVE_TURNPOS_MOVEPOS);
                    telemetry.addLine("Done Path 2");
                }

            default:
                telemetry.addLine("No State Commanded");
                break;

        }
    }

    public void setPathState(PathState newState){
        pathState = newState;
        pathTimer.resetTimer();
    }
    //https://www.youtube.com/watch?v=gdkefs_VL-w&list=PLRHdgFNRLyaMYGJtBMSFEpxNdMvuJOai6&index=3
    //TIMESTAMP: 11:03

    @Override
    public void init(){
        pathState = PathState.DRIVE_STARTPOS_SHOOT_POS;
        pathTimer = new Timer();
        opModeTimer = new Timer();
        follower = Constants.createFollower(hardwareMap);
        //TODO add in any other init mechanisms

        buildPaths();
        follower.setPose(startpose);

    }

    public void start(){
        opModeTimer.resetTimer();
        setPathState(pathState);
    }

    @Override
    public void loop(){
        follower.update();
        statePathUpdate();

        telemetry.addData("Path state", pathState.toString());
        telemetry.addData("x", follower.getPose().getX());
        telemetry.addData("y", follower.getPose().getY());
        telemetry.addData("heading", follower.getPose().getHeading());
        telemetry.addData("Path time", pathTimer.getElapsedTimeSeconds());
        //Run IP address? http://192.168.43.1:8001/
    }
}
