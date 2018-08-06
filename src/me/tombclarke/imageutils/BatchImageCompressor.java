package me.tombclarke.imageutils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple tool to help resize and compress images
 *
 * @author Tom Clarke
 */
public class BatchImageCompressor {

    private final ArrayList<File> allImages;

    public BatchImageCompressor(File folder) throws ImageException, FileNotFoundException {
        // Check initial folder is ok
        if (!folder.exists()) {
            throw new FileNotFoundException("Specified directory does not exist.");
        }
        if (!folder.isDirectory()) {
            throw new ImageException("Specified directory is not a directory.");
        }
        if (folder.list() == null || folder.list().length == -1) {
            throw new ImageException("Specified directory is empty.");
        }

        // Set things up
        allImages = new ArrayList<>();

        // Get list of images
        ImageUtils.getListOfImages(folder, allImages);
    }

    public static void main(String[] args) throws IOException, ImageException {
        File folder = null;
        int width = -1;
        int height = -1;
        boolean ignoreAspectRatio = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-f":
                case "-folder":
                    i++;
                    folder = new File(args[i]);
                    break;
                case "-w":
                case "-width":
                    i++;
                    width = Integer.valueOf(args[i]);
                    break;
                case "-h":
                case "-height":
                    i++;
                    height = Integer.valueOf(args[i]);
                    break;
                case "-a":
                    i++;
                    ignoreAspectRatio = Boolean.valueOf(args[i]);
                    break;
                case "-help":
                    System.out.println("usage: BatchImageCompressor -f <target_folder> [-w <width> -h <height> [-a]]");
                    System.out.println("-f <target_folder> Specifies a folder to look for images in to compress");
                    System.out.println("-w <width> Specifies a new (max) width of pictures");
                    System.out.println("-h <height> Specifies a new (max) height of pictures");
                    System.out.println("-a If set, aspect ration will be ignored");
                    System.out.println("-help Show help");
                    System.out.println("");
                    System.exit(0);
                default:
                    System.out.println("Unknown argument: " + args[i]);
            }
        }

        if (folder == null) {
            System.out.println("No folder supplied! Run with -help to see help.");
            System.exit(-1);
        }

        BatchImageCompressor compressor = new BatchImageCompressor(folder);

        // Resize
        if (width > -1 || height > -1) {
            if (width == -1 || height == -1) {
                System.out.println("Both a width and height must be specified for resizing! Run with -h to see help.");
                System.exit(-1);
            }

            compressor.compressImages(width, height, !ignoreAspectRatio);
        }
    }

    /**
     * Compresses all images to the width and height given. If maintainAspectRatio is set, the other parameters are treated as 'max'
     *
     * @param width               The (max) width of the resized image
     * @param height              The (max) height of the resized image
     * @param maintainAspectRatio Whether or not to maintain the aspect ratio
     */
    public void compressImages(int width, int height, boolean maintainAspectRatio) throws IOException, ImageException {
        for (File i : allImages) {
            // Read the image
            BufferedImage originalImage = ImageIO.read(i);
            int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

            // Calculate new width / height if aspect ration is 'on'
            if (maintainAspectRatio) {
                double wRatio = (double) width / (double) originalImage.getWidth();
                double hRatio = (double) height / (double) originalImage.getHeight();

                double transformRatio = Double.min(wRatio, hRatio);
                width = (int) (transformRatio * (double) originalImage.getWidth());
                height = (int) (transformRatio * (double) originalImage.getHeight());
            }

            // Resize into new buffered image
            BufferedImage resizedImage = new BufferedImage(width, height, type);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, width, height, null);
            g.dispose();
            g.setComposite(AlphaComposite.Src);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Save the resized image
            String formatName;
            int index = i.getName().lastIndexOf('.');
            if (index > 0) {
                formatName = i.getName().substring(index + 1);
            } else {
                throw new ImageException("A image without an format name cannot be resized.");
            }
            ImageIO.write(resizedImage, formatName, i);
        }
    }
}
