package me.tombclarke.imageutils;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests the ImageUtils class and
 *
 * @author Tom Clarke
 */
public class ImageUtilsTest {

    public static final String DIR_ORIGINAL = "original";
    public static final String DIR_TEST = "fresh";

    /**
     * Setup up a directory with fresh images for testing (should be followed up by calling 'cleanTestDir')
     *
     * @param clazz The calling class to allow for the retrieval of resources
     * @return The File object representing the folder to test in
     * @throws IOException
     */
    public static File setupTestDir(Object clazz) throws IOException {
        // Clear old pictures and make new folder to process
        File originalFolder = new File(clazz.getClass().getClassLoader().getResource("test_images/" + DIR_ORIGINAL).getFile());
        String originalFolderPath = originalFolder.getPath();
        File testFolder = new File(originalFolderPath.substring(0, originalFolderPath.lastIndexOf(DIR_ORIGINAL)) + DIR_TEST);
        testFolder.mkdir();
        for (File f : originalFolder.listFiles()) {
            File testF = new File(testFolder.getPath() + "/" + f.getName());
            Files.copy(f.toPath(), testF.toPath());
        }
        return testFolder;
    }

    /**
     * Cleans the test folder
     *
     * @param testFolder The test folder created for the given test
     */
    public static void clean(File testFolder) {
        if (testFolder.listFiles() != null) {
            for (File f : testFolder.listFiles()) {
                f.delete();
            }
        }
        testFolder.delete();
    }

    @Test
    public void testGetListOfImages() {
        File folder = new File(getClass().getClassLoader().getResource("test_images/" + DIR_ORIGINAL).getFile());
        List<File> images = new ArrayList<>();
        ImageUtils.getListOfImages(folder, images);
        assertEquals(9, images.size());
    }

}
