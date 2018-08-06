package me.tombclarke.imageutils;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the bad image remover class
 *
 * @author Tom Clarke
 */
public class BatchBatchBadImageRemoverTest {

    private BatchBadImageRemover imgRemover;
    private File testFolder = null;

    @Before
    public void init() throws IOException, ImageException {
        testFolder = ImageUtilsTest.setupTestDir(this);
        imgRemover = new BatchBadImageRemover(testFolder);
    }

    @After
    public void clean() {
        ImageUtilsTest.clean(testFolder);
    }

    @Test
    public void testAllImages() {
        assertEquals(9, imgRemover.getAllImages().size());
    }

    @Test
    public void testFindCorruptImages() {
        imgRemover.findCorruptImages();
        assert (imgRemover.getAllImages().size() < 7);
        assert (imgRemover.getCorruptImages().size() > 2);

        // Check they're the correct images
        List<String> imgNames = new ArrayList<>();
        for (File i : imgRemover.getCorruptImages()) {
            imgNames.add(i.getName());
        }

        assertTrue(imgNames.contains("bad (1).JPG"));
        assertTrue(imgNames.contains("bad (2).JPG"));
        assertTrue(imgNames.contains("bad (3).JPG"));
        assertTrue(!imgNames.contains("good (1).JPG"));
        assertTrue(!imgNames.contains("good (2).JPG"));
        assertTrue(!imgNames.contains("good (3).JPG"));
    }

    @Ignore
    @Test
    public void testFindLikelyCorruptImages() {
        imgRemover.findLikelyCorruptImages();
        assert (imgRemover.getAllImages().size() < 7);
        assert (imgRemover.getCorruptImages().size() > 2);

        // Check they're the correct images
        List<String> imgNames = new ArrayList<>();
        for (File i : imgRemover.getCorruptImages()) {
            imgNames.add(i.getName());
        }

        assertTrue(imgNames.contains("ugly (1).JPG"));
        assertTrue(imgNames.contains("ugly (2).JPG"));
        assertTrue(imgNames.contains("ugly (3).JPG"));
        assertTrue(!imgNames.contains("good (1).JPG"));
        assertTrue(!imgNames.contains("good (2).JPG"));
        assertTrue(!imgNames.contains("good (3).JPG"));
    }

    @Test
    public void testDeleteBadImages() {
        int goodImageCount = filterForGoodPictures();
        imgRemover.deleteBadImages();
        assertEquals(goodImageCount, testFolder.list().length);
    }

    @Test
    public void testMoveBadImages() throws IOException, ImageException {
        int originalImageCount = testFolder.list().length;
        int goodImageCount = filterForGoodPictures();
        String moveFolderName = "move";
        String testFolderPath = testFolder.getPath();
        File moveFolder = new File(testFolderPath.substring(0, testFolderPath.lastIndexOf(ImageUtilsTest.DIR_TEST)) + moveFolderName);
        moveFolder.mkdir();

        try {
            imgRemover.moveBadImages(moveFolder.getPath());

            assertEquals(goodImageCount, testFolder.list().length);
            assertEquals(originalImageCount - goodImageCount, moveFolder.list().length);
        } finally {
            // Cleanup
            for (File f : moveFolder.listFiles()) {
                f.delete();
            }
            moveFolder.delete();
        }
    }

    /**
     * Selects corrupt images (only obvious ones) and returns a count of the remaining good ones
     *
     * @return The number of good pictures in the test folder
     */
    private int filterForGoodPictures() {
        imgRemover.findCorruptImages();
        return imgRemover.getAllImages().size();
    }
}
