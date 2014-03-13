package kyleestes;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class Scat extends AdvancedRobot {
	/**
	 * Represents the enemy robot's current energy level.
	 */
	double previousEnergy = 100;
	
	/**
	 * Represents a movement direction, where positive is ahead and negative is behind.
	 */
	int movementDirection = 1;
		
	/**
	 * Represents a gun turn direction, where positive is clockwise and negative is counter-clockwise.
	 */
	int gunDirection = 1;

	
	/* (non-Javadoc)
	 * @see robocode.Robot#run()
	 */
	public void run() {
		setTurnGunRight(99999);
	}

	/* (non-Javadoc)
	 * @see robocode.Robot#onScannedRobot(robocode.ScannedRobotEvent)
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// Stay at right angles to the opponent.
		setTurnRight(e.getBearing() + 90 - 30 * movementDirection);

		// If the bot has small energy drop, assume it fired.
		double changeInEnergy = previousEnergy - e.getEnergy();
		
		if (changeInEnergy > 0 && changeInEnergy <= 3) {
			// Dodge!
			movementDirection = -movementDirection;
			
			setAhead((e.getDistance() / 4 + 25) * movementDirection);
		}
		
		// When a bot is spotted, sweep the gun and radar.
		gunDirection = -gunDirection;
		setTurnGunRight(99999 * gunDirection);

		// Fire directly at target.
		fire(2);

		// Track the energy level.
		previousEnergy = e.getEnergy();
	}
}
