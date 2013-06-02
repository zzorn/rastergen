package org.rasterfun.rastergen;

import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.BaseConverter;
import org.flowutils.rectangle.ImmutableRectangle;
import org.flowutils.rectangle.Rectangle;
import com.beust.jcommander.IStringConverter;


/**
 * Converts coordinates from the command line to a rectangle.
 */
public class RectangleConverter extends BaseConverter<Rectangle> {

    private static final String DESC = "a rectangle.  A rectangle should have four coordinate values specified: " +
                                       "leftX,upperY,rightX,lowerY, separated by commas and no whitespace.  E.g.  -1,0,2.5,100";

    public RectangleConverter(String optionName) {
        super(optionName);
    }

    @Override public Rectangle convert(String value) {
        final String[] coordinates = value.trim().split("\\s*+,\\s*");
        if (coordinates == null || coordinates.length != 4) throw new ParameterException(getErrorString(value, DESC));

        try {
            double x1 = parseCoordinate(coordinates[0]);
            double y1 = parseCoordinate(coordinates[1]);
            double x2 = parseCoordinate(coordinates[2]);
            double y2 = parseCoordinate(coordinates[3]);

            return new ImmutableRectangle(x1, y1, x2, y2);
        }
        catch (Exception e) {
            throw new ParameterException(getErrorString(value, DESC));
        }
    }

    private double parseCoordinate(String coordinate) {
        return Double.parseDouble(coordinate);
    }
}
