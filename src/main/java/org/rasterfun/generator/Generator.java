package org.rasterfun.generator;

import org.rasterfun.core.PictureCalculations;
import org.rasterfun.core.compiler.RendererBuilder;
import org.rasterfun.core.listeners.PictureCalculationsListener;
import org.rasterfun.picture.Picture;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Something that generates pictures based on provided parameters.
 */
// TODO: Return calculations unstarted, start manually
public interface Generator {

    /**
     * Starts calculating the pictures produced by this generator, using the default values for the parameters.
     *
     * @param executorService service for running the calculation threads.
     * @return an object representing the ongoing calculation, that can be queried for a preview picture and the final picture.
     *         Also has a method to wait for the calculation to complete and return the calculated pictures.
     */
    PictureCalculations generatePictures(ExecutorService executorService);

    /**
     * Starts calculating the pictures produced by this generator.
     *
     * @param executorService service for running the calculation threads.
     * @param listener a listener that is notified about the progress of the calculation.
     * @return an object representing the ongoing calculation, that can be queried for a preview picture and the final picture.
     *         Also has a method to wait for the calculation to complete and return the calculated pictures.
     */
    PictureCalculations generatePictures(ExecutorService executorService, PictureCalculationsListener listener);

    /**
     * Starts calculating the pictures produced by this generator.
     *
     * @param executorService service for running the calculation threads.
     * @param listener a listener that is notified about the progress of the calculation.
     * @param picturesToReuse a list of pictures to reuse when calculating the pictures.  Should be the same size and
     *                        channel number to be useful.  Normally you would pass in pictures that were returned by
     *                        an earlier call to generatePictures when you are regenerating a view.
     *                        If null, new pictures will be allocated instead.
     * @param previewsToReuse same as picturesToReuse, except for preview pictures.
     * @return an object representing the ongoing calculation, that can be queried for a preview picture and the final picture.
     *         Also has a method to wait for the calculation to complete and return the calculated pictures.
     */
    PictureCalculations generatePictures(ExecutorService executorService,
                                         PictureCalculationsListener listener,
                                         List<Picture> picturesToReuse,
                                         List<Picture> previewsToReuse);

    /**
     * Returns a calculator for pictures produced by this generator.
     * Does not start the calculator before returning it.
     *
     * @param executorService service for running the calculation threads.
     * @param picturesToReuse a list of pictures to reuse when calculating the pictures.  Should be the same size and
     *                        channel number to be useful.  Normally you would pass in pictures that were returned by
     *                        an earlier call to generatePictures when you are regenerating a view.
     *                        If null, new pictures will be allocated instead.
     * @param previewsToReuse same as picturesToReuse, except for preview pictures.
     * @return an object representing the ongoing calculation, that can be queried for a preview picture and the final picture.
     *         Also has a method to wait for the calculation to complete and return the calculated pictures.
     */
    // TODO: By default, do not start the calculator, and do not take listener parameter
    PictureCalculations generatePicturesWithoutStarting(ExecutorService executorService,
                                                        List<Picture> picturesToReuse,
                                                        List<Picture> previewsToReuse);

    /**
     * @param listener a listener that should be notified when the generator changes.
     */
    void addListener(GeneratorListener listener);

    /**
     * @param listener listener to remove.
     */
    void removeListener(GeneratorListener listener);


    /**
     * @return the RendererBuilder with the source to generate each picture that this generator produces.
     */
    List<RendererBuilder> createBuilders();

    /**
     * @return a deep copy of the Generator.
     */
    Generator copy();


}
