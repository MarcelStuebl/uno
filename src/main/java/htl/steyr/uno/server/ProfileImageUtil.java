package htl.steyr.uno.server;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileImageUtil {


    /**
     * Creates a small, square PNG thumbnail of a profile image.
     * Profile images can be up to 10 MB, so they are scaled down before they are sent to clients that only display small avatars.
     * The image is cropped to a centered square first, so the thumbnail is not distorted.
     * @param imageData The original image (PNG or JPEG).
     * @param size The width and height of the thumbnail in pixels.
     * @return The thumbnail as PNG, or null if the image could not be read.
     */
    public static byte[] createThumbnail(byte[] imageData, int size) {
        if (imageData == null || imageData.length == 0) {
            return null;
        }

        try {
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(imageData));
            if (source == null) {
                return null;
            }

            int side = Math.min(source.getWidth(), source.getHeight());
            BufferedImage thumbnail = source.getSubimage((source.getWidth() - side) / 2, (source.getHeight() - side) / 2, side, side);

            // Halve the image step by step: a single large bilinear step looks pixelated,
            // and Image.SCALE_SMOOTH is several times slower on large photos.
            int currentSize = side;
            do {
                currentSize = Math.max(currentSize / 2, size);
                thumbnail = scale(thumbnail, currentSize);
            } while (currentSize > size);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, "png", out);
            return out.toByteArray();
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }


    /**
     * Scales an image to a square of the given size using bilinear interpolation.
     * @param image The image to scale.
     * @param size The width and height of the scaled image in pixels.
     * @return The scaled image.
     */
    private static BufferedImage scale(BufferedImage image, int size) {
        BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, size, size, null);
        g.dispose();
        return scaled;
    }
}
