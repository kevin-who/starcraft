package why;

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

	static MapLocation myLocation;

	/**
	 * run() is the method that is called when a robot is instantiated in the
	 * Battlecode world. If this method returns, the robot dies!
	 **/
	public static void run(RobotController rc) throws GameActionException {

		RobotPlayer.rc = rc;
		FastMath.initRand(rc);

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

	static void dodge() throws GameActionException {

		BulletInfo[] bi = rc.senseNearbyBullets();
		for (BulletInfo info : bi) {
			Direction propagationDirection = info.dir;
			MapLocation bulletLocation = info.location;

			// Calculate bullet relations to this robot
			Direction directionToRobot = bulletLocation.directionTo(myLocation);
			float distToRobot = bulletLocation.distanceTo(myLocation);
			float theta = propagationDirection.radiansBetween(directionToRobot);

			if ((theta < 0 ? -theta : theta) > Math.PI / 2) {
				continue;
			}

			float perpendicularDist = (float) Math.abs(distToRobot * Math.sin(theta));

			if (perpendicularDist <= rc.getType().bodyRadius) {
				tryMove(propagationDirection.rotateLeftDegrees(90));
				break;
			}
		}
	}

	static void runArchon() throws GameActionException {
		System.out.println("I'm an archon!");
		Team enemy = rc.getTeam().opponent();
		MapLocation enemyLocation = rc.getInitialArchonLocations(enemy)[0];

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
					enemyLocation = robots[0].getLocation();
					rc.broadcast(2, rc.getRoundNum());
					rc.broadcast(3, (int) enemyLocation.x);
					rc.broadcast(4, (int) enemyLocation.y);

				}
				if (rc.getRoundNum() < 10) {
					rc.broadcast(2, rc.getRoundNum());
					rc.broadcast(3, (int) enemyLocation.x);
					rc.broadcast(4, (int) enemyLocation.y);
				}
				// Generate a random direction
				Direction dir = randomDirection();

				if (!rc.hasMoved())
					tryMove(dir);

				dir = myLocation.directionTo(enemyLocation);
				if (rc.canHireGardener(dir) && (rc.getRoundNum() < 200 || FastMath.rand256() < 10)) {
					rc.hireGardener(dir);
				}

				if (rc.getRoundLimit() - rc.getRoundNum() < 750) {
					if (rc.getTeamBullets() > 60)
						rc.donate(20);
				}
				float ammo = rc.getTeamBullets();
				if ((int) (ammo / 10) + rc.getTeamVictoryPoints() >= 1000) {
					rc.donate(rc.getTeamBullets());
				} else if (ammo > 600)
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
		Direction dir = randomDirection();
		while (true) {
			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {
				if (rc.senseNearbyRobots(4f).length < 1 && rc.canPlantTree(dir)
						&& rc.canPlantTree(dir.rotateLeftRads(60)) && rc.canPlantTree(dir.rotateLeftRads(120))
						&& rc.canPlantTree(dir.rotateLeftRads(180)) && rc.canPlantTree(dir.rotateLeftRads(240))
						&& rc.canPlantTree(dir.rotateLeftRads(300)))
					break;

				if (rc.getTeamBullets() > 50 && !rc.hasMoved())
					if (!tryMove(dir, 0, 0)) {
						dir = dir.opposite();
						if (rc.senseNearbyTrees(-1, Team.NEUTRAL).length > 0 && rc.senseNearbyRobots(3f).length < 1) {
							if (rc.canBuildRobot(RobotType.LUMBERJACK, dir)) {
								rc.buildRobot(RobotType.LUMBERJACK, dir);
								break;
							} else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir.opposite())) {
								rc.buildRobot(RobotType.LUMBERJACK, dir.opposite());
								break;

							} else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir.rotateLeftDegrees(90))) {
								rc.buildRobot(RobotType.LUMBERJACK, dir.rotateLeftDegrees(90));
								break;

							} else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir.rotateRightDegrees(90))) {
								rc.buildRobot(RobotType.LUMBERJACK, dir.rotateRightDegrees(90));
								break;
							}
							dir = randomDirection();
						}
					}

				// Clock.yield() makes the robot wait until the next turn,
				// then
				// it will perform this loop again
				Clock.yield();

			} catch (Exception e) {
				System.out.println("Gardener Exception");
				e.printStackTrace();
			}
		}

		// The code you want your robot to perform every round should be in
		// this
		// loop
		TreeInfo[] ti;
		MapLocation target = rc.getInitialArchonLocations(enemy)[0];
		myLocation = rc.getLocation();
		dir = myLocation.directionTo(target).rotateLeftDegrees(-30);
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {
				if ((int) (rc.getTeamBullets() / 10) + rc.getTeamVictoryPoints() >= 1000) {

					rc.donate(rc.getTeamBullets());
				}
				myLocation = rc.getLocation();

				RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

				if (robots.length > 0) {
					MapLocation enemyLocation = robots[0].getLocation();
					rc.broadcast(2, rc.getRoundNum());
					rc.broadcast(3, (int) enemyLocation.x);
					rc.broadcast(4, (int) enemyLocation.y);
				}

				ti = rc.senseNearbyTrees(2, rc.getTeam());

				for (TreeInfo t : ti) {
					if (t.health < 45 && rc.canWater(t.ID))
						rc.water(t.ID);
				}

				if (ti.length < 5) {

					if (rc.canPlantTree(dir.rotateLeftDegrees(60))) {
						rc.plantTree(dir.rotateLeftDegrees(60));
					} else if (rc.canPlantTree(dir.rotateLeftDegrees(120))) {
						rc.plantTree(dir.rotateLeftDegrees(120));
					} else if (rc.canPlantTree(dir.rotateLeftDegrees(180))) {
						rc.plantTree(dir.rotateLeftDegrees(180));
					} else if (rc.canPlantTree(dir.rotateLeftDegrees(240))) {
						rc.plantTree(dir.rotateLeftDegrees(240));
					} else if (rc.canPlantTree(dir.rotateLeftDegrees(300))) {
						rc.plantTree(dir.rotateLeftDegrees(300));
					} else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir.rotateLeftDegrees(30))) {
						rc.buildRobot(RobotType.LUMBERJACK, dir.rotateLeftDegrees(30));
					} else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir.rotateLeftDegrees(60))) {
						rc.buildRobot(RobotType.LUMBERJACK, dir.rotateLeftDegrees(60));
					} else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir.rotateLeftDegrees(90))) {
						rc.buildRobot(RobotType.LUMBERJACK, dir.rotateLeftDegrees(90));
					} else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir.rotateLeftDegrees(120))) {
						rc.buildRobot(RobotType.LUMBERJACK, dir.rotateLeftDegrees(120));
					} else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir.rotateLeftDegrees(150))) {
						rc.buildRobot(RobotType.LUMBERJACK, dir.rotateLeftDegrees(150));
					} else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir.rotateLeftDegrees(180))) {
						rc.buildRobot(RobotType.LUMBERJACK, dir.rotateLeftDegrees(180));
					}

				} else {
					if (rc.canBuildRobot(RobotType.SOLDIER, dir) && FastMath.rand256() < 50) {
						rc.buildRobot(RobotType.SOLDIER, dir);
					} else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && FastMath.rand256() < 2) {
						rc.buildRobot(RobotType.LUMBERJACK, dir);
					} else if (rc.canBuildRobot(RobotType.SCOUT, dir) && FastMath.rand256() < 1 && rc.isBuildReady()) {
						rc.buildRobot(RobotType.SCOUT, dir);
					}
				}

				// Clock.yield() makes the robot wait until the next turn,
				// then
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

				if ((int) (rc.getTeamBullets() / 10) + rc.getTeamVictoryPoints() >= 1000) {

					rc.donate(rc.getTeamBullets());
				}
				myLocation = rc.getLocation();
				dodge();

				// See if there are any nearby enemy robots
				RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

				// If there are some...
				if (robots.length > 0) {
					// And we have enough bullets, and haven't attacked yet this
					// turn...
					myLocation = rc.getLocation();

					MapLocation enemyLocation = robots[0].getLocation();
					Direction toEnemy = myLocation.directionTo(enemyLocation);

					rc.broadcast(2, rc.getRoundNum());
					rc.broadcast(3, (int) enemyLocation.x);
					rc.broadcast(4, (int) enemyLocation.y);
					if (!rc.hasMoved()) {
						tryMove(toEnemy.opposite());
					}

					myLocation = rc.getLocation();
					toEnemy = myLocation.directionTo(enemyLocation);

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
						if (rc.readBroadcast(2) != 0) {
							int x = rc.readBroadcast(3);
							int y = rc.readBroadcast(4);
							if (myLocation.isWithinDistance(new MapLocation(x, y), 5)) {
								rc.broadcast(2, 0);
							}
							d = new Direction(x - myLocation.x, y - myLocation.y);
						}
					} catch (GameActionException e) {

					}
					if (!rc.hasMoved())
						tryMove(d);
					if (!rc.hasAttacked()) {
						myLocation = rc.getLocation();

						TreeInfo[] trees = rc.senseNearbyTrees(3);

						if (trees.length > 0) {
							for (int x = 0; x < trees.length; x++) {
								TreeInfo this_tree = trees[x];
								if (!this_tree.getTeam().equals(rc.getTeam()) && !rc.hasAttacked()
										&& rc.canFireSingleShot()) {
									rc.fireSingleShot(myLocation.directionTo(this_tree.location));
									break;

								}
							}
						}
					}
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
				if ((int) (rc.getTeamBullets() / 10) + rc.getTeamVictoryPoints() >= 1000) {

					rc.donate(rc.getTeamBullets());
				}
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
						if (myLocation.distanceTo(enemyLocation) > 6)
							tryMove(toEnemy);
						else
							tryMove(toEnemy.opposite());
					}

					if (robots.length > 4 && rc.canFirePentadShot()) {
						rc.firePentadShot(toEnemy);
					} else if (robots.length > 2 && rc.canFireTriadShot()) {
						rc.fireTriadShot(toEnemy);
					} else if (rc.canFireSingleShot())
						rc.fireSingleShot(toEnemy);
				} else {

					Direction d = randomDirection();
					if (rc.readBroadcast(2) != 0) {
						int x = rc.readBroadcast(3);
						int y = rc.readBroadcast(4);
						if (myLocation.isWithinDistance(new MapLocation(x, y), 5)) {
							rc.broadcast(2, 0);
						}
						d = new Direction(x - myLocation.x, y - myLocation.y);
					}

					if (!rc.hasMoved())
						tryMove(d);

					TreeInfo[] trees = rc.senseNearbyTrees(3);

					if (trees.length > 0) {
						for (int x = 0; x < trees.length; x++) {
							TreeInfo this_tree = trees[x];
							if (!this_tree.getTeam().equals(rc.getTeam()) && !rc.hasAttacked()
									&& rc.canFireSingleShot()) {
								rc.fireSingleShot(myLocation.directionTo(this_tree.location));
								break;

							}
						}
					}
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
		Direction d = randomDirection();

		// The code you want your robot to perform every round should be in this
		// loop
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {
				if ((int) (rc.getTeamBullets() / 10) + rc.getTeamVictoryPoints() >= 1000) {

					rc.donate(rc.getTeamBullets());
				}
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
						if (myLocation.distanceTo(enemyLocation) > 9)
							tryMove(toEnemy);
						else
							tryMove(toEnemy.opposite());
					}
					if (rc.canFireSingleShot()) {
						rc.fireSingleShot(toEnemy);
					}
				} else {

					if (!rc.hasMoved())
						if (!tryMove(d, 0, 0)) {
							d = randomDirection();
						}

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
		boolean move = true;

		// The code you want your robot to perform every round should be in this
		// loop
		TreeInfo[] trees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
		int done = 0;
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {
				if (trees.length > 0) {
					int closest_index = 0;
					float mindist = 10000000;
					myLocation = rc.getLocation();
					for (int x = 0; x < trees.length; x++) {
						TreeInfo this_tree = trees[x];
						if (rc.canSenseLocation(this_tree.location)) {
							if (rc.senseTreeAtLocation(this_tree.location) == null
									|| !rc.senseTreeAtLocation(this_tree.location).team.equals(Team.NEUTRAL)) {
								done++;
							} else {
								float distance_to = myLocation.distanceTo(this_tree.location);
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
					}
					boolean far = mindist > (RobotType.LUMBERJACK.bodyRadius + trees[closest_index].radius);
					if (!rc.hasMoved() && move && far && !trees[closest_index].getTeam().equals(rc.getTeam())) {
						tryMove(myLocation.directionTo(trees[closest_index].location));
					} else if (!far) {
						move = false;
					}
					if (done >= trees.length)
						break;
				}
				Clock.yield();

			} catch (Exception e) {
				System.out.println("Lumberjack Exception");
				e.printStackTrace();
			}
		}

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
					robots = rc.senseNearbyRobots(-1, enemy);

					if (robots.length > 0) {

						MapLocation enemyLocation = robots[0].getLocation();
						Direction toEnemy = myLocation.directionTo(enemyLocation);
						rc.broadcast(2, rc.getRoundNum());
						rc.broadcast(3, (int) enemyLocation.x);
						rc.broadcast(4, (int) enemyLocation.y);
						if (!rc.hasMoved())
							tryMove(toEnemy);
					} else {
						trees = rc.senseNearbyTrees(-1);

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
								tryMove(myLocation.directionTo(trees[closest_index].location));
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
							tryMove(d);

						}
					}

				}
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
	static boolean tryMove(Direction dir) throws GameActionException {
		return tryMove(dir, 90, 4);
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
		float perpendicularDist = (float) Math.abs(distToRobot * Math.sin(theta));

		return (perpendicularDist <= rc.getType().bodyRadius);
	}
}