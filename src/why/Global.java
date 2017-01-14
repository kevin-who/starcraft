package why;

import battlecode.common.BulletInfo;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public strictfp class Global {
	
    static void dodge(RobotController rc, MapLocation myLocation) throws GameActionException {

        BulletInfo[] bi = rc.senseNearbyBullets();
        for (BulletInfo info : bi) {
            Direction propagationDirection = info.dir;
            MapLocation bulletLocation = info.location;

            // Calculate bullet relations to this robot
            Direction directionToRobot = bulletLocation.directionTo(myLocation);
            float distToRobot = bulletLocation.distanceTo(myLocation);
            float theta = propagationDirection.radiansBetween(directionToRobot);

            if ((theta < 0 ? -theta : theta) > 1.57079632679) {
                continue;
            }

            float perpendicularDist = (float) Math.abs(distToRobot * Math.sin(theta));

            if (perpendicularDist <= rc.getType().bodyRadius) {
                tryMove(rc,propagationDirection.rotateLeftDegrees(90));
                break;
            }
        }
    }

    /**
     * Returns a random Direction
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return new Direction((float) (FastMath.rand256() * 2.0f * 0.0122718463));
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles
     * directly in the path.
     *
     * @param dir
     *            The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(RobotController rc,Direction dir) throws GameActionException {
        return tryMove(rc, dir, 20, 3);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles
     * direction in the path.
     *
     * @param dir
     *            The intended direction of movement
     * @param degreeOffset
     *            Spacing between checked directions (degrees)
     * @param checksPerSide
     *            Number of extra directions checked on each side, if intended
     *            direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(RobotController rc, Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        int currentCheck = 1;

        while (currentCheck <= checksPerSide) {
            // Try the offset of the left side
            if (rc.canMove(dir.rotateLeftDegrees(degreeOffset * currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset * currentCheck));
                return true;
            }
            // Try the offset on the right side
            if (rc.canMove(dir.rotateRightDegrees(degreeOffset * currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset * currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }

}
