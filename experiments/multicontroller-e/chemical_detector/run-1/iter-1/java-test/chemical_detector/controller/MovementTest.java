package chemical_detector.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import chemical_detector.actuator.Vehicle;
import chemical_detector.constants.ChemConstants;
import chemical_detector.data.Angle;
import chemical_detector.data.Chem;
import chemical_detector.data.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.Sensor;
import org.junit.jupiter.api.Test;

class MovementTest {

    private final Sensor sensor = new Sensor(new Chem(1));
    private final Clock timer = new Clock();
    private final Vehicle vehicle = new Vehicle();
    private final Movement movement = new Movement(sensor, timer, vehicle, new ChangeDirection(vehicle));

    @Test
    void waitingRandomWalksAndIgnoresResume() {
        movement.step(null);
        assertEquals(MovementMode.Waiting, movement.currentMode());
        assertTrue(vehicle.randomWalking());
        movement.step(new InputEvent.Resume());
        assertEquals(MovementMode.Waiting, movement.currentMode());
    }

    @Test
    void turnStartsAndRedirectsMotion() {
        movement.step(new InputEvent.Turn(Angle.Left));
        assertEquals(MovementMode.Going, movement.currentMode());
        assertEquals(Angle.Left, vehicle.heading());
        assertEquals(ChemConstants.LV, vehicle.velocity());

        movement.step(new InputEvent.Turn(Angle.Back));
        assertEquals(MovementMode.Going, movement.currentMode());
        assertEquals(Angle.Back, movement.a());
        assertEquals(Angle.Back, vehicle.heading());

        movement.step(new InputEvent.Resume());
        assertEquals(MovementMode.Waiting, movement.currentMode());
    }

    @Test
    void obstacleTriggersAvoidance() {
        movement.step(new InputEvent.Turn(Angle.Front));
        sensor.updateOdometer(4.0);
        movement.step(new InputEvent.Obstacle(Loc.left));
        assertEquals(MovementMode.Avoiding, movement.currentMode());
        assertEquals(Loc.left, movement.l());
        assertEquals(4.0, movement.d0());
        assertEquals(Angle.Right, vehicle.heading());
        assertEquals(ChemConstants.EVADE_TIME, vehicle.lastPause());

        movement.step(new InputEvent.Turn(Angle.Front));
        assertEquals(MovementMode.TryingAgain, movement.currentMode());
        assertEquals(Angle.Front, vehicle.heading());
    }

    @Test
    void secondObstacleSoonAfterFirstKeepsAvoiding() {
        enterAvoidingAgain(1.0, 1);
        movement.step(null);
        assertEquals(MovementMode.Avoiding, movement.currentMode());
        assertEquals(Angle.Left, vehicle.heading());
    }

    @Test
    void secondObstacleAfterGoodProgressKeepsAvoiding() {
        enterAvoidingAgain(ChemConstants.STUCK_DIST + 1.0, ChemConstants.STUCK_PERIOD);
        movement.step(null);
        assertEquals(MovementMode.Avoiding, movement.currentMode());
    }

    @Test
    void stuckRobotGetsOut() {
        enterAvoidingAgain(ChemConstants.STUCK_DIST, ChemConstants.STUCK_PERIOD);
        movement.step(null);
        assertEquals(MovementMode.GettingOut, movement.currentMode());
        assertTrue(vehicle.shortRandomWalking());
        assertEquals(ChemConstants.OUT_PERIOD, vehicle.lastPause());

        movement.step(new InputEvent.Turn(Angle.Right));
        assertEquals(MovementMode.Going, movement.currentMode());
        assertEquals(Angle.Right, vehicle.heading());
    }

    @Test
    void stopHaltsAndFlagsOnce() {
        movement.step(new InputEvent.Turn(Angle.Right));
        movement.step(new InputEvent.Stop());
        assertEquals(MovementMode.Found, movement.currentMode());
        assertEquals(1, vehicle.flags());
        assertEquals(0.0, vehicle.velocity());

        movement.step(new InputEvent.Stop());
        movement.step(new InputEvent.Turn(Angle.Left));
        movement.step(null);
        assertEquals(MovementMode.Found, movement.currentMode());
        assertEquals(1, vehicle.flags());
        assertEquals(0.0, vehicle.velocity());
    }

    @Test
    void stopPreemptsStuckDetection() {
        enterAvoidingAgain(0.0, ChemConstants.STUCK_PERIOD);
        movement.step(new InputEvent.Stop());
        assertEquals(MovementMode.Found, movement.currentMode());
    }

    private void enterAvoidingAgain(double progress, long elapsed) {
        movement.step(new InputEvent.Turn(Angle.Front));
        sensor.updateOdometer(10.0);
        movement.step(new InputEvent.Obstacle(Loc.front));
        movement.step(new InputEvent.Turn(Angle.Front));
        timer.advance(elapsed);
        sensor.updateOdometer(10.0 + progress);
        movement.step(new InputEvent.Obstacle(Loc.right));
        assertEquals(MovementMode.AvoidingAgain, movement.currentMode());
        assertEquals(10.0 + progress, movement.d1());
    }
}
