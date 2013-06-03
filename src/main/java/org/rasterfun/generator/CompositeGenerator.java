package org.rasterfun.generator;

import org.rasterfun.core.compiler.RendererBuilder;
import org.rasterfun.utils.ParameterChecker;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses other generators to generate pictures.
 * May add prefixes to the names of the pictures generated by each generator, or just have a growing index number for them.
 */
// TODO: Support for common sets of effects to run before / after the other generators for each picture?
public final class CompositeGenerator extends GeneratorBase {

    private List<Generator> generators = new ArrayList<Generator>();

    private final GeneratorListener generatorListener = new GeneratorListener() {
        @Override
        public void onGeneratorChanged(Generator generator) {
            notifyGeneratorChanged();
        }
    };

    @Override
    public List<RendererBuilder> createBuilders() {
        List<RendererBuilder> builders = new ArrayList<RendererBuilder>();

        for (Generator generator : generators) {
            builders.addAll(generator.createBuilders());
        }

        return builders;
    }

    public void addGenerator(Generator generator) {
        ParameterChecker.checkNotNull(generator, "generator");
        ParameterChecker.checkNotAlreadyContained(generator, generators, "generators");

        generators.add(generator);
        generator.addListener(generatorListener);

        notifyGeneratorChanged();
    }

    public void removeGenerator(Generator generator) {
        ParameterChecker.checkNotNull(generator, "generator");
        ParameterChecker.checkContained(generator, generators, "generators");

        generator.removeListener(generatorListener);
        generators.remove(generator);

        notifyGeneratorChanged();
    }

    @Override
    public Generator copy() {
        final CompositeGenerator theCopy = new CompositeGenerator();

        for (Generator generator : generators) {
            theCopy.generators.add(generator.copy());
        }

        return theCopy;
    }
}