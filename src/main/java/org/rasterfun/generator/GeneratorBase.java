package org.rasterfun.generator;

import org.rasterfun.core.PictureCalculations;
import org.rasterfun.core.compiler.RendererBuilder;
import org.rasterfun.core.listeners.PictureCalculationsListener;
import org.rasterfun.picture.Picture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.rasterfun.utils.ParameterChecker.checkNotNull;

/**
 * Common functionality for PictureGenerators.
 */
public abstract class GeneratorBase implements Generator {

    private List<GeneratorListener> listeners = null;

    protected GeneratorBase() {
    }

    @Override
    public final PictureCalculations generatePictures(ExecutorService executorService) {
        return generatePictures(executorService, null);
    }

    @Override
    public final PictureCalculations generatePictures(ExecutorService executorService, PictureCalculationsListener listener) {
        return generatePictures(executorService, listener, null, null);
    }

    @Override
    public final PictureCalculations generatePictures(ExecutorService executorService,
                                                      PictureCalculationsListener listener,
                                                      List<Picture> picturesToReuse,
                                                      List<Picture> previewsToReuse) {
        // Create calculation
        final PictureCalculations calculation = generatePicturesWithoutStarting(executorService, picturesToReuse, previewsToReuse);
        if (listener != null) calculation.addListener(listener);

        // Start it
        calculation.start();

        // Return reference to ongoing calculation
        return calculation;
    }

    @Override
    public final PictureCalculations generatePicturesWithoutStarting(ExecutorService executorService, List<Picture> picturesToReuse, List<Picture> previewsToReuse) {
        // Compose the source
        final List<RendererBuilder> builders = createBuilders();

        // Create calculation task
        return new PictureCalculations(executorService, builders, picturesToReuse, previewsToReuse);
    }

    @Override
    public final void addListener(GeneratorListener listener) {
        checkNotNull(listener, "listener");
        if (listeners == null) {
            listeners = new ArrayList<GeneratorListener>();
        }
        listeners.add(listener);
    }

    @Override
    public final void removeListener(GeneratorListener listener) {
        checkNotNull(listener, "listener");
        if (listeners != null) listeners.remove(listener);
    }

    /**
     * @return default name for generators of this type.
     * The actual name is stored in the Parameters.
     */
    protected String getDefaultName() {
        return getClass().getSimpleName();
    }


    /**
     * Tells listeners of this PictureGenerator that it has changed, and any previews need to be redrawn, etc.
     */
    protected final void notifyGeneratorChanged() {
        if (listeners != null) {
            for (GeneratorListener listener : listeners) {
                listener.onGeneratorChanged(this);
            }
        }
    }

}
