package cheese;

import battlecode.common.*;

import static cheese.FastMath.initRand;
import static cheese.FastMath.rand256;
import static cheese.FastMath.xsin;

public strictfp class RobotPlayer {
	static RobotController rc;

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

		initRand(rc);
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

	static void dodge() throws GameActionException {

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

			float perpendicularDist = (float) Math.abs(distToRobot * xsin(theta));

			if (perpendicularDist <= rc.getType().bodyRadius) {
				tryMove(propagationDirection.rotateLeftDegrees(90));
				break;
			}
		}
	}

	static void runArchon() throws GameActionException {
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
				dodge();

				int nearby_gardeners = 0;

				RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
				RobotInfo[] bots = rc.senseNearbyRobots(-1, enemy);
				for (int x = 0; x < bots.length; x++) {
					if(bots[x].getType()==RobotType.GARDENER){
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
						tryMove(dir);

					}

				if (rc.getTeamBullets() > 200)
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

	static void runGardener() throws GameActionException {
		System.out.println("GARDENER");
		Team enemy = rc.getTeam().opponent();
		boolean tree_hub = false;
		Direction dir = randomDirection();

		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {
				if (rc.senseNearbyRobots(5f).length <= 1 && rc.canPlantTree(dir)
						&& rc.canPlantTree(dir.rotateLeftRads(60)) && rc.canPlantTree(dir.rotateLeftRads(120))
						&& rc.canPlantTree(dir.rotateLeftRads(180)) && rc.canPlantTree(dir.rotateLeftRads(240))
						&& rc.canPlantTree(dir.rotateLeftRads(300)))
					break;

				if (rc.getTeamBullets() > 50 && !rc.hasMoved())
					if (!tryMove(dir, 0, 0)) {
						dir = randomDirection();
					}

				// Clock.yield() makes the robot wait until the next turn, then
				// it will perform this loop again
				Clock.yield();

			} catch (Exception e) {
				System.out.println("Gardener Exception");
				e.printStackTrace();
			}
		}

		// The code you want your robot to perform every round should be in this
		// loop
		TreeInfo[] ti;
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {
				//
				// float archon_dist = 0;
				//
				// RobotInfo[] near_bots =
				// rc.senseNearbyRobots(-1,rc.getTeam());
				////
				// for (int x = 0; x < near_bots.length; x++) {
				// RobotInfo this_bot = near_bots[x];
				// if(this_bot.getType()==RobotType.ARCHON){
				// float distance =
				// myLocation.distanceSquaredTo(this_bot.location);
				// if(distance<archon_dist){
				// archon_dist = distance;
				// }
				// }
				// }

				myLocation = rc.getLocation();

				RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

				if (robots.length > 0) {
					MapLocation enemyLocation = robots[0].getLocation();
					rc.broadcast(2, rc.getRoundNum());
					rc.broadcast(3, (int) enemyLocation.x);
					rc.broadcast(4, (int) enemyLocation.y);

				}

				ti = rc.senseNearbyTrees(-1, rc.getTeam());

				int close_trees = 0;
				int far_trees = 0;
				int neutral_trees = rc.senseNearbyTrees(-1, Team.NEUTRAL).length;

				for (int x = 0; x < ti.length; x++) {
					TreeInfo this_tree = ti[x];
					float distance_to = myLocation.distanceSquaredTo(this_tree.location);
					if (distance_to < 5) {
						close_trees++;
					} else if (distance_to < 30) {
						far_trees++;
					}
				}

				float min = 50f;
				for (TreeInfo t : ti) {
					if (t.health < min) {
						min = t.health;
					}
					if (t.health < 40 && rc.canWater(t.ID))
						rc.water(t.ID);
				}

				if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && neutral_trees > 5 && rc.isBuildReady()) {
					rc.buildRobot(RobotType.LUMBERJACK, dir);
				}

				if (close_trees == 0 && far_trees == 0 && rc.canPlantTree(dir)) {
					tree_hub = true;
				}

				if (tree_hub == true && close_trees < 5 && rc.canPlantTree(dir)) {
					rc.plantTree(dir);
					dir = dir.rotateLeftDegrees(60);
					close_trees++;

				} else {
					if (tree_hub == false) {
						tryMove(randomDirection());
					} else {
						if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && neutral_trees > 5 && rc.isBuildReady()) {
							rc.buildRobot(RobotType.LUMBERJACK, dir);
						} else {
							if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && rc.isBuildReady()) {
								rc.buildRobot(RobotType.LUMBERJACK, dir);
							}
						}
					}
					// rc.buildRobot(RobotType.SOLDIER, dir);
					// } else
					// if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) &&
					// rand256() <4 && rc.isBuildReady()) {
					// rc.buildRobot(RobotType.LUMBERJACK, dir);
					// }
					// else if (rc.canBuildRobot(RobotType.SCOUT, dir) &&
					// rand256() < 0 && rc.isBuildReady()) {
					// rc.buildRobot(RobotType.SCOUT, dir);
					// } else if (rc.canBuildRobot(RobotType.TANK, dir) &&
					// rand256() < 0 && rc.isBuildReady()) {
					// rc.buildRobot(RobotType.TANK, dir);
					// }
				}

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
		System.out.println("SOLDIER");
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
						tryMove(d);
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

	static void runTank() throws GameActionException {
		System.out.println("TANK");
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
							if (myLocation.isWithinDistance(new MapLocation(x, y), 5)) {
								rc.broadcast(2, 0);
							}
							d = new Direction(x - myLocation.x, y - myLocation.y);
							tryMove(d);
							if (rc.canFireSingleShot()) {
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
				System.out.println("TANK EXCEPTION");
				e.printStackTrace();
			}
		}
	}

	static void runScout() throws GameActionException {
		System.out.println("SCOUT");
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
				System.out.println("SCOUT EXCEPTION");
				e.printStackTrace();
			}
		}
	}

	static void runLumberjack() throws GameActionException {
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
				dodge();

				if (rc.readBroadcast(10) != -1&&!rc.hasMoved()) {
					int x = rc.readBroadcast(11);
					int y = rc.readBroadcast(12);
					tryMove(new Direction(x - myLocation.x, y - myLocation.y));
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
							tryMove(toEnemy);
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
				System.out.println("Jinchao Exception");
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
		return new Direction((float) (rand256() * 2.0f * 0.0122718463));
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
		if (Math.abs(theta) > 1.57079632679) {
			return false;
		}

		// distToRobot is our hypotenuse, theta is our angle, and we want to
		// know this length of the opposite leg.
		// This is the distance of a line that goes from myLocation and
		// intersects perpendicularly with propagationDirection.
		// This corresponds to the smallest radius circle centered at our
		// location that would intersect with the
		// line that is the path of the bullet.
		float perpendicularDist = (float) Math.abs(distToRobot * xsin(theta)); // soh
		// cah
		// toa
		// :)

		return (perpendicularDist <= rc.getType().bodyRadius);
	}
}