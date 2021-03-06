package bytecounter;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.Team;

public strictfp class RobotPlayer {
	static RobotController rc;

	public static void run(RobotController rc) throws GameActionException {
		if (rc.getTeam().equals(Team.B)) {
			int count = 0;
			double ans = 0;
			for (double x = -3.1; x < 3.1; x+=.1) {
				Clock.yield();
				ans = Math.abs(x);
				count = Clock.getBytecodeNum();
				System.out.println("F: " + x + ", " + ans + ", " + count);

				Clock.yield();
				ans = Double.longBitsToDouble(Double.doubleToRawLongBits(x) & 0x7FFFFFFFFFFFl);
				count = Clock.getBytecodeNum();
				System.out.println("N: " + x + ", " + ans + ", " + count);

			}
		} else {
			while (true) {
				Clock.yield();
			}
		}

	}

}