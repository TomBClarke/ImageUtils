package me.tombclarke.imageutils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Useful common image functions
 *
 * @author Tom Clarke
 */
public final class ImageUtils {

    private static final List<String> SUPPORTED_IMAGE_TYPES = Arrays.asList(".png", ".jpg", ".jpeg");

    private ImageUtils() {
        // Nothing to do here
    }

    /**
     * Finds all images in a given folder
     *
     * @param folder    The folder containing the target files
     * @param allImages A list of images to add found images too
     */
    public static void getListOfImages(File folder, List<File> allImages) {
        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            // Ensure we can read the file
            if (!folder.canRead()) {
                continue;
            }

            // If it's a folder, go into it and get the pictures
            if (f.isDirectory()) {
                getListOfImages(f, allImages);
            }

            // It's a file, see if it's an image we want
            String name = f.getName();
            int beginningOfPostfix = name.lastIndexOf(".");
            if (beginningOfPostfix == -1) {
                continue;
            }
            String postfix = name.substring(beginningOfPostfix).toLowerCase();
            if (SUPPORTED_IMAGE_TYPES.contains(postfix)) {
                allImages.add(f);
            }
        }
    }
}
