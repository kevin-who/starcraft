package marines;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public strictfp class Bug {
	private static MapLocation dest = null;

	private static boolean tracing = false;
	private static MapLocation lastWall = null;
	private static int closestDistWhileBugging = Integer.MAX_VALUE;
	public static MapLocation loc;
	public static RobotController rc;
	private static Direction lastDir = null;
	private static boolean isRobot = false;

	public static Direction rndDir() {
		return new Direction(FastMath.rand256() * 0.0245436926f);
	}

	public static boolean tryMove(Direction dir) throws GameActionException {
		return tryMove(dir, 10, 4);
	}

	public static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

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

	public static void goTo(MapLocation theDest) throws GameActionException {
		if (!theDest.equals(dest)) {
			lastDir = loc.directionTo(theDest);
			dest = theDest;
			tracing = false;
			isRobot = false;
		}

		Direction dir = loc.directionTo(dest);

		if (!tracing) {
			rc.setIndicatorDot(loc.add(dir), 128, 0, 0);
			if (tryMove(dir)) {
				lastDir = loc.directionTo(theDest);
				return;
			} else if (rc.senseNearbyRobots(rc.getType().strideRadius + rc.getType().bodyRadius).length > 0) {
				isRobot = true;
				rc.setIndicatorDot(loc.add(dir), 128, 0, 128);
			} else {
				startTracing();
			}
		} else if (loc.distanceSquaredTo(dest) < closestDistWhileBugging) {
			rc.setIndicatorDot(loc.add(dir), 0, 128, 0);
			if (tryMove(dir)) {
				lastDir = loc.directionTo(theDest);
				tracing = false;
				return;
			}

		}
		if (isRobot) {
			dir = rndDir();
			rc.setIndicatorLine(loc, loc.add(dir), 128, 128, 128);
			for (int i = 0; i < 4; i++) {
				dir = dir.rotateLeftDegrees(90);
				rc.setIndicatorLine(loc, loc.add(dir), 0, 0, 128);
				if (tryMove(dir)) {
					lastDir = dir;
					isRobot = false;
					tracing = false;
					return;
				}
			}

		} else {
			traceMove();
		}
	}

	static void startTracing() {
		tracing = true;
		lastWall = loc.add(loc.directionTo(dest));
		closestDistWhileBugging = (int) loc.distanceSquaredTo(dest);
	}

	static void traceMove() throws GameActionException {
		Direction tryDir = loc.directionTo(lastWall);
		rc.setIndicatorDot(lastWall, 0, 128, 128);
		rc.setIndicatorLine(loc, loc.add(lastDir), 255, 255, 0);
		double dif = tryDir.radians - lastDir.radians;
		if ((dif + 3.14159265358979323846264338) % (2 * 3.14159265358979323846264338)
				- 3.14159265358979323846264338 < 0) {
			for (int i = 0; i < 8; i++) {
				tryDir = tryDir.rotateLeftDegrees(90);
				rc.setIndicatorLine(loc, loc.add(tryDir), 0, 0, 128);
				if (tryMove(tryDir)) {
					lastDir = tryDir;
					return;
				} else if (rc.senseNearbyRobots(rc.getType().strideRadius + rc.getType().bodyRadius).length > 0) {
					isRobot = true;
					tracing = false;
				} else {
					lastWall = loc.add(tryDir);
				}
			}
		} else {
			for (int i = 0; i < 8; i++) {
				tryDir = tryDir.rotateRightDegrees(90);
				rc.setIndicatorLine(loc, loc.add(tryDir), 0, 0, 128);
				if (tryMove(tryDir)) {
					lastDir = tryDir;
					return;
				} else {
					lastWall = loc.add(tryDir);
				}

			}
		}
	}
}
