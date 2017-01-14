package stars;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public strictfp class RobotPlayer {
	static RobotController rc;

	static MapLocation myLocation;

	public static void run(RobotController rc) throws GameActionException {

		MapLocation dist = rc.getInitialArchonLocations(rc.getTeam().opponent())[0];
		// float range = rc.getType().sensorRadius;

		if (rc.getType().equals(RobotType.GARDENER)) {
			// FastLocSet map = new FastLocSet();
			Bug.rc = rc;
			while (true) {

				try {
					myLocation = rc.getLocation();
					Bug.here = myLocation;

					Bug.goTo(dist);
					if (myLocation.isWithinDistance(dist, 1)) {
						rc.resign();
					}
					Clock.yield();

				} catch (GameActionException e) {
					e.printStackTrace();
				}
			}
		} else {
			while (true) {

				try {
					myLocation = rc.getLocation();
					if (rc.getRobotCount() < 2) {
						if (rc.canBuildRobot(RobotType.GARDENER, Direction.getWest()))
							rc.buildRobot(RobotType.GARDENER, Direction.getWest());
						else if (rc.canBuildRobot(RobotType.GARDENER, Direction.getNorth()))
							rc.buildRobot(RobotType.GARDENER, Direction.getNorth());
						else if (rc.canBuildRobot(RobotType.GARDENER, Direction.getSouth()))
							rc.buildRobot(RobotType.GARDENER, Direction.getSouth());
						else if (rc.canBuildRobot(RobotType.GARDENER, Direction.getEast()))
							rc.buildRobot(RobotType.GARDENER, Direction.getEast());

					} else {
						return;
					}
					Clock.yield();

				} catch (GameActionException e) {
					e.printStackTrace();
				}
			}
		}

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
	static boolean tryMove(RobotController rc, Direction dir) throws GameActionException {
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

}