package cheddar;

import battlecode.common.*;

import static cheddar.Global.*;

/**
 * Created by kevinhu on 1/12/17.
 */
public class Scout {
    static RobotController rc;

    public Scout(RobotController rc)
    {
        Scout.rc = rc;
    }

    MapLocation myLocation;

    public void run() {
        System.out.println("SCOUT");
        Team enemy = rc.getTeam().opponent();
        MapLocation[] targets = rc.getInitialArchonLocations(enemy);



        // The code you want your robot to perform every round should be in this
        // loop
        while (true) {
            // Try/catch blocks stop unhandled exceptions, which cause your
            // robot to explode
            try {
                if (!rc.hasMoved()) {
                    dodge(rc, myLocation);
                }

                myLocation = rc.getLocation();

                if (rc.readBroadcast(2) >= rc.getRoundNum() - 1) {
                    int x = rc.readBroadcast(3);
                    int y = rc.readBroadcast(4);
                    if (!rc.hasMoved()) {
                        tryMove(rc, new Direction(x - myLocation.x, y - myLocation.y));
                    }
                }

                MapLocation enemyBase = targets[0];
                Direction toBase = myLocation.directionTo(enemyBase);

                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
                RobotInfo enemyGardener;

                for (int x = 0; x < robots.length; x++) {
                    RobotInfo this_bot = robots[x];
                    if (this_bot.getType() == RobotType.LUMBERJACK) {
                        enemyGardener = this_bot;
                        MapLocation gardenerLocation = enemyGardener.getLocation();
                        Direction toGardener = myLocation.directionTo(gardenerLocation);

                        rc.broadcast(2, rc.getRoundNum());
                        rc.broadcast(3, (int) gardenerLocation.x);
                        rc.broadcast(4, (int) gardenerLocation.y);
                        if (!rc.hasMoved()) {
                            if (myLocation.distanceTo(gardenerLocation) > 12)
                                tryMove(rc, toGardener);
                            else
                                tryMove(rc, toGardener.opposite());
                        }
                        if (rc.canFireSingleShot()) {
                            rc.fireSingleShot(toGardener);
                        }
                        break;
                    }
                }

                if (!rc.hasMoved()) {
                    tryMove(rc, randomDirection());
                }

            } catch (Exception e) {
                System.out.println("SCOUT EXCEPTION");
                e.printStackTrace();
            }
        }
    }
}
