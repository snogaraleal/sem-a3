import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * User interface components.
 */
public class UI {

    /**
     * {@code GridBagConstraints} with default values.
     */
    private static class SimpleGridBagConstraints extends GridBagConstraints {

        private static Insets INSETS = new Insets(0, 0, 0, 0);
        private static double WEIGHT = 1.0;
        private static int PAD = 0;

        /**
         * Initialize {@code SimpleGridBagConstraints}.
         * @see {@link GridBagConstraints}
         */
        public SimpleGridBagConstraints(int x, int y, int width, int height) {
            super(x, y, width, height,
                    WEIGHT, WEIGHT,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    INSETS, PAD, PAD);
        }
    }

    /**
     * Panel with buttons for changing direction and picking up.
     */
    public static class ControlPanel extends JPanel {

        /**
         * Action listener.
         */
        public interface Listener {
            /**
             * Change direction.
             * @param direction New direction
             */
            void onDirectionChange(Game.Direction direction);

            /**
             * Pick up object.
             */
            void onPick();
        }

        private Listener listener;

        /**
         * {@code AbstractAction} representing a change in direction.
         */
        public static class Action extends AbstractAction {

            private ControlPanel control;
            private Game.Direction direction;

            /**
             * Initialize {@code Action}.
             * @param control {@code ControlPanel} used to change direction
             * @param direction Game.Direction in which to change
             */
            public Action(ControlPanel control, Game.Direction direction) {
                super(direction.getName());

                this.control = control;
                this.direction = direction;
            }

            @Override
            public void actionPerformed(ActionEvent event) {
                control.listener.onDirectionChange(direction);
            }
        }

        private GridBagLayout layout = new GridBagLayout();

        private JButton up = new JButton(new Action(this, Game.Direction.UP));
        private JButton down = new JButton(new Action(this, Game.Direction.DOWN));
        private JButton left = new JButton(new Action(this, Game.Direction.LEFT));
        private JButton right = new JButton(new Action(this, Game.Direction.RIGHT));
        private JButton pick = new JButton();

        /**
         * Initialize {@code ControlPanel}.
         * @param listener {@link Listener} to handle actions
         */
        public ControlPanel(Listener listener) {
            super();

            this.listener = listener;

            setLayout(layout);

            add(up, new SimpleGridBagConstraints(1, 0, 1, 1));
            add(down, new SimpleGridBagConstraints(1, 2, 1, 1));
            add(left, new SimpleGridBagConstraints(0, 1, 1, 1));
            add(right, new SimpleGridBagConstraints(2, 1, 1, 1));
            add(pick, new SimpleGridBagConstraints(1, 1, 1, 1));

            bind();
        }

        /**
         * Bind additional events to underlying {@code Listener}.
         */
        private void bind() {
            pick.setAction(new AbstractAction("Pick") {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    listener.onPick();
                }
            });
        }
    }

    /**
     * Menu providing basic actions.
     */
    public static class ActionsMenu extends JMenu {

        /**
         * Action listener.
         */
        public interface Listener {
            /**
             * Pause game.
             */
            void onPause();

            /**
             * Restart game.
             */
            void onRestart();

            /**
             * Quit game.
             */
            void onQuit();
        }

        private Listener listener;

        private JMenuItem pause = new JMenuItem();
        private JMenuItem restart = new JMenuItem();
        private JMenuItem quit = new JMenuItem();

        /**
         * Initialize {@code ActionsMenu}.
         * @param listener {@link Listener} to handle actions
         */
        public ActionsMenu(Listener listener) {
            super("Actions");

            this.listener = listener;

            add(pause);
            add(restart);
            add(new JSeparator());
            add(quit);

            bind();
        }

        /**
         * Bind additional events to underlying {@code Listener}.
         */
        private void bind() {
            pause.setAction(new AbstractAction("Pause") {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    listener.onPause();
                }
            });

            restart.setAction(new AbstractAction("Restart") {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    listener.onRestart();
                }
            });

            quit.setAction(new AbstractAction("Quit") {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    listener.onQuit();
                }
            });
        }
    }

    /**
     * Menu providing difficulty mode selection.
     */
    public static class SettingsMenu extends JMenu {

        /**
         * Action listener.
         */
        public interface Listener {
            /**
             * Change difficulty mode.
             * @param mode Difficulty mode
             */
            void onModeChange(Game.Mode mode);
        }

        protected Listener listener;

        /**
         * {@code AbstractAction} representing a change in difficulty.
         */
        public static class Action extends AbstractAction {

            private SettingsMenu menu;
            private Game.Mode mode;

            /**
             * Initialize {@code Action}.
             * @param menu {@code SettingsMenu} used to change difficulty
             * @param mode Difficulty mode changed to
             */
            public Action(SettingsMenu menu, Game.Mode mode) {
                super(mode.getName());

                this.menu = menu;
                this.mode = mode;
            }

            @Override
            public void actionPerformed(ActionEvent event) {
                menu.listener.onModeChange(mode);
            }
        }

        private ButtonGroup group = new ButtonGroup();

        /**
         * Initialize {@code SettingsMenu}.
         * @param listener {@link Listener} to handle actions
         */
        public SettingsMenu(Listener listener) {
            super("Settings");

            this.listener = listener;

            for (Game.Mode mode : Game.Mode.ALL) {
                addModeButton(mode);
            }
        }

        /**
         * Add difficulty change button.
         * @param mode Target difficulty mode
         */
        private void addModeButton(Game.Mode mode) {
            JRadioButton button = new JRadioButton(new Action(this, mode));
            button.setSelected(mode == Game.Mode.DEFAULT);
            add(button);
            group.add(button);
        }
    }
}