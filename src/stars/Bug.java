package stars;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Bug {
	private static MapLocation dest = null;

	private static boolean tracing = false;
	private static MapLocation lastWall = null;
	private static int closestDistWhileBugging = Integer.MAX_VALUE;
	public static MapLocation here;
	public static RobotController rc;

	static boolean tryMove(RobotController rc, Direction dir) throws GameActionException {
		return tryMove(rc, dir, 20, 3);
	}

	static boolean tryMove(RobotController rc, Direction dir, float degreeOffset, int checksPerSide)
			throws GameActionException {

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
			dest = theDest;
			tracing = false;
		}

		if (!tracing) {
			rc.setIndicatorDot(here.add(here.directionTo(dest)), 128, 0, 0);
			if (tryMove(rc, here.directionTo(dest))) {
				return;
			} else {
				startTracing();
			}
		} else if (here.distanceSquaredTo(dest) < closestDistWhileBugging) {
			rc.setIndicatorDot(here.add(here.directionTo(dest)), 128, 0, 0);
			if (tryMove(rc, here.directionTo(dest))) {
				tracing = false;
				return;
			}

		}
		traceMove();
	}

	static void startTracing() {
		tracing = true;
		lastWall = here.add(here.directionTo(dest));
		closestDistWhileBugging = (int) here.distanceSquaredTo(dest);
	}

	static void traceMove() throws GameActionException {
		Direction tryDir = here.directionTo(lastWall);
		rc.setIndicatorDot(here.add(tryDir), 128, 0, 0);

		for (int i = 0; i < 4; i++) {
			tryDir = tryDir.rotateRightDegrees(90);
			if (tryMove(rc, tryDir)) {
				return;
			} else {
				lastWall = here.add(tryDir);
			}
		}
	}
}