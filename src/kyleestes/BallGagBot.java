package kyleestes;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/**
 * This robot likes bondage and wears a ball gag during battle.
 * 
 * @author Kyle Estes
 */
public class BallGagBot extends AdvancedRobot {
	/**
	 * Represents the enemy robot's current energy level.
	 */
	private double enemyCurrentEnergyLevel = 100;

	/**
	 * Determines whether to rotate the tank nose toward or away from the enemy.
	 * This also affects whether the robot will move forward or backward when
	 * dodging.
	 */
	private boolean adjustHeadingTowardEnemy = true;

	/**
	 * Represents a radar bearing modulator for our robot, where positive is
	 * clockwise and negative is counter-clockwise.
	 */
	private int radarBearingModulator = 1;

	/**
	 * Represents the power of the bullets we fire.
	 */
	private double bulletPower = 3;

	/**
	 * Represents the number of radians in a circle.
	 */
	private static final double RADIANS_IN_CIRCLE = Math.PI * 2;

	/**
	 * Represents the arc length of our radar scans.
	 */
	private static final double RADAR_SCAN_ARC_LENGTH = RADIANS_IN_CIRCLE
			+ Math.PI / 2;

	/**
	 * Represents the magnitude of the angle needed to face the tank
	 * perpendicular to an enemy when the tank is directly facing the enemy.
	 */
	private static final double ADJUST_HEADING_TO_PERPENDICULAR = Math.PI / 2;

	/**
	 * Represents the magnitude of the angle to adjust the tank slightly
	 * off-perpendicular.
	 */
	private static final double ADJUST_HEADING = Math.PI / 6;

	/**
	 * Run the robot.
	 * 
	 * <p>
	 * Note that there is no need for an infinite loop, nor is there a need to
	 * call {@link robocode.AdvancedRobot#execute() execute}. The initial scan
	 * will locate the enemy robot, which will trigger the event handler. The
	 * event handler moves our robot, points our gun and radar, and continuously
	 * re-generates new scan events, thus causing the event handler to repeat
	 * continuously. The event handler does not call
	 * {@link robocode.AdvancedRobot#execute() execute} either, because
	 * {@link robocode.AdvancedRobot#execute() execute} is called by default
	 * when the turn (i.e., tick) is complete.
	 * </p>
	 * 
	 * @see robocode.Robot#run()
	 */
	public void run() {
		// Decouple the gun movement from the tank movement.
		setAdjustGunForRobotTurn(true);

		// Decouple the radar movement from the gun movement.
		setAdjustRadarForGunTurn(true);

		// Perform an initial scan.
		setTurnRadarRightRadians(RADAR_SCAN_ARC_LENGTH);
	}

	/**
	 * Handle scanned robot events.
	 * 
	 * <p>
	 * Performs linear targeting to fire at the projected position of the enemy.
	 * </p>
	 * 
	 * @see robocode.Robot#onScannedRobot(robocode.ScannedRobotEvent)
	 * @see <a href='http://robowiki.net/wiki/Linear_Targeting'>Linear
	 *      Targeting</a>
	 * @see <a href='http://robowiki.net/wiki/Maximum_Escape_Angle'>Maximum
	 *      Escape Angle</a>
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		/*
		 * Lay in a new heading based on our bearing to the enemy robot.
		 */

		// Determine a heading for the tank perpendicular to our bearing to the
		// enemy. In other words, rotate the nose of the tank such that the
		// enemy is directly port-side.
		double newTankHeading = e.getBearingRadians()
				+ ADJUST_HEADING_TO_PERPENDICULAR;

		if (adjustHeadingTowardEnemy) {
			// Turn the nose of the tank towards the enemy. If the enemy fires,
			// we will want to dodge by moving forward so as to get closer to
			// the enemy.
			newTankHeading = newTankHeading - ADJUST_HEADING;
		} else {
			// Turn the nose of the tank away from the enemy. If the enemy
			// fires, we will want to dodge by moving backward so as to get
			// closer to the enemy.
			newTankHeading = newTankHeading + ADJUST_HEADING;
		}

		// Lay in the new heading.
		setTurnRightRadians(newTankHeading);

		/*
		 * Determine if the enemy fired a shot, and if so, dodge.
		 */

		// Determine the change in the enemy's energy level.
		double deltaEnemyEnergyLevel = enemyCurrentEnergyLevel - e.getEnergy();

		// If the enemy's energy level has dropped, assume it fired. Note that
		// these energy drops may be false-positives from the enemy hitting a
		// wall or another robot. For now, it is good enough.
		if (deltaEnemyEnergyLevel >= Rules.MIN_BULLET_POWER
				&& deltaEnemyEnergyLevel <= Rules.MAX_BULLET_POWER) {

			// Determine a distance to move ahead in order to dodge the bullet.
			double moveDistance = e.getDistance() / 4 + 25;

			// Next time, switch things up to keep the enemy guessing. This
			// achieves a sort of see-saw effect when dodging successive
			// bullets.
			adjustHeadingTowardEnemy = !adjustHeadingTowardEnemy;

			// If the nose of the tank is pointing away from the enemy...
			if (!adjustHeadingTowardEnemy) {
				// Negate the move distance and cause the tank to move backward
				// instead of forward.
				moveDistance = -moveDistance;
			}

			// Engage.
			setAhead(moveDistance);
		}

		// Update the enemy's current energy level.
		enemyCurrentEnergyLevel = e.getEnergy();

		/*
		 * Scan the enemy with the radar.
		 */

		// Whichever way the radar was scanning, reverse it so as to sweep back
		// across the enemy.
		radarBearingModulator = -radarBearingModulator;

		// Sweep the radar.
		setTurnRadarRightRadians(RADAR_SCAN_ARC_LENGTH * radarBearingModulator);

		/*
		 * Fire at the enemy's future position as determined by a linear
		 * extrapolation.
		 */

		// Determine a new heading for the gun that points at the enemy's
		// current location.
		double newGunHeading = getHeadingRadians() + e.getBearingRadians();

		// If we shoot with the gun at this heading, and the enemy is moving,
		// the bullet will miss. We need to adjust the gun heading so that it
		// points at the enemy's future location at the time a bullet fired from
		// the gun would arrive at that location.
		newGunHeading = newGunHeading
				+ Math.asin(e.getVelocity() / Rules.getBulletSpeed(bulletPower)
						* Math.sin(e.getHeadingRadians() - newGunHeading));

		// Aim the gun.
		setTurnGunRightRadians(Utils.normalRelativeAngle(newGunHeading
				- getGunHeadingRadians()));

		// Fire.
		setFire(bulletPower);
	}
}
