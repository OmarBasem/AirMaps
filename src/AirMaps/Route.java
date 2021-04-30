package AirMaps;

import java.util.List;

public interface Route {

	/** Returns the list of airports codes composing the route */
	List<String> getStops();
	
	/** Returns the list of flight codes composing the route */
	List<String> getFlights();
	
	/** Returns the number of connections of the route */
	int totalHop();
	
	/** Returns the total cost of the route */
	int totalCost();
	
	/** Returns the total time in flight of the route */
	int airTime();
	
	/** Returns the total time in connection of the route */
	int connectingTime();
	
	/** Returns the total travel time of the route */
	int totalTime();
}
