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

        protected DestroyableRobot user;
        protected DestroyableRobot enemy;

        protected Listener listener;

        /**
         * Initialize {@code WorldController}.
         * @param user User robot
         * @param enemy Enemy robot
         * @param listener {@code Listener}
         */
        public WorldController(
                DestroyableRobot user, DestroyableRobot enemy,
                Listener listener) {

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

        protected DestroyableRobot robot;

        protected int width;
        protected int height;

        /**
         * Initialize {@code Controller}.
         * @param robot Robot to control
         * @param width Width boundary
         * @param height Height boundary
         */
        public RobotController(DestroyableRobot robot, int width, int height) {
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
        public EnemyRobotController(DestroyableRobot robot, int width, int height) {
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

        /**
         * {@code Task} that can be performed by the user robot.
         */
        public static class Task {

            /**
             * Action to perform.
             */
            public enum Command {
                PICK,
                MOVE,
            }

            private Command command;
            private Direction direction;

            /**
             * Initialize {@code Task}.
             * @param command Action to perform
             */
            public Task(Command command) {
                this.command = command;
            }

            public Task(Command command, Direction direction) {
                this(command);
                this.direction = direction;
            }

            /**
             * Get action to perform.
             * @return Action
             */
            public Command getCommand() {
                return command;
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
         * Action {@code Listener}.
         */
        public interface Listener {
            /**
             * Indicate something was picked.
             */
            void onThingPicked();
        }

        private BlockingQueue<Task> queue = new LinkedBlockingQueue<>();

        protected Listener listener;

        /**
         * Initialize {@code UserRobotController}.
         * @param robot Robot to control
         * @param width Width boundary
         * @param height Height boundary
         * @param listener Action listener
         */
        public UserRobotController(
                DestroyableRobot robot, int width, int height,
                Listener listener) {

            super(robot, width, height);
            this.listener = listener;
        }

        /**
         * Indicate move in the specified direction.
         * @param task Task to perform
         */
        public void enqueue(Task task) {
            queue.add(task);
        }

        @Override
        public void next() throws InterruptedException {
            Task task = queue.take();
            switch (task.getCommand()) {
                case PICK: pick(); break;
                case MOVE: move(task.getDirection()); break;
            }
        }

        /**
         * Perform pick action.
         */
        private void pick() {
            if (robot.canPickThing()) {
                robot.pickThing();

                listener.onThingPicked();
            }
        }

        /**
         * Perform move with robot.
         * @param next Direction in which to move
         */
        private void move(Direction next) {
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

    private static final String PRIZE_ICON_PATH = "prize.jpeg";

    private City city;
    private Mode mode;
    private int width;
    private int height;

    private DestroyableRobot enemy;
    private DestroyableRobot user;

    private WorldController worldController;
    private EnemyRobotController enemyController;
    private UserRobotController userController;

    private WorldController.Listener worldListener;
    private UserRobotController.Listener userListener;

    private boolean running = false;

    /**
     * Initialize {@code Game}.
     * @param city City in which the game is played
     * @param mode Initial difficulty mode
     * @param width Width boundary
     * @param height Height boundary
     * @param worldListener {@code WorldController.Listener}
     * @param userListener {@code UserRobotController.Listener}
     */
    public Game(
            City city, Mode mode, int width, int height,
            WorldController.Listener worldListener,
            UserRobotController.Listener userListener) {

        this.city = city;
        this.city.setSize(width, height);
        this.mode = mode;

        this.width = width;
        this.height = height;

        this.worldListener = worldListener;
        this.userListener = userListener;

        Random random = ThreadLocalRandom.current();

        // Create user
        user = new DestroyableRobot(
                city, random.nextInt(width), random.nextInt(height),
                Direction.random().toBecker());
        user.setColor(Color.BLUE);
    }

    /**
     * Set difficulty mode.
     * @param mode Mode
     */
    public void setMode(Mode mode) {
        this.mode = mode;

        // Change enemy speed right away
        double speed = user.getSpeed();
        speed *= mode.getSpeed();
        enemy.setSpeed(speed);
    }

    /**
     * Initialize game.
     */
    public void reset() {
        Random random = ThreadLocalRandom.current();

        // Create enemy
        enemy = new DestroyableRobot(
                city, random.nextInt(width), random.nextInt(height),
                Direction.random().toBecker());
        enemy.setColor(Color.RED);

        // Create thing
        Thing thing = new Thing(
                city, random.nextInt(width), random.nextInt(height));
        thing.setIcon(new ImageIcon(PRIZE_ICON_PATH));

        // Reflect difficulty mode
        setMode(this.mode);
    }

    /**
     * Start game.
     */
    public void start() {
        running = true;

        worldController = new WorldController(
                user, enemy, worldListener);
        enemyController = new EnemyRobotController(
                enemy, width, height);
        userController = new UserRobotController(
                user, width, height, userListener);

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

        worldController = null;
        enemyController = null;
        userController = null;

        running = false;
    }

    /**
     * Get whether the game is running.
     * @return Running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Get user robot.
     * @return User robot
     */
    public DestroyableRobot getUser() {
        return user;
    }

    /**
     * Get enemy robot.
     * @return Enemy question
     */
    public DestroyableRobot getEnemy() {
        return enemy;
    }

    /**
     * Get user robot controller.
     * @return User robot controller
     */
    public UserRobotController getUserController() {
        return userController;
    }
}