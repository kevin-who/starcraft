package marines;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TreeInfo;

public class Gardener {

	MapLocation myLocation;
	RobotController rc;
	MapLocation enemyLocation;
	boolean scout = true;

	void phase1() {
		Team enemy = rc.getTeam().opponent();
		Direction dir = Global.rndDir();
		MapLocation start = rc.getLocation();
		MapLocation[] enemies = rc.getInitialArchonLocations(enemy);
		int closest = 0;
		float min = 10000000;
		for (int e = 0; e < enemies.length; e++) {
			float temp = start.distanceTo(enemies[e]);
			if (temp < min) {
				closest = e;
				min = temp;
			}
		}

		enemyLocation = rc.getInitialArchonLocations(enemy)[closest];

		int count = 0;
		while (true) {
			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {
				myLocation = rc.getLocation();

				if (scout && rc.getRoundNum() < 100 && rc.getTeamBullets() > RobotType.SCOUT.bulletCost) {
					dir = Global.rndDir();
					for (int x = 0; x < 18; x++) {
						dir = dir.rotateLeftDegrees(20);
						if (rc.canBuildRobot(RobotType.SCOUT, dir)) {
							rc.buildRobot(RobotType.SCOUT, dir);
						}

					}
					scout = false;
				}

				if (count < 30 && start.distanceTo(myLocation) < 7) {
					Global.tryMove(myLocation.directionTo(enemyLocation), 30, 6);
					count++;
				} else {
					if (rc.isCircleOccupiedExceptByThisRobot(myLocation, 5) && rc.senseNearbyTrees(5).length < 1) {
						break;
					}

					if (rc.senseNearbyRobots(5).length < 1 && rc.senseNearbyTrees(5, rc.getTeam()).length < 1) {
						dir = Global.rndDir();
						if (rc.getTeamBullets() > RobotType.LUMBERJACK.bulletCost) {
							for (int x = 0; x < 18; x++) {
								dir = dir.rotateLeftDegrees(20);
								if (rc.canBuildRobot(RobotType.LUMBERJACK, dir)) {
									rc.buildRobot(RobotType.LUMBERJACK, dir);
									break;
								}

							}
							if (!rc.isBuildReady()) {
								break;
							}
						}
					} else if (!rc.hasMoved() && !Global.tryMove(dir, 20, 10) && rc.senseNearbyRobots(5f).length < 1) {
						if (rc.getTeamBullets() > RobotType.LUMBERJACK.bulletCost) {
							for (int x = 0; x < 18; x++) {
								dir = dir.rotateLeftDegrees(20);
								if (rc.canBuildRobot(RobotType.LUMBERJACK, dir)) {
									rc.buildRobot(RobotType.LUMBERJACK, dir);
									break;
								}

							}
							if (!rc.isBuildReady()) {
								break;
							}
							dir = Global.rndDir();

						}
					}
				}
				Clock.yield();

			} catch (Exception e) {
				System.out.println("Gardener Exception");
				e.printStackTrace();
			}
		}

	}

	void phase2() {

		Team enemy = rc.getTeam().opponent();
		Direction dir = myLocation.directionTo(enemyLocation).rotateLeftDegrees(-30);
		myLocation = rc.getLocation();
		TreeInfo[] ti;

		while (true) {
			try {

				if (scout && rc.getRoundNum() < 100 && rc.getTeamBullets() > RobotType.SCOUT.bulletCost) {
					dir = Global.rndDir();
					for (int x = 0; x < 18; x++) {
						dir = dir.rotateLeftDegrees(20);
						if (rc.canBuildRobot(RobotType.SCOUT, dir)) {
							rc.buildRobot(RobotType.SCOUT, dir);
						}

					}
				}

				if ((int) (rc.getTeamBullets() / 10) + rc.getTeamVictoryPoints() >= 1000) {

					rc.donate(rc.getTeamBullets());
				}
				myLocation = rc.getLocation();

				RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

				if (robots.length > 0) {
					enemyLocation = robots[0].getLocation();
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
					}

				} else {
					if (rc.canBuildRobot(RobotType.SOLDIER, dir) && FastMath.rand256() < 60) {
						rc.buildRobot(RobotType.SOLDIER, dir);
					}
					// else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) &&
					// FastMath.rand256() < 100) {
					// rc.buildRobot(RobotType.LUMBERJACK, dir);
					// } else if (rc.canBuildRobot(RobotType.SCOUT, dir) &&
					// FastMath.rand256() < 3) {
					// rc.buildRobot(RobotType.SCOUT, dir);
					// }
				}

				Clock.yield();

			} catch (Exception e) {
				System.out.println("Gardener Exception");
				e.printStackTrace();
			}
		}
	}

	void run() {
		System.out.println("I'm a gardener!");
		phase1();
		phase2();
	}

	public Gardener(RobotController rc) {
		this.rc = rc;
		Global.rc = rc;
		run();
	}

}
