package org.rasterfun.rastergen;

import com.beust.jcommander.JCommander;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Command line utility for generating images from raster descriptions.
 */
// TODO: Add easy to use facade class for the library
public class RasterGen {
    private static final int ERROR   = 0;
    private static final int INFO    = 1;
    private static final int VERBOSE = 2;

    private final GenerationParameters parameters;
    private final int verbosityLevel;
    private final String fileFormat;


    public static void main(String[] args) {
        GenerationParameters parameters = new GenerationParameters();
        final JCommander commander = new JCommander(parameters);
        commander.setProgramName("rastergen");
        try {
            commander.parse(args);

            // Parse size from comma separated pair
            parameters.parseSize();
        }
        catch (Throwable e) {
            System.out.println();
            System.out.println(e.getMessage());
            System.out.println();
            commander.usage();
            System.exit(1);
        }

        if (parameters.help) {
            commander.usage();
        }
        else {
            RasterGen rasterGen = new RasterGen(parameters);
            rasterGen.generateFiles();
        }
    }

    public RasterGen(GenerationParameters parameters) {
        this.parameters = parameters;
        verbosityLevel = determineVerbosity(parameters);

        // Determine file type
        if (parameters.format.equalsIgnoreCase("png")) {
            fileFormat = "png";
        }
        else if (parameters.format.equalsIgnoreCase("jpg")) {
            fileFormat = "jpg";
        }
        else {
            throw new IllegalArgumentException("The specified file format '"+parameters.format+"' is not supported");
        }
    }

    public void generateFiles() {
        if (parameters.rasterGenFiles.isEmpty()) {
            out(INFO, "No input files to process.");
        }
        else {
            for (File rasterGenFile : parameters.rasterGenFiles) {
                processFile(rasterGenFile, parameters);
            }
        }
    }

    private void processFile(File genFile, GenerationParameters parameters) {
        out(VERBOSE, "Processing " + genFile);

        if (!genFile.exists()) out(ERROR, "File not found: " + genFile);
        else if (!genFile.isFile()) out(ERROR, "Not a file: " + genFile);
        else if (!genFile.canRead()) out(ERROR, "Can not read the file: " + genFile);
        else {
            // Load and parse generator

            // Generate images with generator

            // TODO: Determine default channel mappings, use channel names specified in generator in the order they are specified?
            // Or use red, green, blue, alpha if found, fill with other channels in the order they are found?
            final HashMap<String, String> defaultMappings = new HashMap<String, String>();
            defaultMappings.put("red", "red");
            defaultMappings.put("green", "green");
            defaultMappings.put("blue", "blue");
            defaultMappings.put("alpha", "alpha");

            // TODO: How to map channels with wide value range?  Specify ranges with parameters:  red = height[-100..100] green = gradient[10+70]  (dynamic with 10 as 50% point and 10+70 as 75%? or one std dev?)
            // TODO: Option for hue or other gradient mapping by specifying hue or gradient as target channel? gradient = height[-1000..1000]
            // hue = height[0+500]  (gradient could be some semi-standard false color thing, hue would be normal hue cycle, with possibility
            // to specify sat and lum as well. There could be gradient1, gradient2, etc, or some other named gradients.
            // TODO: Add grey as alias for all channels


            // Determine channel mappings
            final Map<String, Map<String, String>> fileAndChannelMappings = parameters.calculateFileAndChannelMappings(defaultMappings);

            for (int num = 1; num <= parameters.number; num++) {

                // Loop through files to generate
                for (Map.Entry<String, Map<String, String>> entry : fileAndChannelMappings.entrySet()) {
                    final String fileSuffix = entry.getKey();
                    final Map<String, String> channelMapping = entry.getValue();

                    // Determine file name
                    File outputFile = determineFileName(genFile, fileSuffix, num, parameters.number > 1);

                    // Check if file already exists
                    if (outputFile.exists() && !parameters.overwrite) {
                        out(INFO, outputFile + " already exists, skipping  (use --overwrite to overwrite).");
                    }
                    else {
                        // Prepare random seed parameter
                        long seed = outputFile.getPath().hashCode() + 1337 * num + num;

                        // Generate image
                        out(VERBOSE, "Generating " + outputFile + " of size ("+parameters.width+", "+parameters.height+")");
                        // TODO

                        // Notify user
                        String updateType = "Saving ";
                        if (outputFile.exists()) updateType = "Overwriting ";
                        out(INFO, updateType + outputFile);
                        out(VERBOSE, ""); // Newline

                        // Save image in correct format
                        // TODO

                    }
                }
            }

        }
    }

    private File determineFileName(File genFile, String fileSuffix, int num, boolean includeNumber) {
        String fileName = genFile.getName();

        // Strip extension
        final int extensionIndex = fileName.lastIndexOf(".");
        if (extensionIndex > 0) {
            fileName = fileName.substring(0, extensionIndex);
        }

        // Get parent path
        String parent = genFile.getParent();
        if (parent == null) parent = ".";

        // Append suffix and separator
        final String fullFileName = parent + File.separator + fileName + (includeNumber ? ""+num : "") + fileSuffix + "." + fileFormat;

        return new File(fullFileName);
    }


    private void out(int verbosity, final String message) {
        if (verbosity >= verbosityLevel) System.out.println(message);
    }

    private int determineVerbosity(GenerationParameters parameters) {
        int verbosityLevel = INFO;
        if (parameters.quiet) verbosityLevel = 0;
        else if (parameters.verbose) verbosityLevel = 2;
        return verbosityLevel;
    }
}
