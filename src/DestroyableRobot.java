import becker.robots.City;
import becker.robots.Direction;
import becker.robots.RobotException;
import becker.robots.RobotSE;

public class DestroyableRobot extends RobotSE {

    public static final String MESSAGE = "Destroyed";

    public DestroyableRobot(City city, int x, int y, Direction direction) {
        super(city, x, y, direction);
    }

    /**
     * Destroy robot.
     */
    public void destroy() {
        try {
            breakRobot(MESSAGE);
        } catch (RobotException exception) {
            // Robot destroyed
        }
    }

    /**
     * Remove robot.
     */
    public void remove() {
        destroy();
        setIcon(null);
    }
}
