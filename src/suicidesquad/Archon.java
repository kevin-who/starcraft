package suicidesquad;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class Archon {

	MapLocation myLoc;
	RobotController rc;

	void run() {
		System.out.println("I'm an archon!");
		Team team = rc.getTeam();
		Team enemy = team.opponent();
		myLoc = rc.getLocation();
		MapLocation[] enemies = rc.getInitialArchonLocations(enemy);
		int number = enemies.length;
		int closest = 0;
		float min = 10000000;
		for (int e = 0; e < enemies.length; e++) {
			float temp = myLoc.distanceTo(enemies[e]);
			if (temp < min) {
				closest = e;
				min = temp;
			}
		}
		MapLocation enemyLocation = rc.getInitialArchonLocations(enemy)[closest];

		// The code you want your robot to perform every round should be in this
		// loop
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {
				myLoc = rc.getLocation();
				Global.loc = myLoc;
				Global.dodge();
				int num = rc.getRoundNum();

				RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

				if (robots.length > 0) {
					enemyLocation = robots[0].getLocation();
					rc.broadcast(2, num);
					rc.broadcast(3, (int) enemyLocation.x);
					rc.broadcast(4, (int) enemyLocation.y);

				}
				// Generate a random direction
				Direction dir = Global.rndDir();

				if (!rc.hasMoved())
					Global.tryMove(dir, 0, 0);

				dir = myLoc.directionTo(enemyLocation);
				if (rc.getRobotCount() < 2 * number && rc.canHireGardener(dir)) {
					rc.hireGardener(dir);
				} else if (rc.canHireGardener(dir) && (rc.getTreeCount() > 1&&rc.getRoundNum()>200) && FastMath.rand256() < 30) {
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

	public Archon(RobotController rc) {
		this.rc = rc;
		Global.rc = rc;
		run();
	}

}
