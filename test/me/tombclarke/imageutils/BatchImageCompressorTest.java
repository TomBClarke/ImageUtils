package me.tombclarke.imageutils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BatchImageCompressorTest {

    private BatchImageCompressor compressor;
    private File testFolder = null;

    @Before
    public void init() throws IOException, ImageException {
        testFolder = ImageUtilsTest.setupTestDir(this);

        // Clear out all but 2 files
        for (File i : testFolder.listFiles()) {
            if (!(i.getName().contains("good (1)") || i.getName().contains("good (2)"))) {
                i.delete();
            }
        }

        compressor = new BatchImageCompressor(testFolder);
    }

    @After
    public void clean() {
        ImageUtilsTest.clean(testFolder);
    }

    @Test
    public void testCompressImagesFixedRatio() throws IOException, ImageException {
        int width = 800;
        int height = 480;
        compressor.compressImages(width, height, false);

        for (File i : testFolder.listFiles()) {
            BufferedImage compressedImage = ImageIO.read(i);
            assert (compressedImage.getWidth() == width);
            assert (compressedImage.getHeight() == height);
        }
    }

    @Test
    public void testCompressImagesRespectedRatio() throws IOException, ImageException {
        int width = 800;
        int height = 480;
        compressor.compressImages(width, height, true);

        for (File i : testFolder.listFiles()) {
            BufferedImage compressedImage = ImageIO.read(i);
            assert (compressedImage.getWidth() <= width);
            assert (compressedImage.getHeight() <= height);
        }
    }

}
