package chemical_detector.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.constants.Constants;
import chemical_detector.data.Angle;
import chemical_detector.data.Chem;
import chemical_detector.data.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.sensor.Sensor;
import chemical_detector.timing.Clock;
import java.util.List;
import org.junit.jupiter.api.Test;

class MovementControllerTest {

    private static final InputEvent IDLE = new InputEvent.NoEvent();

    private final Sensor sensor = new Sensor(new Chem(1));
    private final Vehicle vehicle = new Vehicle();
    private final Actuator out = new Actuator();
    private final Clock time = new Clock();
    private final MovementController mv = new MovementController(sensor, vehicle, out, time);

    private void reachTryingAgain() {
        mv.step(new InputEvent.Turn(Angle.Left));
        sensor.updateOdometer(2.0);
        mv.step(new InputEvent.Obstacle(Loc.front));
        mv.step(new InputEvent.Turn(Angle.Right));
        assertEquals(MovementMode.TryingAgain, mv.currentMode());
    }

    @Test
    void waitingRandomWalksUntilTurned() {
        mv.step(IDLE);
        assertEquals(MovementMode.Waiting, mv.currentMode());
        assertTrue(vehicle.randomWalking());

        mv.step(new InputEvent.Resume());
        assertEquals(MovementMode.Waiting, mv.currentMode());

        mv.step(new InputEvent.Turn(Angle.Back));
        assertEquals(MovementMode.Going, mv.currentMode());
        assertEquals(Angle.Back, vehicle.direction());
        assertEquals(Constants.LV, vehicle.velocity());
    }

    @Test
    void goingReissuesMoveOnTurnAndAvoidsObstacles() {
        mv.step(new InputEvent.Turn(Angle.Left));
        mv.step(new InputEvent.Turn(Angle.Front));
        assertEquals(MovementMode.Going, mv.currentMode());
        assertEquals(Angle.Front, vehicle.direction());

        time.advance(5);
        sensor.updateOdometer(3.0);
        mv.step(new InputEvent.Obstacle(Loc.left));
        assertEquals(MovementMode.Avoiding, mv.currentMode());
        assertEquals(Loc.left, mv.l());
        assertEquals(3.0, mv.d0());
        assertEquals(5L, mv.evasionStart());
        assertEquals(Angle.Right, vehicle.direction());
        assertEquals(Constants.EVADE_TIME, vehicle.lastPause());
    }

    @Test
    void secondObstacleSoonAfterFirstReturnsToAvoiding() {
        reachTryingAgain();
        mv.step(new InputEvent.Obstacle(Loc.right));
        assertEquals(MovementMode.AvoidingAgain, mv.currentMode());
        assertEquals(2.0, mv.d1());

        mv.step(IDLE);
        assertEquals(MovementMode.Avoiding, mv.currentMode());
        assertEquals(Angle.Left, vehicle.direction());
    }

    @Test
    void secondObstacleAfterStuckPeriodWithoutProgressGetsOut() {
        reachTryingAgain();
        mv.step(new InputEvent.Obstacle(Loc.right));
        time.advance(Constants.STUCK_PERIOD);
        mv.step(IDLE);
        assertEquals(MovementMode.GettingOut, mv.currentMode());
        assertTrue(vehicle.shortRandomWalking());
        assertEquals(Constants.OUT_PERIOD, vehicle.lastPause());

        mv.step(new InputEvent.Turn(Angle.Front));
        assertEquals(MovementMode.Going, mv.currentMode());
    }

    @Test
    void secondObstacleAfterStuckPeriodWithProgressReturnsToAvoiding() {
        reachTryingAgain();
        sensor.updateOdometer(2.0 + Constants.STUCK_DIST + 0.5);
        mv.step(new InputEvent.Obstacle(Loc.right));
        time.advance(Constants.STUCK_PERIOD);
        mv.step(IDLE);
        assertEquals(MovementMode.Avoiding, mv.currentMode());
    }

    @Test
    void resumeReturnsToWaitingFromEveryActiveMode() {
        mv.step(new InputEvent.Turn(Angle.Left));
        mv.step(new InputEvent.Resume());
        assertEquals(MovementMode.Waiting, mv.currentMode());

        reachTryingAgain();
        mv.step(new InputEvent.Resume());
        assertEquals(MovementMode.Waiting, mv.currentMode());
    }

    @Test
    void stopHaltsFlagsOnceAndStaysFound() {
        reachTryingAgain();
        out.drain();
        mv.step(new InputEvent.Stop());
        assertEquals(MovementMode.Found, mv.currentMode());
        assertEquals(List.of(new OutputEvent.Flag()), out.drain());
        assertEquals(0.0, vehicle.velocity());
        assertFalse(vehicle.randomWalking());

        mv.step(new InputEvent.Obstacle(Loc.left));
        mv.step(new InputEvent.Turn(Angle.Back));
        mv.step(new InputEvent.Resume());
        assertEquals(MovementMode.Found, mv.currentMode());
        assertEquals(Loc.left, mv.l());
        assertEquals(0.0, vehicle.velocity());
        assertTrue(out.drain().isEmpty());
    }
}
