package cheddar;

import battlecode.common.*;

import static cheddar.Global.dodge;
import static cheddar.Global.randomDirection;
import static cheddar.Global.tryMove;

/**
 * Created by kevinhu on 1/12/17.
 */
public class Soldier {
    static RobotController rc;

    public Soldier(RobotController rc)
    {
        Soldier.rc = rc;
    }

    MapLocation myLocation;

    public void run() {
        System.out.println("SOLDIER");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this
        // loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your
            // robot to explode
            try {

                myLocation = rc.getLocation();
                dodge(rc,myLocation);

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

                // If there are some...
                if (robots.length > 0) {
                    // And we have enough bullets, and haven't attacked yet this
                    // turn...
                    MapLocation enemyLocation = robots[0].getLocation();
                    Direction toEnemy = myLocation.directionTo(enemyLocation);

                    rc.broadcast(2, rc.getRoundNum());
                    rc.broadcast(3, (int) enemyLocation.x);
                    rc.broadcast(4, (int) enemyLocation.y);
                    if (!rc.hasMoved()) {
                        tryMove(rc,toEnemy);
                    }
                    int d = (int) myLocation.distanceTo(enemyLocation);
                    if (d < 3 && rc.canFirePentadShot())
                        rc.firePentadShot(toEnemy);
                    else if (d < 5 && rc.canFireTriadShot()) {
                        rc.fireTriadShot(toEnemy);
                    } else if (rc.canFireSingleShot()) {
                        rc.fireSingleShot(toEnemy);
                    }

                } else {

                    Direction d = randomDirection();
                    try {
                        if (rc.readBroadcast(2) >= rc.getRoundNum() - 1) {
                            int x = rc.readBroadcast(3);
                            int y = rc.readBroadcast(4);
                            d = new Direction(x - myLocation.x, y - myLocation.y);
                        }
                    } catch (GameActionException e) {

                    }
                    if (!rc.hasMoved())
                        tryMove(rc,d);
                }
                // Clock.yield() makes the robot wait until the next turn, then
                // it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("SOLDIER EXCEPTION");
                e.printStackTrace();
            }
        }
    }
}
