package cheddar;

import battlecode.common.*;

import static cheddar.FastMath.rand256;
import static cheddar.Global.*;

/**
 * Created by kevinhu on 1/12/17.
 */
public class Archon {
    static RobotController rc;

    public Archon(RobotController rc)
    {
        Archon.rc = rc;
    }

    MapLocation myLocation;

    public void run() {
        System.out.println("ARCHON");
        Team enemy = rc.getTeam().opponent();
        Direction dir= randomDirection();

        // The code you want your robot to perform every round should be in this
        // loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your
            // robot to explode
            try {
                int round = rc.getRoundNum();
                myLocation = rc.getLocation();
                dodge(rc,myLocation);

                int nearby_gardeners = 0;

                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
                RobotInfo[] bots = rc.senseNearbyRobots(-1, enemy);
                for (int x = 0; x < bots.length; x++) {
                    if(bots[x].getType()== RobotType.GARDENER){
                        nearby_gardeners++;
                    }
                }

                if (robots.length > 0) {
                    MapLocation enemyLocation = robots[0].getLocation();
                    rc.broadcast(10, rc.getRoundNum());
                    rc.broadcast(11, (int) enemyLocation.x);
                    rc.broadcast(12, (int) enemyLocation.y);
                }else{
                    rc.broadcast(10, -1);
                }

                // Generate a random direction
                // Randomly attempt to build a gardener in this direction
                if (rc.canHireGardener(dir)&&nearby_gardeners<5&&rand256()<200) {
                    rc.hireGardener(dir);
                }

                if (round>100&&!rc.hasMoved()&&round%4==0){
                    dir = randomDirection();
                    tryMove(rc,dir);
                }

                if (rc.getTeamBullets() > 150)
                    rc.donate(rc.getTeamBullets()/3);

                // Clock.yield() makes the robot wait until the next turn, then
                // it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("ARCHON EXCEPTION");
                e.printStackTrace();
            }
        }
    }
}
