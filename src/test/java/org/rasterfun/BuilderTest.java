package org.rasterfun;

import org.junit.Before;
import org.junit.Test;
import org.rasterfun.core.PictureCalculations;
import org.rasterfun.core.compiler.CompilationException;
import org.rasterfun.core.compiler.RendererBuilder;
import org.rasterfun.core.listeners.PictureCalculationsListener;
import org.rasterfun.core.listeners.PictureCalculationsListenerAdapter;
import org.rasterfun.picture.Picture;
import org.rasterfun.utils.RasterExecutor;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests core aspect of the pixel calculator, and the PictureCalculation.
 */
public class BuilderTest {

    public static final int TEST_CALCULATION_INDEX = 123;
    private RendererBuilder rendererBuilder;
    private RasterExecutor rasterExecutor;

    @Before
    public void setUp() throws Exception {
        rendererBuilder = new RendererBuilder("TestPic", 100, 100, channelsList("roses", "violets"), channelsList(), 0, 1);
        rasterExecutor = new RasterExecutor();
    }

    @Test
    public void testPictureCalculation() throws CompilationException {
        // Create builder with some output
        rendererBuilder = new RendererBuilder("TestPic", 100, 100, channelsList("xs", "ys"), channelsList(), 0, 1);
        rendererBuilder.addChannelAssignment("xs", "x");
        rendererBuilder.addChannelAssignment("ys", "y");

        // Create calculation task and start it
        final PictureCalculations calculation = new PictureCalculations(rasterExecutor.getExecutor(), rendererBuilder);
        calculation.start(TEST_CALCULATION_INDEX);

        // Get result picture
        final List<Picture> results = calculation.getPicturesAndWait();
        assertEquals("We should get one picture", 1, results.size());
        Picture result = results.get(0);

        // Check generated picture
        assertEquals("Number of channels should be correct", 2, result.getChannelCount());
        assertPixelCorrect(result, "xs",  0,  0,  0);
        assertPixelCorrect(result, "xs",  1,  0,  1);
        assertPixelCorrect(result, "ys",  1,  0,  0);
        assertPixelCorrect(result, "xs",  1,  1,  1);
        assertPixelCorrect(result, "xs",  4,  9,  4);
        assertPixelCorrect(result, "ys",  4,  9,  9);
        assertPixelCorrect(result, "xs", 99, 99, 99);
        assertPixelCorrect(result, "ys", 99, 99, 99);

        // Check generated preview
        assertEquals("We should get one preview", 1, calculation.getPreviews().size());
        final Picture preview = calculation.getPreviews().get(0);
        assertEquals("Number of channels should be correct in preview", 2, preview.getChannelCount());
        assertPixelCorrect(preview, "xs",  0,  0,  0);
        assertPixelCorrect(preview, "xs",  1,  0,  1);
        assertPixelCorrect(preview, "ys",  1,  0,  0);
        assertPixelCorrect(preview, "xs",  1,  1,  1);
    }

    @Test
    public void testCalculationIndex() throws Exception {
        final PictureCalculations calculation = new PictureCalculations(rasterExecutor.getExecutor(), rendererBuilder);

        final int[] calcIndexes = {-1, -1, -1, -1, -1};
        calculation.addListener(new PictureCalculationsListener() {
            @Override
            public void onProgress(int calculationIndex, float progress) {
                calcIndexes[0] = calculationIndex;
            }

            @Override
            public void onPreviewReady(int calculationIndex, int pictureIndex, Picture preview) {
                calcIndexes[1] = calculationIndex;
            }

            @Override
            public void onPictureReady(int calculationIndex, int pictureIndex, Picture picture) {
                calcIndexes[2] = calculationIndex;
            }

            @Override
            public void onError(int calculationIndex, String shortDescription, String longDescription, Throwable cause) {
                calcIndexes[3] = calculationIndex;
            }

            @Override
            public void onReady(int calculationIndex, List<Picture> pictures) {
                calcIndexes[4] = calculationIndex;
            }
        });

        calculation.start(TEST_CALCULATION_INDEX);

        // Wait to finish
        calculation.getPicturesAndWait();

        delay(10);

        assertEquals("In progress reports, the correct calculation index should have been used", TEST_CALCULATION_INDEX, calcIndexes[0]);
        assertEquals("In preview reports, the correct calculation index should have been used", TEST_CALCULATION_INDEX, calcIndexes[1]);
        assertEquals("In picture reports, the correct calculation index should have been used", TEST_CALCULATION_INDEX, calcIndexes[2]);
        assertEquals("In ready reports, the correct calculation index should have been used", TEST_CALCULATION_INDEX, calcIndexes[4]);
    }

    @Test
    public void testErrorCalculation() throws CompilationException {

        // Lets make a division by zero halfway through
        rendererBuilder.addPixelCalculationLine("int w = 1 / (50 - y); // Oops!\n");

        final PictureCalculations calculation = new PictureCalculations(rasterExecutor.getExecutor(), rendererBuilder);

        final boolean[] errorReported = {false};
        final float[] progressMade = {0};
        final boolean[] readyCalled = {false};
        calculation.addListener(new PictureCalculationsListenerAdapter() {
            @Override
            public void onProgress(int calculationIndex, float progress) {
                //System.out.println("progress = " + progress);
                progressMade[0] = progress;
            }

            @Override
            public void onError(int calculationIndex, String shortDescription, String longDescription, Throwable cause) {
                //System.out.println("Error description = " + description);
                errorReported[0] = true;
            }

            @Override
            public void onReady(int calculationIndex, List<Picture> pictures) {
                readyCalled[0] = true;
            }
        });

        calculation.start();

        // Wait for boom
        calculation.getPicturesAndWait();

        delay(10);

        assertTrue("An error should have been reported", errorReported[0]);
        assertTrue("Some progress should have been made, but not complete, but progress was " + progressMade[0], progressMade[0] > 0.05 && progressMade[0] < 0.95);
        assertFalse("onReady should not have been called", readyCalled[0]);
    }

    @Test
    public void testStop() throws CompilationException {
        // Create builder with sleep
        rendererBuilder.addPixelCalculationLine("        try {\n" +
                                                  "            Thread.sleep(10);\n" +
                                                  "        } \n" +
                                                  "        catch (InterruptedException e) {\n" +
                                                  "            \n" +
                                                  "        }\n");


        // Create calculation task and start it
        final PictureCalculations calculation = new PictureCalculations(rasterExecutor.getExecutor(), rendererBuilder);
        calculation.start();

        // Should be running
        assertFalse("The calculation should not be done yet", calculation.isDone());

        // Wait a bit for the calculation to start
        delay(50);

        // Stop calculation
        calculation.stop();

        // Wait a bit for the calculation to stop
        delay(50);

        // Should be stopped now
        assertTrue("The calculation should have stopped now", calculation.isDone());
    }

    private void delay(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            // Ignore
        }
    }


    private void assertPixelCorrect(Picture picture, String channel, int x, int y, float expected) {
        assertEquals("We should get the correct pixel value at channel " + channel + ", location (x: "+x+", y: "+y+")",
                     expected, picture.getPixel(channel, x, y), 0.0001);
    }

    private List<String> channelsList(String ... names) {
        return Arrays.asList(names);
    }


}
