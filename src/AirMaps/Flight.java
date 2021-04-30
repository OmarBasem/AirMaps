package AirMaps;

import org.jgrapht.graph.*;

public class Flight extends DefaultWeightedEdge {
    private String code;
    private String departure;
    private String arrival;

    public Flight(String a, String b, String c) {
        code = a;
        departure = b;
        arrival = c;
    }

    public String getCode() {
        return this.code;
    }

    public String getDeparture() {
        return this.departure;
    }

    public String getArrival() {
        return this.arrival;
    }

}
