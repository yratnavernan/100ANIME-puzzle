package puzzlegame;

import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class ImageUtils {
    public static BufferedImage loadLevelImage(int level, String folder) throws IOException {
        File imageFile = new File(folder + "/level" + level + ".jpg");
        if (!imageFile.exists()) {
            throw new FileNotFoundException("Image for level " + level + " not found");
        }
        return ImageIO.read(imageFile);
    }
}
