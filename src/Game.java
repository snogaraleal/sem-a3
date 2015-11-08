import becker.robots.*;

import java.awt.*;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Game logic.
 */
public class Game {

    /**
     * Direction in which the player can move.
     */
    public enum Direction {
        UP("Up", 0),
        RIGHT("Right", 1),
        DOWN("Down", 2),
        LEFT("Left", 3);

        private String name;
        private int turns;

        /**
         * Initialize {@code Direction} with the specified name.
         * @param name Name of the direction
         * @param turns Number of equivalent right turns
         */
        Direction(String name, int turns) {
            this.name = name;
            this.turns = turns;
        }

        /**
         * Get direction name.
         * @return Name
         */
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return getName();
        }

        /**
         * Difference between two {@code Direction}.
         */
        public static class Delta {

            private int turns;
            private Direction direction;

            /**
             * Initialize {@code Delta}.
             * @param turns Number of turns
             * @param direction Direction of turns
             */
            public Delta(int turns, Direction direction) {
                this.turns = turns;
                this.direction = direction;
            }

            /**
             * Get number of turns.
             * @return Turns
             */
            public int getTurns() {
                return turns;
            }

            /**
             * Get direction.
             * @return Direction
             */
            public Direction getDirection() {
                return direction;
            }
        }

        /**
         * Get {@code Delta} between this direction and the specified direction.
         * @param target Final direction
         * @return {@code Delta} from this direction
         */
        public Delta delta(Direction target) {
            int turns = target.turns - this.turns;

            Direction direction = turns < 0 ? Direction.LEFT : Direction.RIGHT;
            turns = Math.abs(turns);

            if (turns > 2) {
                turns = Math.abs(turns - 4);

                // Swap direction
                direction = direction == Direction.LEFT ?
                        Direction.RIGHT : Direction.LEFT;
            }

            return new Delta(turns, direction);
        }

        /**
         * Get random {@code Direction}.
         * @return Random direction
         */
        public static Direction random() {
            switch (ThreadLocalRandom.current().nextInt(4)) {
                case 0: return UP;
                case 1: return RIGHT;
                case 2: return DOWN;
                case 3: return LEFT;
            }

            throw new RuntimeException();
        }

        /**
         * Get Becker Robots API equivalent.
         * @return Equivalent {@code becker.robots.Direction}
         * @throws NoSuchElementException
         */
        public becker.robots.Direction toBecker()
                throws NoSuchElementException {

            switch (this) {
                case UP:
                    return becker.robots.Direction.NORTH;
                case DOWN:
                    return becker.robots.Direction.SOUTH;
                case LEFT:
                    return becker.robots.Direction.WEST;
                case RIGHT:
                    return becker.robots.Direction.EAST;
            }

            throw new NoSuchElementException();
        }

        /**
         * Get from Becker Robots API equivalent.
         * @param direction {@code becker.robots.Direction}
         * @return Direction
         * @throws NoSuchElementException
         */
        public static Direction fromBecker(becker.robots.Direction direction)
                throws NoSuchElementException {

            switch (direction) {
                case NORTH:
                    return Direction.UP;
                case SOUTH:
                    return Direction.DOWN;
                case WEST:
                    return Direction.LEFT;
                case EAST:
                    return Direction.RIGHT;
            }

            throw new NoSuchElementException();
        }
    }

    /**
     * Difficulty mode.
     */
    public enum Mode {
        EASY("Easy", 0.25),
        MEDIUM("Medium", 0.5),
        HARD("Hard", 1);

        public static Mode DEFAULT = EASY;
        public static Mode[] ALL = new Mode[] { EASY, MEDIUM, HARD };

        private String name;
        private double speed;

        /**
         * Initialize {@code Mode} with the specified name.
         * @param name Mode name
         * @param speed Speed
         */
        Mode(String name, double speed) {
            this.name = name;
            this.speed = speed;
        }

        /**
         * Get difficulty mode name.
         * @return Mode name
         */
        public String getName() {
            return name;
        }

        /**
         * Get difficulty mode speed.
         * @return Mode speed
         */
        public double getSpeed() {
            return speed;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    /**
     * Base controller {@code Thread}.
     */
    public static abstract class Controller extends Thread {

        protected volatile boolean running;

        /**
         * Initialize {@code Controller}.
         */
        public Controller() {
            this.running = false;
        }

        /**
         * Perform next move.
         * @throws InterruptedException
         */
        public abstract void next() throws InterruptedException;

        @Override
        public void run() {
            this.running = true;
            while (this.running) {
                try {
                    next();
                } catch (InterruptedException exception) {
                    this.running = false;
                }
            }
        }

        /**
         * Finish {@code Controller} thread.
         */
        public void terminate() {
            this.interrupt();
            this.running = false;
        }
    }

    /**
     * {@code Controller} for game world that detects proximity and
     * collision between two robots.
     */
    public static class WorldController extends Controller {

        /**
         * Action {@code Listener}.
         */
        public interface Listener {
            /**
             * Indicate proximity between robots.
             */
            void onProximityChanged(boolean proximity);

            /**
             * Indicate collision between robots.
             */
            void onCollisionChanged(boolean collision);
        }

        private static final int INTERVAL = 1;
        private static final int PROXIMITY = 2;

        protected RobotSE user;
        protected RobotSE enemy;

        protected Listener listener;

        public WorldController(RobotSE user, RobotSE enemy, Listener listener) {
            super();

            this.user = user;
            this.enemy = enemy;

            this.listener = listener;
        }

        @Override
        public void next() throws InterruptedException {

            listener.onCollisionChanged(
                user.getIntersection() == enemy.getIntersection());

            listener.onProximityChanged(
                (Math.abs(user.getAvenue() - enemy.getAvenue()) <= PROXIMITY) &&
                (Math.abs(user.getStreet() - enemy.getStreet()) <= PROXIMITY));

            Thread.sleep(INTERVAL);
        }
    }

    /**
     * {@code Controller} for a moving robot.
     */
    public static abstract class RobotController extends Controller {

        protected RobotSE robot;

        protected int width;
        protected int height;

        /**
         * Initialize {@code Controller}.
         * @param robot Robot to control
         * @param width Width boundary
         * @param height Height boundary
         */
        public RobotController(RobotSE robot, int width, int height) {
            super();

            this.robot = robot;

            this.width = width;
            this.height = height;
        }
    }

    /**
     * {@code RobotController} for a robot that moves randomly.
     */
    public static class EnemyRobotController extends RobotController {

        /**
         * Initialize {@code EnemyRobotController}.
         * @param robot Robot to control
         * @param width Width boundary
         * @param height Height boundary
         */
        public EnemyRobotController(RobotSE robot, int width, int height) {
            super(robot, width, height);
        }

        @Override
        public void next() throws InterruptedException {
            Random random = ThreadLocalRandom.current();

            if (random.nextBoolean()) {
                robot.turnLeft();
            } else {
                robot.turnRight();
            }

            int x = robot.getAvenue();
            int y = robot.getStreet();

            int steps = random.nextInt(4);

            // Check boundaries
            switch (robot.getDirection()) {
                case NORTH:
                    if ((y - steps) < 0) steps = y;
                    break;
                case WEST:
                    if ((x - steps) < 0) steps = x;
                    break;
                case SOUTH:
                    if ((y + steps) > height) steps = height - y;
                    break;
                case EAST:
                    if ((x + steps) > width) steps = width - x;
                    break;
            }

            robot.move(steps);
        }
    }

    /**
     * {@code RobotController} for a robot that moves based on specific input.
     */
    public static class UserRobotController extends RobotController {

        private BlockingQueue<Direction> queue = new LinkedBlockingQueue<>();

        /**
         * Initialize {@code UserRobotController}.
         * @param robot Robot to control
         * @param width Width boundary
         * @param height Height boundary
         */
        public UserRobotController(RobotSE robot, int width, int height) {
            super(robot, width, height);
        }

        /**
         * Indicate move in the specified direction.
         * @param direction Direction in which to move
         */
        public void enqueue(Direction direction) {
            queue.add(direction);
        }

        @Override
        public void next() throws InterruptedException {
            handle(queue.take());
        }

        /**
         * Perform move with robot.
         * @param next Direction in which to move
         */
        private void handle(Direction next) {
            Direction current = Direction.fromBecker(robot.getDirection());

            if (current == next) {
                int x = robot.getAvenue();
                int y = robot.getStreet();

                // Check boundaries
                switch (robot.getDirection()) {
                    case NORTH:
                        if (y == 0) return;
                        break;
                    case WEST:
                        if (x == 0) return;
                        break;
                    case SOUTH:
                        if (y == height) return;
                        break;
                    case EAST:
                        if (x == width) return;
                        break;
                }

                robot.move();
            } else {
                Direction.Delta delta = current.delta(next);

                switch (delta.getDirection()) {
                    case LEFT:
                        robot.turnLeft(delta.getTurns());
                        break;

                    case RIGHT:
                        robot.turnRight(delta.getTurns());
                        break;
                }
            }
        }
    }

    private int width;
    private int height;

    private WorldController.Listener listener;

    private RobotSE enemy;
    private RobotSE user;

    private WorldController worldController;
    private EnemyRobotController enemyController;
    private UserRobotController userController;

    /**
     * Initialize {@code Game}.
     * @param city City in which the game is played
     * @param width Width boundary
     * @param height Height boundary
     * @param listener {@code WorldController.Listener}
     */
    public Game(
            City city, int width, int height,
            WorldController.Listener listener) {

        city.setSize(width, height);

        this.width = width;
        this.height = height;

        this.listener = listener;

        Random random = ThreadLocalRandom.current();

        // Create robots
        enemy = new RobotSE(
                city, random.nextInt(width), random.nextInt(height),
                Direction.random().toBecker());
        user = new RobotSE(
                city, random.nextInt(width), random.nextInt(height),
                Direction.random().toBecker());

        // Set robot colors
        enemy.setColor(Color.RED);
        user.setColor(Color.BLUE);
    }

    /**
     * Set difficulty mode.
     * @param mode Mode
     */
    public void setMode(Mode mode) {
        double speed = user.getSpeed();
        speed *= mode.getSpeed();
        enemy.setSpeed(speed);
    }

    /**
     * Start game.
     */
    public void start() {
        worldController = new WorldController(user, enemy, listener);
        enemyController = new EnemyRobotController(enemy, width, height);
        userController = new UserRobotController(user, width, height);

        worldController.start();
        enemyController.start();
        userController.start();
    }

    /**
     * Stop game.
     */
    public void stop() {
        worldController.terminate();
        enemyController.terminate();
        userController.terminate();
    }

    /**
     * Get user robot.
     * @return User robot
     */
    public RobotSE getUser() {
        return user;
    }

    /**
     * Get user robot controller.
     * @return User robot controller.
     */
    public UserRobotController getUserController() {
        return userController;
    }
}