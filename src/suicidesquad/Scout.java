package suicidesquad;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TreeInfo;

public class Scout {

	MapLocation myLoc;
	RobotController rc;

	void run() {
		Team team = rc.getTeam();
		Team enemy = team.opponent();
		myLoc = rc.getLocation();
		Global.loc = myLoc;
		MapLocation[] enemies = rc.getInitialArchonLocations(enemy);
		int closest = 0;
		float min = 10000000;
		for (int e = 0; e < enemies.length; e++) {
			float temp = myLoc.distanceTo(enemies[e]);
			if (temp < min) {
				closest = e;
				min = temp;
			}
		}
		MapLocation enemyBase = rc.getInitialArchonLocations(enemy)[closest];
		boolean move;
		boolean toBase = true;

		// The code you want your robot to perform every round should be in this
		// loop
		Direction dir = myLoc.directionTo(enemyBase);

		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {
				myLoc = rc.getLocation();
				Global.loc = myLoc;

				if (!rc.hasMoved()) {
					Global.dodge();
				}
				move = true;

				RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

				if (robots.length > 0) {
					closest = 0;
					boolean target = false;
					RobotInfo enemyGardener;
					for (int x = 0; x < robots.length; x++) {
						RobotInfo bot = robots[x];
						if (bot.getType().equals(RobotType.GARDENER)) {
							closest = x;
							target = true;
							break;
						}

					}
					if (target) {
						enemyGardener = robots[closest];
						MapLocation gardenerLocation = enemyGardener.getLocation();
						if (!rc.hasMoved() && myLoc.distanceTo(gardenerLocation) > RobotType.GARDENER.bodyRadius
								+ RobotType.GARDENER.bodyRadius + .2)
							Global.goTo(gardenerLocation);

						myLoc = rc.getLocation();
						Direction toGardener = myLoc.directionTo(gardenerLocation);
						if (myLoc.distanceTo(gardenerLocation) <= RobotType.GARDENER.bodyRadius
								+ RobotType.GARDENER.bodyRadius + .2 && rc.canFireSingleShot()) {
							rc.fireSingleShot(toGardener);
						}
						move = false;
						toBase = false;
					} else {
						MapLocation enemyLocation = robots[0].getLocation();
						Direction toEnemy = myLoc.directionTo(enemyLocation);

						rc.broadcast(2, rc.getRoundNum());
						rc.broadcast(3, (int) enemyLocation.x);
						rc.broadcast(4, (int) enemyLocation.y);

						if (!rc.hasMoved() && myLoc.distanceTo(enemyLocation) < 7) {
							Global.tryMove(toEnemy.opposite());
							myLoc = rc.getLocation();
							toEnemy = myLoc.directionTo(enemyLocation);
						}
						if (rc.canFireSingleShot()) {
							rc.fireSingleShot(toEnemy);
						}
						toBase = false;
					}
				}
				if (!rc.hasMoved() && move) {
					if (toBase)
						Global.goTo(enemyBase);
					else if (!Global.tryMove(dir, 0, 0)) {
						dir = Global.rndDir();
					}
				}

				TreeInfo[] ti = rc.senseNearbyTrees(-1, Team.NEUTRAL);
				if (ti.length > 0) {
					for (TreeInfo t : ti) {
						if (rc.canShake(t.ID)) {
							rc.shake(t.ID);
							break;
						}
					}
				}

				Clock.yield();

			} catch (Exception e) {
				System.out.println("Scout Exception");
				e.printStackTrace();
			}
		}

	}

	public Scout(RobotController rc) {
		this.rc = rc;
		Global.rc = rc;
		run();
	}

}
