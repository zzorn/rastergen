package org.rasterfun.rastergen;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import com.beust.jcommander.validators.PositiveInteger;
import org.flowutils.rectangle.ImmutableRectangle;
import org.flowutils.rectangle.Rectangle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parsed command line arguments.
 */
public class GenerationParameters {

    private static final String FILE_SUFFIX = "@";
    private static final String DEFAULT_FILE = "";
    @Parameter(names = {"-h", "--help"}, help = true, description = "Prints usage instructions.")
    public boolean help = false;

    @Parameter(names = {"-q", "--quiet"}, description = "Do not print any progress information.")
    public boolean quiet = false;

    @Parameter(names = {"-v", "--verbose"}, description = "Print detailed information of what the program is doing.")
    public boolean verbose = false;

    @Parameter(names = {"-o", "--overwrite"}, description = "Overwrite existing files with the same filename.  If not specified, will skip over any already existing files.")
    public boolean overwrite = false;

    @Parameter(names = {"-f", "--format"}, description = "Image format for output images.  Supported formats are png and jpg.")
    public String format = "png";

    @Parameter(names = {"-s", "--size"}, description = "Specifies the width and height of the generated images, separated by a comma.  " +
                                                       "If only one value is given, it is used for both the width and height.")
    public String size = "256,256";

    @Parameter(names = {"-n", "--number"}, description = "Number of times to generate each image. " +
                                                         "Each subsequent image is generated with a new random seed, " +
                                                         "and has the number appended to the file name.", validateWith = PositiveInteger.class)
    public int number = 1;

    @Parameter(names = {"-a", "--area"}, converter = RectangleConverter.class,
               description =
                       "Coordinates for the source area used when generating the image. " +
                       "Should be four floating point values, for leftX,upperY," +
                       "rightX,lowerY, separated by commas and no spaces.")
    public Rectangle area = new ImmutableRectangle(0, 0, 1, 1);

    @DynamicParameter(names = "-C", description = "Used to specify mappings from generated channels to output image channels. " +
                                                  "E.g. to render the height channel to the red channel in the output image, use -Cred=Height. " +
                                                  "You can use several output images, in that case append a suffix for the output image to the channel, " +
                                                  "separated by @, e.g. -Cred@-first = Height -Cred@-second = Slope  " +
                                                  "applied to a generator file named \"foo\" would produce three images, \"foo.png\" with default mappings, " +
                                                  "\"foo-first.png\" with the Height channel as red, " +
                                                  "and \"foo-second.png\", with the Slope channel as red.  To omit \"foo.png\" with the default mappings, specify --omit-default-mappings.")
    public Map<String, String> channelMappings = new HashMap<String, String>();

    @Parameter(names = {"-m", "--omit-default-mappings"}, description = "Do not generate the image file with the default channel mappings (only generate images generated by special channel mappings using -C, if any)")
    public boolean omitDefaultMappings = false;

    @DynamicParameter(names = "-P", description = "Generator parameters. " +
                                                  "Any custom parameters to pass to the generator.  E.g. -P SpotSize=4.5 -P EyeColor=#2020D0")
    public Map<String, String> generatorParameters = new HashMap<String, String>();

    @Parameter(required = true, converter = FileConverter.class,
               description = "Raster generator files to render to images.")
    public List<File> rasterGenFiles =  new ArrayList<File>();

    public int width = 256;
    public int height = 256;


    public Map<String, Map<String, String>> calculateFileAndChannelMappings(Map<String, String> defaultMappings) {
        final HashMap<String, Map<String, String>> fileAndChannelMappings = new HashMap<String, Map<String, String>>();

        // Add default mappings initially, if we are not omitting the default file
        if (!omitDefaultMappings) {
            fileAndChannelMappings.put(DEFAULT_FILE, new HashMap<String, String>(defaultMappings));
        }

        // Add specified custom channel mappings
        for (Map.Entry<String, String> entry : channelMappings.entrySet()) {
            final String key = entry.getKey().trim();

            String sourceChannel = entry.getValue().trim();
            String destChannel = key;
            String fileSuffix = DEFAULT_FILE;

            // Check if a file suffix was specified (a separate file for this channel)
            final int index = key.indexOf(FILE_SUFFIX);
            if (index >= 0) {
                fileSuffix = key.substring(index + 1).trim();
                destChannel = key.substring(0, index).trim();
            }

            // Default destination channel to be the same as the source channel if it is not specified
            if (destChannel.length() == 0) destChannel = sourceChannel;

            // Only add the mapping if it is for a file with a custom suffix, or if we did not omit the default file.
            if (fileSuffix.length() > 0 || !omitDefaultMappings) {

                // Get channel mappings for the file with the suffix (or original if no suffix).
                Map<String, String> channelMappings = fileAndChannelMappings.get(fileSuffix);
                if (channelMappings == null) {
                    channelMappings = new HashMap<String, String>();
                    fileAndChannelMappings.put(fileSuffix, channelMappings);
                }

                // Add mapping
                channelMappings.put(destChannel, sourceChannel);
            }
        }

        return fileAndChannelMappings;
    }

    public void parseSize() {
        final String[] coords = size.split(",");
        if (coords.length < 1 || coords.length > 2) throw new IllegalArgumentException("Could not parse size parameter '"+size+"'");

        width = parseInt(coords[0], false, "width");
        height = parseInt(coords[coords.length == 1 ? 0 : 1], false, "height");
    }

    private int parseInt(String s, boolean allowZeroOrNegative, String desc) {
        try {
            final int i = Integer.parseInt(s);
            if (!allowZeroOrNegative && i <= 0) throw new IllegalArgumentException("Zero or negative values not allowed for " + desc + ", got '"+s+"'");
            return i;
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Could not parse value '"+s+"' for " + desc);
        }
    }

}
