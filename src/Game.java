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
         * Get equivalent to Becker Robots API.
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
     * Game boundary.
     */
    public static class Boundary {

        private int width;
        private int height;

        /**
         * Initialize {@code Boundary}.
         * @param width Width
         * @param height Height
         */
        public Boundary(int width, int height) {
            this.width = width;
            this.height = height;
        }

        /**
         * Change size and apply walls to city.
         * @param city City to apply boundaries to
         */
        public void apply(City city) {
            city.setSize(width, height);

            for (int i = 0; i <= width; i++) {
                new Wall(city, 0, i, becker.robots.Direction.NORTH);
                new Wall(city, height, i, becker.robots.Direction.SOUTH);
            }

            for (int i = 0; i <= height; i++) {
                new Wall(city, i, 0, becker.robots.Direction.WEST);
                new Wall(city, i, width, becker.robots.Direction.EAST);
            }
        }

        /**
         * Get random {@code X} value.
         * @return Value
         */
        public int randomX() {
            Random random = ThreadLocalRandom.current();
            return random.nextInt(width);
        }

        /**
         * Get random {@code Y} value.
         * @return Value
         */
        public int randomY() {
            Random random = ThreadLocalRandom.current();
            return random.nextInt(height);
        }

        /**
         * Get whether a move starting in the specified position towards
         * the specified direction is allowed given this boundaries.
         *
         * @param x Initial position {@code X}
         * @param y Initial position {@code Y}
         * @param direction Direction
         * @return Whether the move is allowed
         */
        public boolean isMoveAllowed(int x, int y, Direction direction) {
            switch (direction) {
                case UP:
                    if (y == 0) return false;
                    break;

                case LEFT:
                    if (x == 0) return false;
                    break;

                case DOWN:
                    if (y == height) return false;
                    break;

                case RIGHT:
                    if (x == width) return false;
                    break;
            }

            return true;
        }

        /**
         * Limit the number of steps that can be taken starting in the
         * specified position towards the specified direction.
         *
         * @param x Initial position {@code X}
         * @param y Initial position {@code Y}
         * @param steps Intended number of steps
         * @param direction Direction
         * @return Number of steps
         */
        public int limitMove(int x, int y, int steps, Direction direction) {
            switch (direction) {
                case UP:
                    if ((y - steps) < 0) steps = y;
                    break;

                case LEFT:
                    if ((x - steps) < 0) steps = x;
                    break;

                case DOWN:
                    if ((y + steps) > height) steps = height - y;
                    break;

                case RIGHT:
                    if ((x + steps) > width) steps = width - x;
                    break;
            }

            return steps;
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
        protected Boundary boundary;

        /**
         * Initialize {@code Controller}.
         * @param robot Robot to control
         * @param boundary Boundaries
         */
        public RobotController(DestroyableRobot robot, Boundary boundary) {
            super();

            this.robot = robot;
            this.boundary = boundary;
        }
    }

    /**
     * {@code RobotController} for a robot that moves randomly.
     */
    public static class EnemyRobotController extends RobotController {

        /**
         * Initialize {@code EnemyRobotController}.
         * @param robot Robot to control
         * @param boundary Boundaries
         */
        public EnemyRobotController(DestroyableRobot robot, Boundary boundary) {
            super(robot, boundary);
        }

        @Override
        public void next() throws InterruptedException {
            Random random = ThreadLocalRandom.current();

            if (random.nextBoolean()) {
                robot.turnLeft();
            } else {
                robot.turnRight();
            }

            int steps = boundary.limitMove(
                    robot.getAvenue(), robot.getStreet(), random.nextInt(4),
                    Direction.fromBecker(robot.getDirection()));

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

        private boolean bounded;
        protected Listener listener;

        /**
         * Initialize {@code UserRobotController}.
         * @param robot Robot to control
         * @param boundary Boundaries
         * @param bounded Whether moves are limited to boundary
         * @param listener Action listener
         */
        public UserRobotController(
                DestroyableRobot robot, Boundary boundary,
                boolean bounded, Listener listener) {

            super(robot, boundary);

            this.bounded = bounded;
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
                if (!bounded || boundary.isMoveAllowed(
                        robot.getAvenue(), robot.getStreet(),
                        Direction.fromBecker(robot.getDirection()))) {

                    robot.move();
                }
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
    private static final double USER_SPEED = 10;
    private static final boolean USER_BOUNDED = true;

    private City city;
    private Mode mode;
    private Boundary boundary;

    private DestroyableRobot enemy;
    private DestroyableRobot user;
    private Thing thing;

    private WorldController worldController;
    private EnemyRobotController enemyController;
    private UserRobotController userController;

    private WorldController.Listener worldListener;
    private UserRobotController.Listener userListener;

    private boolean running = false;

    /**
     * Initialize {@code Game}.
     *
     * @param city City in which the game is played
     * @param mode Initial difficulty mode
     * @param boundary Boundaries
     *
     * @param worldListener {@code WorldController.Listener}
     * @param userListener {@code UserRobotController.Listener}
     */
    public Game(
            City city, Mode mode, Boundary boundary,
            WorldController.Listener worldListener,
            UserRobotController.Listener userListener) {

        this.city = city;
        this.mode = mode;
        this.boundary = boundary;

        this.boundary.apply(this.city);

        this.worldListener = worldListener;
        this.userListener = userListener;
    }

    /**
     * Set difficulty mode.
     * @param mode Mode
     */
    public void setMode(Mode mode) {
        this.mode = mode;
        this.reflectMode();
    }

    /**
     * Reflect difficulty mode.
     */
    private void reflectMode() {
        // Configure enemy
        if (enemy != null) {
            enemy.setSpeed(USER_SPEED * mode.getSpeed());
        }

        // Configure user
        if (user != null) {
            user.setSpeed(USER_SPEED);
        }
    }

    /**
     * Initialize game.
     */
    public void reset() {
        // Create enemy
        if (enemy != null) {
            enemy.remove();
        }
        enemy = new DestroyableRobot(
                city, boundary.randomX(), boundary.randomY(),
                Direction.random().toBecker());
        enemy.setColor(Color.RED);

        // Create user
        if (user != null) {
            user.remove();
        }
        user = new DestroyableRobot(
                city, boundary.randomX(), boundary.randomY(),
                Direction.random().toBecker());
        user.setColor(Color.BLUE);

        // Create thing
        if (thing != null) {
            thing.setIcon(null);
        }
        thing = new Thing(city, boundary.randomX(), boundary.randomY());
        thing.setIcon(new ImageIcon(PRIZE_ICON_PATH));

        reflectMode();
    }

    /**
     * Start game.
     */
    public void start() {
        running = true;

        worldController = new WorldController(user, enemy, worldListener);
        enemyController = new EnemyRobotController(enemy, boundary);
        userController = new UserRobotController(
                user, boundary, USER_BOUNDED, userListener);

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