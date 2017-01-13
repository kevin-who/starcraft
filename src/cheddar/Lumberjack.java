package cheddar;

import battlecode.common.*;

import static cheddar.Global.dodge;
import static cheddar.Global.randomDirection;
import static cheddar.Global.tryMove;

/**
 * Created by kevinhu on 1/12/17.
 */
public class Lumberjack {
    static RobotController rc;

    public Lumberjack(RobotController rc)
    {
        Lumberjack.rc = rc;
    }

    MapLocation myLocation;

    public void run() {
        System.out.println("I'm a Jinchao!");
        Team enemy = rc.getTeam().opponent();
        boolean move = true;

        // The code you want your robot to perform every round should be in this
        // loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your
            // robot to explode
            try {

                if ((int) (rc.getTeamBullets() / 10) + rc.getTeamVictoryPoints() >= 1000) {
                    rc.donate(rc.getTeamBullets());
                }
                move = true;
                // See if there are any enemy robots within striking range
                // (distance 1 from lumberjack's radius)

                myLocation = rc.getLocation();
                dodge(rc,myLocation);

                if (rc.readBroadcast(10) != -1&&!rc.hasMoved()) {
                    int x = rc.readBroadcast(11);
                    int y = rc.readBroadcast(12);
                    tryMove(rc,new Direction(x - myLocation.x, y - myLocation.y));
                }

                RobotInfo[] robots = rc.senseNearbyRobots(
                        RobotType.LUMBERJACK.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);

                if (robots.length > 0 && !rc.hasAttacked()) {
                    // Use strike() to hit all nearby robots!
                    rc.strike();
                    rc.broadcast(2, rc.getRoundNum());
                    rc.broadcast(3, (int) myLocation.x);
                    rc.broadcast(4, (int) myLocation.y);
                } else {
                    robots = rc.senseNearbyRobots(-1, enemy);

                    if (robots.length > 0) {

                        MapLocation enemyLocation = robots[0].getLocation();
                        Direction toEnemy = myLocation.directionTo(enemyLocation);
                        rc.broadcast(2, rc.getRoundNum());
                        rc.broadcast(3, (int) enemyLocation.x);
                        rc.broadcast(4, (int) enemyLocation.y);
                        if (!rc.hasMoved())
                            tryMove(rc,toEnemy);
                    } else {
                        TreeInfo[] trees = rc.senseNearbyTrees(-1);

                        if (trees.length > 0) {
                            int closest_index = 0;
                            float mindist = 10000000;
                            for (int x = 0; x < trees.length; x++) {
                                TreeInfo this_tree = trees[x];
                                if (!this_tree.getTeam().equals(rc.getTeam())) {
                                    float distance_to = myLocation.distanceSquaredTo(this_tree.location);
                                    if (mindist > distance_to) {
                                        closest_index = x;
                                        mindist = distance_to;
                                    }
                                    if (rc.canChop(this_tree.ID)) {
                                        rc.chop(this_tree.ID);
                                        move = false;
                                    }
                                }
                            }
                            boolean far = mindist > (RobotType.LUMBERJACK.bodyRadius + trees[closest_index].radius);
                            if (!rc.hasMoved() && move && far && !trees[closest_index].getTeam().equals(rc.getTeam())) {
                                tryMove(rc,myLocation.directionTo(trees[closest_index].location));
                            } else if (!far) {
                                move = false;
                            }
                        }
                        if (!rc.hasMoved() && move) {

                            Direction d = randomDirection();
                            if (rc.readBroadcast(2) != 0) {
                                int x = rc.readBroadcast(3);
                                int y = rc.readBroadcast(4);
                                if (myLocation.isWithinDistance(new MapLocation(x, y), 5)) {
                                    rc.broadcast(2, 0);
                                }
                                d = new Direction(x - myLocation.x, y - myLocation.y);
                            }
                            tryMove(rc,d);

                        }
                    }

                }
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Jinchao Exception");
                e.printStackTrace();
            }
        }
    }
}
