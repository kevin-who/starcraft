package cheddar;

import battlecode.common.*;

import static cheddar.FastMath.rand256;
import static cheddar.Global.*;

/**
 * Created by kevinhu on 1/12/17.
 */
public class Gardener {
    static RobotController rc;

    public Gardener(RobotController rc)
    {
        Gardener.rc = rc;
    }

    MapLocation myLocation;

    public void run() {
        System.out.println("GARDENER");
        Team enemy = rc.getTeam().opponent();
        boolean tree_hub = false;
        Direction dir = randomDirection();

        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your
            // robot to explode
            try {
                if (rc.senseNearbyRobots(5f).length <= 1 && rc.canPlantTree(dir)
                        && rc.canPlantTree(dir.rotateLeftRads(60)) && rc.canPlantTree(dir.rotateLeftRads(120))
                        && rc.canPlantTree(dir.rotateLeftRads(180)) && rc.canPlantTree(dir.rotateLeftRads(240))
                        && rc.canPlantTree(dir.rotateLeftRads(300)))
                    break;

                if (rc.getTeamBullets() > 50 && !rc.hasMoved())
                    if (!tryMove(rc,dir, 0, 0)) {
                        dir = randomDirection();
                    }

                // Clock.yield() makes the robot wait until the next turn, then
                // it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }

        // The code you want your robot to perform every round should be in this
        // loop
        TreeInfo[] ti;
        while (true) {


            // Try/catch blocks stop unhandled exceptions, which cause your
            // robot to explode
            try {
                int round = rc.getRoundNum();

                //
                // float archon_dist = 0;
                //
                // RobotInfo[] near_bots =
                // rc.senseNearbyRobots(-1,rc.getTeam());
                ////
                // for (int x = 0; x < near_bots.length; x++) {
                // RobotInfo this_bot = near_bots[x];
                // if(this_bot.getType()==RobotType.ARCHON){
                // float distance =
                // myLocation.distanceSquaredTo(this_bot.location);
                // if(distance<archon_dist){
                // archon_dist = distance;
                // }
                // }
                // }

                myLocation = rc.getLocation();

                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

                if (robots.length > 0) {
                    MapLocation enemyLocation = robots[0].getLocation();
                    rc.broadcast(2, rc.getRoundNum());
                    rc.broadcast(3, (int) enemyLocation.x);
                    rc.broadcast(4, (int) enemyLocation.y);

                }

                ti = rc.senseNearbyTrees(-1, rc.getTeam());

                int close_trees = 0;
                int far_trees = 0;
                int neutral_trees = rc.senseNearbyTrees(-1, Team.NEUTRAL).length;

                for (int x = 0; x < ti.length; x++) {
                    TreeInfo this_tree = ti[x];
                    float distance_to = myLocation.distanceSquaredTo(this_tree.location);
                    if (distance_to < 5) {
                        close_trees++;
                    } else if (distance_to < 30) {
                        far_trees++;
                    }
                }

                float min = 50f;
                for (TreeInfo t : ti) {
                    if (t.health < min) {
                        min = t.health;
                    }
                    if (t.health < 40 && rc.canWater(t.ID))
                        rc.water(t.ID);
                }

                if (neutral_trees > 2 && rc.isBuildReady()) {
                    for(int x = 0; x < 12; x++) {
                        dir = dir.rotateLeftDegrees(30);
                        if(rc.canBuildRobot(RobotType.LUMBERJACK, dir)){
                            rc.buildRobot(RobotType.LUMBERJACK, dir);
                            System.out.println("DESTROY THE FOREST");
                        }
                    }
                }

                if (close_trees == 0 && far_trees == 0 && rc.canPlantTree(dir)) {
                    tree_hub = true;
                    if (rc.canBuildRobot(RobotType.LUMBERJACK, dir)) {
                        rc.buildRobot(RobotType.LUMBERJACK, dir);
                    }
                }

                if (tree_hub == true && close_trees < 5 && rc.canPlantTree(dir)) {
                    rc.plantTree(dir);
                    dir = dir.rotateLeftDegrees(60);
                    close_trees++;

                } else {
                    if (tree_hub == false&&rc.canBuildRobot(RobotType.LUMBERJACK, dir) && round<100 && rc.isBuildReady() && rand256()<128) {
                        rc.buildRobot(RobotType.LUMBERJACK, dir);
                    }
                    else if (tree_hub == false&& rand256()>128) {
                        tryMove(rc,randomDirection());
                    } else {
                        if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && (neutral_trees > 5||rand256()<128) && rc.isBuildReady()) {
                            rc.buildRobot(RobotType.LUMBERJACK, dir);
                        } else {
                            if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && rc.isBuildReady()) {
                                rc.buildRobot(RobotType.LUMBERJACK, dir);
                            }
                        }
                    }
                    // rc.buildRobot(RobotType.SOLDIER, dir);
                    // } else
                    // if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) &&
                    // rand256() <4 && rc.isBuildReady()) {
                    // rc.buildRobot(RobotType.LUMBERJACK, dir);
                    // }
                    // else if (rc.canBuildRobot(RobotType.SCOUT, dir) &&
                    // rand256() < 0 && rc.isBuildReady()) {
                    // rc.buildRobot(RobotType.SCOUT, dir);
                    // } else if (rc.canBuildRobot(RobotType.TANK, dir) &&
                    // rand256() < 0 && rc.isBuildReady()) {
                    // rc.buildRobot(RobotType.TANK, dir);
                    // }
                }

                // Clock.yield() makes the robot wait until the next turn, then
                // it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }
}
