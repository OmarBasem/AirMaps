package AirMaps;

import java.util.HashSet;
import java.util.List;

public interface Routes {

	/**
	 * Populates the graph with the airlines, airports and flights information.
	 * Returns true if the operation was successful.
	 */
	boolean populate(HashSet<String[]> airlines, HashSet<String[]> airports, HashSet<String[]> routes);

	/**
	 * Returns a cheapest flight route from one airport (airport code) to another
	 */
	Route leastCost(String from, String to) throws AirRoutesException;

	/**
	 * Returns a least connections flight route from one airport (airport code) to
	 * another
	 */
	Route leastHop(String from, String to) throws AirRoutesException;

	/**
	 * Returns a cheapest flight route from one airport (airport code) to another,
	 * excluding a list of airport (airport codes)
	 */
	Route leastCost(String from, String to, List<String> excluding) throws AirRoutesException;

	/**
	 * Returns a least connections flight route from one airport (airport code) to
	 * another, excluding a list of airport (airport codes)
	 */
	Route leastHop(String from, String to, List<String> excluding) throws AirRoutesException;

	/**
	 * Returns the airport code of a best airport for the meet up of two people
	 * located in two different airports (airport codes) accordingly to the routes
	 * costs
	 */
	String leastCostMeetUp(String at1, String at2) throws AirRoutesException;

	/**
	 * Returns the airport code of a best airport for the meet up of two people
	 * located in two different airports (airport codes) accordingly to the number
	 * of connections
	 */
	String leastHopMeetUp(String at1, String at2) throws AirRoutesException;

	/**
	 * Returns the airport code of a best airport for the earliest meet up of two
	 * people located in two different airports (airport codes) when departing at a
	 * given time
	 */
	String leastTimeMeetUp(String at1, String at2, String startTime) throws AirRoutesException;

	/**
	 * Returns the flight routes, in increasing price cost, below or equal to a
	 * given price, from one airport (airport code) to another, excluding a list of
	 * airport (airport codes)
	 */
	List<Route> allRoutesCost(String from, String to, List<String> excluding, int maxCost) throws AirRoutesException;

	/**
	 * Returns the flights routes, in increasing number of hops, below or equal to a
	 * given number of hops, from one airport (airport code) to another, excluding a
	 * list of airport (airport codes)
	 */
	List<Route> allRoutesHop(String from, String to, List<String> excluding, int maxHop) throws AirRoutesException;

}
