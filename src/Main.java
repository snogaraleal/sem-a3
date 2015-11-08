import becker.robots.*;

import javax.swing.*;

public class Main implements
        UI.ControlPanel.Listener,
        UI.ActionsMenu.Listener,
        UI.SettingsMenu.Listener {

    public static void main(String[] args) {
        new Main().start();
    }

    private static final int CITY_WIDTH = 10;
    private static final int CITY_HEIGHT = 10;
    private static final int CITY_ZOOM = 30;

    private static final String WIN_TITLE = "Game";
    private static final int WIN_WIDTH = 640;
    private static final int WIN_HEIGHT = 480;

    private Game game;

    public void start() {
        City.showFrame(false);
        City city = new City();

        RobotUIComponents components = new RobotUIComponents(city);
        components.getZoom().setValue(CITY_ZOOM);
        components.getStartStopButton().doClick();

        UI.ControlPanel control = new UI.ControlPanel(this);

        JMenuBar menu = new JMenuBar();
        menu.add(new UI.ActionsMenu(this));
        menu.add(new UI.SettingsMenu(this));

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.add(components.getCityView());
        root.add(control);

        JFrame frame = new JFrame(WIN_TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(root);
        frame.setJMenuBar(menu);
        frame.setSize(WIN_WIDTH, WIN_HEIGHT);
        frame.pack();
        frame.setVisible(true);

        game = new Game(city, CITY_WIDTH, CITY_HEIGHT);
        game.getUser().setSpeed(10);
        game.setMode(Game.Mode.DEFAULT);
        game.start();
    }

    @Override
    public void onDirectionChange(Game.Direction direction) {
        game.getUserController().enqueue(direction);
    }

    @Override
    public void onPick() {
    }

    @Override
    public void onPause() {
        game.stop();
    }

    @Override
    public void onRestart() {
        game.start();
    }

    @Override
    public void onQuit() {
        System.exit(0);
    }

    @Override
    public void onModeChange(Game.Mode mode) {
        game.setMode(mode);
    }
}