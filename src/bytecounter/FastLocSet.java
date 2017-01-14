package bytecounter;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;

public class FastLocSet {
	private static final int HASH = Math.max(GameConstants.MAP_MAX_WIDTH, GameConstants.MAP_MAX_HEIGHT);
	private boolean[][] has = new boolean[HASH][HASH];

	public void add(MapLocation loc) {
		int x = (int) loc.x % HASH;
		int y = (int) loc.y % HASH;
		if (!has[x][y]) {
			has[x][y] = true;
		}
	}

	public void remove(MapLocation loc) {
		int x = (int) loc.x % HASH;
		int y = (int) loc.y % HASH;
		if (has[x][y]) {
			has[x][y] = false;
		}
	}

	public boolean contains(MapLocation loc) {
		return has[(int) loc.x % HASH][(int) loc.y % HASH];
	}

	public void clear() {
		has = new boolean[HASH][HASH];
	}
}
