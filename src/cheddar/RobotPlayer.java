package cheddar;

import battlecode.common.*;

import static cheddar.Archon.*;
import static cheddar.FastMath.initRand;
import static cheddar.FastMath.rand256;
import static cheddar.FastMath.xsin;

public strictfp class RobotPlayer {
    static RobotController rc;

    static MapLocation myLocation;

    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions
        // from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        initRand(rc);
        // Here, we've separated the controls into a different method for each
        // RobotType.
        // You can add the missing ones or rewrite this into your own control
        // structure.

        switch (rc.getType()) {
            case ARCHON:
                runArchon();
                break;
            case GARDENER:
                runGardener();
                break;
            case SOLDIER:
                runSoldier();
                break;
            case LUMBERJACK:
                runLumberjack();
                break;
            case TANK:
                runTank();
                break;
            case SCOUT:
                runScout();
                break;
        }
    }
    static void runTank() throws GameActionException
    {
        Tank tank = new Tank(rc);
        tank.run();
    }

    static void runScout() throws GameActionException
    {
        Scout scout = new Scout(rc);
        scout.run();
    }

    static void runArchon() throws GameActionException {
        Archon archon = new Archon(rc);
        archon.run();
    }

    static void runGardener() throws GameActionException {
        Gardener gardener = new Gardener(rc);
        gardener.run();
    }

    static void runSoldier() throws GameActionException {
        Soldier soldier = new Soldier(rc);
        soldier.run();
    }

    static void runLumberjack() throws GameActionException {
        Lumberjack l = new Lumberjack(rc);
        l.run();
    }

}