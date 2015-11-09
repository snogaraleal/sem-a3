import becker.robots.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;

public class Main implements
        UI.ControlPanel.Listener,
        UI.ActionsMenu.Listener,
        UI.SettingsMenu.Listener,

        Game.UserRobotController.Listener,
        Game.WorldController.Listener {

    public static void main(String[] args) {
        new Main().start();
    }

    /*
     * Frame
     */
    private static final String WIN_TITLE = "Game";
    private static final int WIN_WIDTH = 640;
    private static final int WIN_HEIGHT = 480;

    /*
     * Dialog
     */
    private static final String DIALOG_TITLE = "Game Over";
    private static final String DIALOG_WIN = "You win! \n Restart game?";
    private static final String DIALOG_LOSE = "You lose! \n Restart game?";

    /*
     * City
     */
    private static final int CITY_WIDTH = 10;
    private static final int CITY_HEIGHT = 10;
    private static final int CITY_ZOOM = 30;

    /*
     * Border
     */
    private static final int BORDER_WIDTH = 10;
    private static final Border BORDER_ALERT =
            new BorderUIResource.LineBorderUIResource(Color.RED, BORDER_WIDTH);
    private static final Border BORDER_NORMAL =
            new BorderUIResource.EmptyBorderUIResource(
                    BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH);

    private Game game;
    private CityView view;

    /**
     * Start application.
     */
    public void start() {
        City.showFrame(false);
        City city = new City();

        RobotUIComponents components = new RobotUIComponents(city);
        components.getZoom().setValue(CITY_ZOOM);
        components.getStartStopButton().doClick();

        UI.ControlPanel control = new UI.ControlPanel(this);

        // Create main menu bar
        JMenuBar menu = new JMenuBar();
        menu.add(new UI.ActionsMenu(this));
        menu.add(new UI.SettingsMenu(this));

        // Create main panel
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        view = components.getCityView();
        view.setBorder(BORDER_NORMAL);
        root.add(view);
        root.add(control);

        // Create main frame
        JFrame frame = new JFrame(WIN_TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(root);
        frame.setJMenuBar(menu);
        frame.setSize(WIN_WIDTH, WIN_HEIGHT);
        frame.pack();
        frame.setVisible(true);

        // Create and start game
        game = new Game(
                city, Game.Mode.DEFAULT,
                CITY_WIDTH, CITY_HEIGHT, this, this);
        game.setMode(Game.Mode.DEFAULT);
        game.reset();
        game.start();
    }

    /*
     * {@code UI.ControlPanel.Listener}
     */

    @Override
    public void onDirectionChange(Game.Direction direction) {
        game.getUserController().enqueue(new Game.UserRobotController.Task(
                Game.UserRobotController.Task.Command.MOVE, direction));
    }

    @Override
    public void onPick() {
        game.getUserController().enqueue(new Game.UserRobotController.Task(
                Game.UserRobotController.Task.Command.PICK));
    }

    /*
     * {@code UI.ActionsMenu.Listener}
     */

    @Override
    public void onPause() {
        if (game.isRunning()) {
            game.stop();
        } else {
            game.start();
        }
    }

    @Override
    public void onRestart() {
        game.stop();
        game.reset();
        game.start();
    }

    @Override
    public void onQuit() {
        System.exit(0);
    }

    /*
     * {@code UI.SettingsMenu.Listener}
     */

    @Override
    public void onModeChange(Game.Mode mode) {
        game.setMode(mode);
    }

    /*
     * {@code Game.UserRobotController.Listener}
     */

    @Override
    public void onThingPicked() {
        game.getEnemy().destroy();

        int result = JOptionPane.showConfirmDialog(null,
                DIALOG_WIN, DIALOG_TITLE, JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            onRestart();
        } else {
            onQuit();
        }
    }

    /*
     * {@code Game.WorldController.Listener}
     */

    @Override
    public void onProximityChanged(boolean proximity) {
        if (proximity) {
            view.setBorder(BORDER_ALERT);
        } else {
            view.setBorder(BORDER_NORMAL);
        }
    }

    @Override
    public void onCollisionChanged(boolean collision) {
        if (collision) {
            game.getUser().destroy();

            int result = JOptionPane.showConfirmDialog(null,
                DIALOG_LOSE, DIALOG_TITLE, JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                onRestart();
            } else {
                onQuit();
            }
        }
    }
}