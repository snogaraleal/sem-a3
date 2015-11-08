import becker.robots.City;
import becker.robots.Direction;
import becker.robots.RobotSE;

public class DestroyableRobot extends RobotSE {

    public static final String MESSAGE = "Destroyed";

    public DestroyableRobot(City city, int x, int y, Direction direction) {
        super(city, x, y, direction);
    }

    public void destroy() {
        breakRobot(MESSAGE);
    }
}
