package ben;

import java.util.Random;

import battlecode.common.BulletInfo;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TreeInfo;

public strictfp class RobotPlayer {
	static RobotController rc;

	static Random rnd;
	
	static MapLocation myLocation;

	/**
	 * run() is the method that is called when a robot is instantiated in the
	 * Battlecode world. If this method returns, the robot dies!
	 **/
	public static void run(RobotController rc) throws GameActionException {

		// This is the RobotController object. You use it to perform actions
		// from this robot,
		// and to get information on its current status.
		RobotPlayer.rc = rc;
		rnd = new Random(rc.getID());
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
	
	static void dodge() throws GameActionException{

		BulletInfo[] bi = rc.senseNearbyBullets();
		for (BulletInfo info : bi) {
			Direction propagationDirection = info.dir;
			MapLocation bulletLocation = info.location;

			// Calculate bullet relations to this robot
			Direction directionToRobot = bulletLocation.directionTo(myLocation);
			float distToRobot = bulletLocation.distanceTo(myLocation);
			float theta = propagationDirection.radiansBetween(directionToRobot);

			// If theta > 90 degrees, then the bullet is traveling
			// away from us and
			// we can break early
			if ((theta < 0 ? -theta : theta) > Math.PI / 2) {
				continue;
			}

			float perpendicularDist = (float) Math.abs(distToRobot * Math.sin(theta));

			if (perpendicularDist <= rc.getType().bodyRadius) {
				tryMove(info.getDir().rotateLeftDegrees(90));
				break;
			}
		}
	}

	static void runArchon() throws GameActionException {
		System.out.println("I'm an archon!");
		Team enemy = rc.getTeam().opponent();

		// The code you want your robot to perform every round should be in this
		// loop
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {
				myLocation = rc.getLocation();
				dodge();

				RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

				if (robots.length > 0) {
					MapLocation enemyLocation = robots[0].getLocation();
					rc.broadcast(2, rc.getRoundNum());
					rc.broadcast(3, (int) enemyLocation.x);
					rc.broadcast(4, (int) enemyLocation.y);

				}

				// Generate a random direction
				Direction dir = randomDirection();

				// Randomly attempt to build a gardener in this direction
				if (rc.canHireGardener(dir) && (rnd.nextDouble() < .001 || rc.getRoundNum() < 200)) {
					if (rc.canHireGardener(dir))
						rc.hireGardener(dir);
				}
				if (!rc.hasMoved())
					tryMove(randomDirection());

				if (rc.getRoundLimit() - rc.getRoundNum() < 750) {
					if (rc.getTeamBullets() > 60)
						rc.donate(20);
				}
				if (rc.getTeamBullets() > 600)
					rc.donate(100);

				// Clock.yield() makes the robot wait until the next turn, then
				// it will perform this loop again
				Clock.yield();

			} catch (Exception e) {
				System.out.println("Archon Exception");
				e.printStackTrace();
			}
		}
	}

	static void runGardener() throws GameActionException {
		System.out.println("I'm a gardener!");
		Team enemy = rc.getTeam().opponent();

		// The code you want your robot to perform every round should be in this
		// loop
		TreeInfo[] ti;
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {
				myLocation = rc.getLocation();
				
				dodge();

				RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

				if (robots.length > 0) {
					MapLocation enemyLocation = robots[0].getLocation();
					rc.broadcast(2, rc.getRoundNum());
					rc.broadcast(3, (int) enemyLocation.x);
					rc.broadcast(4, (int) enemyLocation.y);

				}
				
				Direction dir = randomDirection();
				ti = rc.senseNearbyTrees(-1, rc.getTeam());
				
				if (ti.length < 3 && rc.canPlantTree(dir) && (rc.getRoundNum() < 125 || rnd.nextDouble() < .01)) {
					rc.plantTree(dir);
				} else if (rc.canBuildRobot(RobotType.SOLDIER, dir) && rnd.nextDouble() < .02) {
					rc.buildRobot(RobotType.SOLDIER, dir);
				} else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && rnd.nextDouble() < .02 && rc.isBuildReady()) {
					rc.buildRobot(RobotType.LUMBERJACK, dir);
				} else if (rc.canBuildRobot(RobotType.SCOUT, dir) && rnd.nextDouble() < .02 && rc.isBuildReady()) {
					rc.buildRobot(RobotType.SCOUT, dir);
				} else if (rc.canBuildRobot(RobotType.TANK, dir) && rnd.nextDouble() < .02 && rc.isBuildReady()) {
					rc.buildRobot(RobotType.TANK, dir);
				}

				float min = 50f;
				for (TreeInfo t : ti) {
					if (t.health < min) {
						dir = new Direction(rc.getLocation(), t.getLocation());
						min = t.health;
					}
					if (t.health < 40 && rc.canWater(t.ID))
						rc.water(t.ID);
				}

				// Move randomly
				if (!rc.hasMoved())
					tryMove(dir);

				// Clock.yield() makes the robot wait until the next turn, then
				// it will perform this loop again
				Clock.yield();

			} catch (Exception e) {
				System.out.println("Gardener Exception");
				e.printStackTrace();
			}
		}
	}

	static void runSoldier() throws GameActionException {
		System.out.println("I'm a soldier!");
		Team enemy = rc.getTeam().opponent();

		// The code you want your robot to perform every round should be in this
		// loop
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {
				
				myLocation = rc.getLocation();
				dodge();

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
						tryMove(toEnemy);
					}
					int d = (int) myLocation.distanceTo(enemyLocation);
					if(d < 3 && rc.canFirePentadShot())
						rc.firePentadShot(toEnemy);
					else if (d < 5 && rc.canFireTriadShot()) {
						rc.fireTriadShot(toEnemy);
					}else if (rc.canFireSingleShot()) {
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
						tryMove(d);
				}
				// Clock.yield() makes the robot wait until the next turn, then
				// it will perform this loop again
				Clock.yield();

			} catch (Exception e) {
				System.out.println("Soldier Exception");
				e.printStackTrace();
			}
		}
	}

	static void runTank() throws GameActionException {
		System.out.println("I'm a tank!");
		Team enemy = rc.getTeam().opponent();

		// The code you want your robot to perform every round should be in this
		// loop
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {
				myLocation = rc.getLocation();
				dodge();
				
				// See if there are any nearby enemy robots
				RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

				// If there are some...
				if (robots.length > 0) {
					// And we have enough bullets, and haven't attacked yet this
					// turn...
					MapLocation enemyLocation = robots[0].getLocation();
					Direction toEnemy = myLocation.directionTo(enemyLocation);

					if (rc.canFireSingleShot())
						rc.fireSingleShot(toEnemy);
					rc.broadcast(2, rc.getRoundNum());
					rc.broadcast(3, (int) enemyLocation.x);
					rc.broadcast(4, (int) enemyLocation.y);
					
					if (!rc.hasMoved()) {
						if (myLocation.distanceTo(enemyLocation) > 6)
							tryMove(toEnemy);
						else
							tryMove(toEnemy.opposite());
					}
				} else {

					Direction d = randomDirection();
					try {
						if (rc.readBroadcast(2) >= rc.getRoundNum() - 1) {
							int x = rc.readBroadcast(3);
							int y = rc.readBroadcast(4);
							d = new Direction(x - myLocation.x, y - myLocation.y);
							tryMove(d);
							if (rc.canFireSingleShot()){
								rc.fireSingleShot(d);
							}							
						}
					} catch (GameActionException e) {

					}
					if (!rc.hasMoved())
						tryMove(d);
				}
				// Clock.yield() makes the robot wait until the next turn, then
				// it will perform this loop again
				Clock.yield();

			} catch (Exception e) {
				System.out.println("Tank Exception");
				e.printStackTrace();
			}
		}
	}

	static void runScout() throws GameActionException {
		System.out.println("I'm an scout!");
		Team enemy = rc.getTeam().opponent();

		// The code you want your robot to perform every round should be in this
		// loop
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {
				myLocation = rc.getLocation();
				dodge();
				// See if there are any nearby enemy robots
				RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

				// If there are some...
				if (robots.length > 0) {
					// And we have enough bullets, and haven't attacked yet this
					// turn...
					MapLocation enemyLocation = robots[0].getLocation();
					Direction toEnemy = myLocation.directionTo(enemyLocation);

					if (rc.canFireSingleShot()) {
						rc.fireSingleShot(toEnemy);
					}
					rc.broadcast(2, rc.getRoundNum());
					rc.broadcast(3, (int) enemyLocation.x);
					rc.broadcast(4, (int) enemyLocation.y);
					
					if (!rc.hasMoved()) {
						if (myLocation.distanceTo(enemyLocation) > 9)
							tryMove(toEnemy);
						else
							tryMove(toEnemy.opposite());
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
						tryMove(d);
				}
				// Clock.yield() makes the robot wait until the next turn, then
				// it will perform this loop again
				Clock.yield();

			} catch (Exception e) {
				System.out.println("Scout Exception");
				e.printStackTrace();
			}
		}
	}

	static void runLumberjack() throws GameActionException {
		System.out.println("I'm a lumberjack!");
		Team enemy = rc.getTeam().opponent();

		// The code you want your robot to perform every round should be in this
		// loop
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {

				// See if there are any enemy robots within striking range
				// (distance 1 from lumberjack's radius)

				myLocation = rc.getLocation();
				dodge();

				RobotInfo[] robots = rc.senseNearbyRobots(
						RobotType.LUMBERJACK.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);

				if (robots.length > 0 && !rc.hasAttacked()) {
					// Use strike() to hit all nearby robots!
					rc.strike();
					rc.broadcast(2, rc.getRoundNum());
					rc.broadcast(3, (int) myLocation.x);
					rc.broadcast(4, (int) myLocation.y);
				} else {
					// No close robots, so search for robots within sight radius
					robots = rc.senseNearbyRobots(-1, enemy);

					// If there is a robot, move towards it

					if (robots.length > 0) {
						MapLocation enemyLocation = robots[0].getLocation();
						Direction toEnemy = myLocation.directionTo(enemyLocation);
						rc.broadcast(2, rc.getRoundNum());
						rc.broadcast(3, (int) enemyLocation.x);
						rc.broadcast(4, (int) enemyLocation.y);
						if (!rc.hasMoved())
							tryMove(toEnemy);
					} else if (!rc.hasMoved()) {
						Direction d = randomDirection();

						if (rc.readBroadcast(2) >= rc.getRoundNum() - 1) {
							int x = rc.readBroadcast(3);
							int y = rc.readBroadcast(4);
							d = new Direction(x - myLocation.x, y - myLocation.y);
						}
						tryMove(d);

					}

				}

				// Clock.yield() makes the robot wait until the next turn, then
				// it will perform this loop again
				Clock.yield();

			} catch (Exception e) {
				System.out.println("Lumberjack Exception");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns a random Direction
	 * 
	 * @return a random Direction
	 */
	static Direction randomDirection() {
		return new Direction((float) (rnd.nextDouble() * 2.0f * Math.PI));
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
	static boolean tryMove(Direction dir) throws GameActionException {
		return tryMove(dir, 20, 3);
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
	static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

		// First, try intended direction
		if (rc.canMove(dir)) {
			rc.move(dir);
			return true;
		}

		// Now try a bunch of similar angles
		boolean moved = false;
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

	/**
	 * A slightly more complicated example function, this returns true if the
	 * given bullet is on a collision course with the current robot. Doesn't
	 * take into account objects between the bullet and this robot.
	 *
	 * @param bullet
	 *            The bullet in question
	 * @return True if the line of the bullet's path intersects with this
	 *         robot's current position.
	 */
	static boolean willCollideWithMe(BulletInfo bullet) {
		MapLocation myLocation = rc.getLocation();

		// Get relevant bullet information
		Direction propagationDirection = bullet.dir;
		MapLocation bulletLocation = bullet.location;

		// Calculate bullet relations to this robot
		Direction directionToRobot = bulletLocation.directionTo(myLocation);
		float distToRobot = bulletLocation.distanceTo(myLocation);
		float theta = propagationDirection.radiansBetween(directionToRobot);

		// If theta > 90 degrees, then the bullet is traveling away from us and
		// we can break early
		if (Math.abs(theta) > Math.PI / 2) {
			return false;
		}

		// distToRobot is our hypotenuse, theta is our angle, and we want to
		// know this length of the opposite leg.
		// This is the distance of a line that goes from myLocation and
		// intersects perpendicularly with propagationDirection.
		// This corresponds to the smallest radius circle centered at our
		// location that would intersect with the
		// line that is the path of the bullet.
		float perpendicularDist = (float) Math.abs(distToRobot * Math.sin(theta)); // soh
																					// cah
																					// toa
																					// :)

		return (perpendicularDist <= rc.getType().bodyRadius);
	}
}