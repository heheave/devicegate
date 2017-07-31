package simulation.device;

/**
 * Created by xiaoke on 17-5-15.
 */
public class Location {

    private final double longitude;

    private final double dimension;

    public Location(double longitude, double dimension) {
        this.longitude = longitude;
        this.dimension = dimension;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getDimension() {
        return dimension;
    }
}
