import becker.robots.icons.Icon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * {@code Icon} displaying an image.
 */
public class ImageIcon extends Icon {

    private Image image;

    /**
     * Initialize {@code ImageIcon}.
     * @param path Path to image file
     */
    public ImageIcon(String path) {
        super();

        try {
            image = ImageIO.read(new File(path));
        } catch (IOException exception) {
            image = null;
        }
    }

    @Override
    public Image getImage(int width, int height, double rotation) {
        return image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }
}
