package me.tombclarke.imageutils;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/*
 * Finds and removes bad images from a given folder
 *
 * @author Tom Clarke
 */
public class BatchBadImageRemover {

    private final File folder;
    private final List<File> allImages;
    private final List<File> corruptImages;
    private final List<File> halfCorruptImages;

    public BatchBadImageRemover(File folder) throws ImageException, FileNotFoundException {
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
        this.folder = folder;
        allImages = new ArrayList<>();
        corruptImages = new ArrayList<>();
        halfCorruptImages = new ArrayList<>();

        // Get list of images
        ImageUtils.getListOfImages(folder, allImages);
    }

    public static void main(String[] args) throws IOException, ImageException {
        File folder = null;
        boolean autoDelete = false;
        String autoMove = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-f":
                case "-folder":
                    i++;
                    folder = new File(args[i]);
                    break;
                case "-d":
                case "-delete":
                    autoDelete = true;
                    break;
                case "-m":
                case "-move":
                    i++;
                    autoMove = args[i];
                    break;
                case "-h":
                case "-help":
                    System.out.println("usage: BatchBadImageRemover -f <target_folder> [-d] [-m <move_folder>] [-h]");
                    System.out.println("-f <target_folder> Specifies a folder to look for images in to check");
                    System.out.println("-d Automatically delete all corrupted files");
                    System.out.println("-m <move_folder> Specifies a folder to automatically move the corrupt pictures to");
                    System.out.println("-h Show help");
                    System.exit(0);
                default:
                    System.out.println("Unknown argument: " + args[i]);
            }
        }

        if (folder == null) {
            System.out.println("No folder supplied! Run with -h to see help.");
            System.exit(-1);
        }

        BatchBadImageRemover imgRemover = new BatchBadImageRemover(folder);
        System.out.println("BatchBadImageRemover initialised, found " + imgRemover.allImages.size() + " images to process.");

        // Find obviously corrupt files
        imgRemover.findCorruptImages();
        System.out.println("BatchBadImageRemover found " + imgRemover.corruptImages.size() + " corrupt images.");
        // Find likely corrupt files
//        imgRemover.findLikelyCorruptImages();
//        System.out.println("BatchBadImageRemover found " + imgRemover.halfCorruptImages.size() + " liked corrupt images.");

        boolean cleaned = false;
        // Ask about deleting, or default action?
        if (autoDelete) {
            imgRemover.deleteBadImages();
            System.out.println("Removed.");
            cleaned = true;
        } else {
            System.out.println("Do you want to delete bad images? Y/N");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().toLowerCase();
            boolean waiting = true;
            while (waiting) {
                if (input.equals("y")) {
                    imgRemover.deleteBadImages();
                    System.out.println("Removed.");
                    waiting = false;
                    cleaned = true;
                } else if (input.equals("n")) {
                    waiting = false;
                }
            }
        }
        if (!cleaned) {
            if (autoMove != null) {
                imgRemover.moveBadImages(autoMove);
                System.out.println("Moved to " + autoMove + ".");
            } else {
                System.out.println("Do you want to move bad images? <folder>/N");
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine();
                boolean waiting = true;
                while (waiting) {
                    if (input.equals("n") || input.equals("N")) {
                        waiting = false;
                    } else {
                        try {
                            imgRemover.moveBadImages(input);
                            System.out.println("Moved to " + input + ".");
                            waiting = false;
                        } catch (ImageException | IOException e) {
                            System.out.println("Could not move images to given folder.");
                            System.out.println("Do you want to move bad images? <folder>/N");
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks all images and moves corrupt files to the corrupt list
     */
    public void findCorruptImages() {
        List<File> imagesToMarkCorrupted = new ArrayList<>();
        for (File imgToCheck : allImages) {
            try {
                BufferedImage image = ImageIO.read(imgToCheck);
                if (image == null) {
                    // Couldn't decode an image, so let's remove it
                    imagesToMarkCorrupted.add(imgToCheck);
                }
            } catch (IOException e) {
                // The exception is expected (as the file is broken) so we shall ignore it
                imagesToMarkCorrupted.add(imgToCheck);
            }
        }

        // Add/remove image list appropriately
        allImages.removeAll(imagesToMarkCorrupted);
        corruptImages.addAll(imagesToMarkCorrupted);
    }

    /**
     * Find images that are likely corrupt
     */
    public void findLikelyCorruptImages() {
        List<File> imagesToMarkLikelyCorrupted = new ArrayList<>();
        for (File imgToCheck : allImages) {
            try {
                BufferedImage image = ImageIO.read(imgToCheck);
                if (image == null) {
                    // Couldn't decode an image, so let's remove it
                    imagesToMarkLikelyCorrupted.add(imgToCheck);
                }

                // Blur, create histogram, look for bad things
                float[] matrix = {
                        0.111f, 0.111f, 0.111f,
                        0.111f, 0.111f, 0.111f,
                        0.111f, 0.111f, 0.111f,
                };
                BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, matrix));
                BufferedImage blurredImage = op.filter(image, null);

                Raster raster = image.getRaster();
                final int h = image.getHeight();
                final int w = image.getWidth();
                double[] r = new double[h * w];
                r = raster.getSamples(0, 0, w, h, 0, r);
                r = raster.getSamples(0, 0, w, h, 1, r);
                r = raster.getSamples(0, 0, w, h, 2, r);

                // TODO haven't decided how to do this yet, I may come back later...

            } catch (IOException e) {
                // The exception is kinda expected so we shall ignore it
                imagesToMarkLikelyCorrupted.add(imgToCheck);
            }
        }

        // Add/remove image list appropriately
        allImages.removeAll(imagesToMarkLikelyCorrupted);
        halfCorruptImages.addAll(imagesToMarkLikelyCorrupted);
    }

    /**
     * Deletes bad images
     */
    public void deleteBadImages() {
        for (File i : corruptImages) {
            i.delete();
        }
        for (File i : halfCorruptImages) {
            i.delete();
        }
    }

    /**
     * Move bad images to another folder
     *
     * @param moveDir The folder to move bad images to
     */
    public void moveBadImages(String moveDir) throws ImageException, IOException {
        File moveFolder = new File(moveDir);
        if (!moveFolder.exists()) {
            moveFolder.mkdirs();
        }
        if (!moveFolder.isDirectory()) {
            throw new ImageException("Specified move directory is not a directory.");
        }

        moveFiles(corruptImages, moveFolder);
        moveFiles(halfCorruptImages, moveFolder);
    }

    /**
     * Moves a list of files to a new folder
     *
     * @param files      The list of files
     * @param moveFolder The destination folder
     * @throws IOException If a problem occured
     */
    private void moveFiles(List<File> files, File moveFolder) throws IOException {
        for (File i : files) {
            File moveDest = new File(moveFolder.getPath() + "/" + i.getName());
            Files.move(i.toPath(), moveDest.toPath());
        }
    }

    public File getFolder() {
        return folder;
    }

    public List<File> getAllImages() {
        return allImages;
    }

    public List<File> getCorruptImages() {
        return corruptImages;
    }

    public List<File> getHalfCorruptImages() {
        return halfCorruptImages;
    }

    @Override
    public String toString() {
        return "'" + folder.getAbsolutePath() + "', " + allImages.size() + " good, " + corruptImages + " corrupt, " + halfCorruptImages + " expected corrupt";
    }
}
