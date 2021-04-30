package AirMaps;

import java.io.FileNotFoundException;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.shortestpath.KShortestSimplePaths;
import org.jgrapht.graph.*;


public class AirRoutes implements Routes {
    private DirectedWeightedPseudograph<String, Flight> graph = new DirectedWeightedPseudograph<>(Flight.class); // graph object
    private DirectedWeightedPseudograph<String, Flight> graphExclude = new DirectedWeightedPseudograph<>(Flight.class); // graph object for excluding airports
    private String ap1, ap2; // airport1 (departure) and airport2 (arrival)


    // setters and getters for the airports entered by the user
    private void setAp1(String ap1) {
        this.ap1 = ap1.toUpperCase();
    }
    private void setAp2(String ap2) { this.ap2 = ap2.toUpperCase(); }
    private String getAp1() {
        return this.ap1;
    }
    private String getAp2() {
        return this.ap2;
    }

    // returns the cheapest edge of parallel edges
    private Flight cheapestEdge(String departure, String arrival) {
        Set<Flight> allFlights = graph.getAllEdges(departure, arrival);
        if (allFlights.size() == 0) {
            allFlights = graphExclude.getAllEdges(departure, arrival);
        }
        Iterator<Flight> fIter = allFlights.iterator();
        Flight f = fIter.next();
        Flight f2;
        Double price = graph.getEdgeWeight(f);
        Double next_price;
        while (fIter.hasNext()) {
            f2 = fIter.next();
            next_price = graph.getEdgeWeight(f2);
            if (next_price < price) {
                price = next_price;
                f = f2;
            }
        }
        return f;
    }

    // returns the total price of a route
    private int totalPrice(List<String> airports) {
        Iterator<String> iter = airports.iterator();
        String v1 = iter.next();
        String v2;
        Flight f;
        int total_price = 0;
        while (iter.hasNext()) {
            v2 = iter.next();
            f = cheapestEdge(v1, v2);
            total_price += graph.getEdgeWeight(f);
            v1 = v2;
        }
        return total_price;
    }

    // returns the list of flight codes of a route
    private List<String> getFlightsList(List<String> vertices) {
        Iterator<String> iter = vertices.iterator();
        String v1 = iter.next();
        String v2;
        Flight f;
        List<String> flights_list = new ArrayList<>();
        while (iter.hasNext()) {
            v2 = iter.next();
            f = cheapestEdge(v1, v2);
            flights_list.add(f.getCode());
            v1 = v2;
        }
        return flights_list;
    }

    // returns the departure time of a route
    private String getDepartureTime(List<String> vertices) {
        Iterator<String> iter = vertices.iterator();
        Flight f = graph.getEdge(iter.next(), iter.next());
        return f.getDeparture();
    }

    // calculates the time difference between departure and arrival
    private int calcTime(String departureTime, String arrivalTime) {
        int startHour = Integer.parseInt(departureTime.substring(0, 2));
        int startMin = Integer.parseInt(departureTime.substring(2, 4));
        int endHour = Integer.parseInt(arrivalTime.substring(0, 2));
        int endMin = Integer.parseInt(arrivalTime.substring(2, 4));
        int minDiff = endMin - startMin;
        int hourDiff = endHour - startHour;
        if (hourDiff < 0 || (hourDiff<=0 && minDiff<=0)) {
            hourDiff += 24;
        }
        int time_diff = hourDiff * 60 + minDiff;
        return time_diff;
    }

    // returns the total time in the air of a route
    private int getAirTime(List<String> vertices) {
        Iterator<String> iter = vertices.iterator();
        String v1 = iter.next();
        String v2;
        Flight f;
        int air_time = 0;
        while (iter.hasNext()) {
            v2 = iter.next();
            f = cheapestEdge(v1, v2);
            air_time += calcTime(f.getDeparture(), f.getArrival());
            v1 = v2;
        }
        return air_time;
    }

    // returns the total waiting time in connections of a route
    private int getConnectingTime(List<String> vertices) {
        Iterator<String> iter = vertices.iterator();
        String v1 = iter.next();
        String v2 = iter.next();
        String v3;
        Flight f1 = cheapestEdge(v1, v2);
        Flight f2;
        int connecting_time = 0;
        while (iter.hasNext()) {
            v3 = iter.next();
            f2 = cheapestEdge(v2, v3);
            connecting_time += calcTime(f1.getArrival(), f2.getDeparture());
            f1 = f2;
            v2 = v3;
        }
        return connecting_time;
    }

    // excludes a list of airports from a list of paths
    private void excludeAirports(List<GraphPath<String, Flight>> allPaths, List<String> airports) {
        Iterator<GraphPath<String, Flight>> pathsIter = allPaths.iterator();
        List<Integer> exclude = new ArrayList<>();
        int index = 0;
        while (pathsIter.hasNext()) {
            List<String> vList = pathsIter.next().getVertexList();
            for (String airport : airports) {
                if (vList.contains(airport)) {
                    exclude.add(index);
                    index--; // decrement index after adding because exclude is an ArrayList not an Array
                    break;  // break if the path includes one of the airports
                }
            }
            index++;  // increment index when iterating to the next path
        }
        for (int i : exclude) {
            allPaths.remove(i);
        }
    }

    // returns the least time meet up route from one airport to another given a starting time.
    // used to find the route from the two starting airports to the meet up airport for the leastTimeMeetUp() method.
    private Route earliestRoute(String from, String to, String startTime) throws AirRoutesException {
        if (!graph.containsVertex(from) || !graph.containsVertex(to)) {
            throw new AirRoutesException("Airport does not exist!");
        }
        AllDirectedPaths<String, Flight> adp = new AllDirectedPaths<>(graph);
        int waitingTime, time1, time2;
        time2 = 10000000;
        Route earliest_route = null;

        List<GraphPath<String, Flight>> allPaths = adp.getAllPaths(from, to, false, 3);
        for (GraphPath<String, Flight> path : allPaths) {
            if (path.getVertexList().size() > 1){
                waitingTime = calcTime(startTime, getDepartureTime(path.getVertexList()));
                Route ir = createRoute(path.getVertexList());
                time1 = waitingTime + ir.totalTime();
                if (time1<time2){
                    time2 = time1;
                    earliest_route = ir;
                }

            }
        }
        return earliest_route;
    }

    // A method that creates an Route from a list of vertices.
    // This method prevents having to write the implementation of the Route interface several times
    // thus improving readability, style and clarity.
    private Route createRoute(List<String> stops) {
        Route ir = new Route() {
            @Override
            public List<String> getStops() {
                return stops;
            }
            @Override
            public int totalCost() {
                return totalPrice(getStops());
            }

            @Override
            public List<String> getFlights() {
                return getFlightsList(getStops());
            }

            @Override
            // subtract 1 because the smallest path has 2 airports, and thus 1 hop
            public int totalHop() {
                return getStops().size() - 1;
            }

            @Override
            public int airTime() {
                return getAirTime(getStops());
            }

            @Override
            public int connectingTime() {
                return getConnectingTime(getStops());
            }
            @Override
            public int totalTime() {
                return airTime() + connectingTime();
            }
        };
        return ir;
    }

    // prints the data of an Route
    private void displayData(Route route) {
        Iterator<String> iter = route.getStops().iterator();
        String formatter = "%-20s %-20s %-20s %-20s %-20s %-20s %-20s";
        String header = String.format(formatter, "Leg", "Leave", "At", "On", "Arrive", "At", "Price");
        System.out.println(header); // print the header row of the route
        String departure = iter.next();
        int i = 1;
        String flightNumber = "";
        String departureTime = "";
        String arrivalTime = "";
        while (iter.hasNext()) { // iterate over the vertices and print every flight of the route
            String arrival = iter.next();
            Flight f = cheapestEdge(departure, arrival);
            flightNumber = f.getCode();
            departureTime = f.getDeparture();
            arrivalTime = f.getArrival();
            System.out.println(String.format(formatter, i, departure, departureTime, flightNumber, arrival, arrivalTime, graph.getEdgeWeight(f)));
            departure = arrival;
            i++;
        }

        System.out.println("Total Journey Cost = " + route.totalCost());
        System.out.println("Total time in the air = " + route.airTime() + " minutes.");
        System.out.println("Connecting time = " + route.connectingTime() + " minutes.");
        System.out.println("Total time of the route = " + route.totalTime() + " minutes.");
    }

    // read the airports from the user
    private void readAirports(String s1, String s2) {
        Scanner s = new Scanner(System.in);
        System.out.println("Please enter the " + s1 +  " airport:");
        setAp1(s.nextLine());
        System.out.println("Please enter the " + s2 +  " airport:");
        setAp2(s.nextLine());
        System.out.println();
    }

    // populates the graph
    private void populateExcluding(HashSet<String[]> airports, HashSet<String[]> routes, List<String> exclude) {

        // add vertices to the graph
        for (String[] airport : airports) {
            if (!exclude.contains(airport[0])) {
                graphExclude.addVertex(airport[0]);
            }
        }

        // add edges to the graph
        for (String[] route : routes) {
            if (!exclude.contains(route[1]) && !exclude.contains(route[3])){
                Flight e = new Flight(route[0], route[2], route[4]); // create a flight object to be used as the edge(code, departure, arrival)
                graphExclude.addEdge(route[1], route[3], e);
                graphExclude.setEdgeWeight(e, Double.parseDouble(route[5])); // add price as edge weight
            }
        }
    }


    // populates the graph
    public boolean populate(HashSet<String[]> airlines, HashSet<String[]> airports, HashSet<String[]> routes) {

        // add vertices to the graph
        for (String[] airport : airports) {
            graph.addVertex(airport[0]);
        }

        // add edges to the graph
        for (String[] route : routes) {
            Flight e = new Flight(route[0], route[2], route[4]); // create a flight object to be used as the edge(code, departure, arrival)
            graph.addEdge(route[1], route[3], e);
            graph.setEdgeWeight(e, Double.parseDouble(route[5])); // add price as edge weight
        }

        return true;
    }

    /**
     * Returns a cheapest flight route from one airport (airport code) to another
     */
    public Route leastCost(String from, String to) throws AirRoutesException {
        if (!graph.containsVertex(from) || !graph.containsVertex(to)) { // if the airport does not exist in the graph throw an exception
            throw new AirRoutesException("Airport does not exist!");
        }
        DijkstraShortestPath<String, Flight> dijkstraAlg = new DijkstraShortestPath<>(graph);
        GraphPath<String, Flight> cheapestPaths = dijkstraAlg.getPath(from, to);
        if (cheapestPaths == null) { // throw an exception if no path exists between two airports.
            throw new AirRoutesException("No route exists between these two airports");
        }
        return createRoute(cheapestPaths.getVertexList()); // create a route from the list of vertices and return it.
    }

    	/**
	 * Returns a least connections flight route from one airport (airport code) to
	 * another
	 */
    public Route leastHop(String from, String to) throws AirRoutesException {
        if (!graph.containsVertex(from) || !graph.containsVertex(to)) {
            throw new AirRoutesException("Airport does not exist!");
        }
        AllDirectedPaths<String, Flight> adp = new AllDirectedPaths<>(graph);
        List<GraphPath<String, Flight>> allPaths;
        int j = 1;
        do {
            allPaths = adp.getAllPaths(from, to, false, j);
            j++;
        } while (allPaths.isEmpty() && j < 100); // keep looking for the least changeover route while allPaths is empty
                                                 // or the length of the path is less than 100 (just a dummy big number)
        Iterator<GraphPath<String, Flight>> iter = allPaths.iterator();
        if (iter.hasNext()) { // if there was a route found
            return createRoute(iter.next().getVertexList()); // return the first route from the list
        }
        return null;
    }

	/**
	 * Returns a cheapest flight route from one airport (airport code) to another,
	 * excluding a list of airport (airport codes)
	 */

    public Route leastCost(String from, String to, List<String> excluding) throws AirRoutesException {
        if (!graph.containsVertex(from) || !graph.containsVertex(to)) {
            throw new AirRoutesException("Airport does not exist!");
        }
        try {
            FlightsParser fr = new FlightsParser(FlightsParser.MOREAIRLINECODES);
            populateExcluding(fr.getAirports(), fr.getFlights(), excluding); // populate another graph excluding these airports
            DijkstraShortestPath<String, Flight> dijkstraAlg = new DijkstraShortestPath<>(graphExclude); // cheapest path from the new graph
            GraphPath<String, Flight> cheapestPaths = dijkstraAlg.getPath(from, to);
            if (cheapestPaths == null) { // throw an exception if no path exists between two airports.
                throw new AirRoutesException("No route exists between these two airports");
            }
            return createRoute(cheapestPaths.getVertexList()); // create a route from the list of vertices and return it.
        } catch (FileNotFoundException | AirRoutesException e) {
            System.out.println(e);
        }
        return null;
    }

	/**
	 * Returns a least connections flight route from one airport (airport code) to
	 * another, excluding a list of airport (airport codes)
	 */
    public Route leastHop(String from, String to, List<String> excluding) throws AirRoutesException {
        if (!graph.containsVertex(from) || !graph.containsVertex(to)) {
            throw new AirRoutesException("Airport does not exist!");
        }
        AllDirectedPaths<String, Flight> adp = new AllDirectedPaths<>(graph);
        List<GraphPath<String, Flight>> allPaths;
        int j = 1;
        do {
            allPaths = adp.getAllPaths(from, to, false, j);
            excludeAirports(allPaths, excluding); // remove routes that includes one of the airports to be excluded
            j++;
        } while (allPaths.isEmpty() && j<100);// keep looking for the least changeover route while allPaths is empty
                                              // or the length of the path is less than 100 (just a dummy big number)

        Iterator<GraphPath<String, Flight>> iter = allPaths.iterator();
        return createRoute(iter.next().getVertexList());
    }

	/**
	 * Returns the airport code of a best airport for the meet up of two people
	 * located in two different airports (airport codes) accordingly to the routes
	 * costs
	 */
    public String leastCostMeetUp(String at1, String at2) throws AirRoutesException {
        if (!graph.containsVertex(at1) || !graph.containsVertex(at2)) {
            throw new AirRoutesException("Airport does not exist!");
        }
        DijkstraShortestPath<String, Flight> dijkstraAlg = new DijkstraShortestPath<>(graph);
        HashMap<Double, String> prices = new HashMap<>(); // a map to store the cheapest route to every airport
        for (String vertex : graph.vertexSet()) { // loop through all the airports
            if (!vertex.equals(at1) && !vertex.equals(at2)) {
                GraphPath<String, Flight> cheapest1 = dijkstraAlg.getPath(at1, vertex); //cheapest route from the first airport
                GraphPath<String, Flight> cheapest2 = dijkstraAlg.getPath(at2, vertex); //cheapest route from the second airport
                if (cheapest1 != null && cheapest2 != null) {
                    Double price1 = cheapest1.getWeight(); // price of the cheapest route from the first airport
                    Double price2 = cheapest2.getWeight(); // price of the cheapest route from the second airport
                    prices.put(price1 + price2, vertex); // put the sum of the prices as the key and vertex as value
                }
            }
        }
        Double min = Collections.min(prices.keySet()); // get the smallest key (price)
        return prices.get(min); // return the value (airport) of the smallest key (price)
    }

	/**
	 * Returns the airport code of a best airport for the meet up of two people
	 * located in two different airports (airport codes) accordingly to the number
	 * of connections
	 */
    public String leastHopMeetUp(String at1, String at2) throws AirRoutesException {
        if (!graph.containsVertex(at1) || !graph.containsVertex(at2)) {
            throw new AirRoutesException("Airport does not exist!");
        }
        HashMap<Integer, String> hops = new HashMap<>(); // a map to store the least changeovers route to every airport
        for (String vertex : graph.vertexSet()) { // loop through all the airports
            if (!vertex.equals(at1) && !vertex.equals(at2)) {
                Route r1 = leastHop(at1, vertex); //least hops route from the first airport
                Route r2 = leastHop(at2, vertex); //least hops route from the second airport
                if (r1!=null && r2!=null) {
                    int h1 = r1.totalHop();
                    int h2 = r2.totalHop();
                    hops.put(h1 + h2, vertex); // put the sum of the hops of the two routes as the key and vertex as value
                }

            }
        }
        int min = Collections.min(hops.keySet()); // get the smallest key (hops)
        return hops.get(min); // return the value (airport) of the smallest key (hops)
    }

	/**
	 * Returns the airport code of a best airport for the earliest meet up of two
	 * people located in two different airports (airport codes) when departing at a
	 * given time
	 */
    public String leastTimeMeetUp(String at1, String at2, String startTime) throws AirRoutesException {
        if (!graph.containsVertex(at1) || !graph.containsVertex(at2)) {
            throw new AirRoutesException("Airport does not exist!");
        }
        HashMap<Integer, String> leastTime = new HashMap<>(); // a map to store the least changeovers route to every airport
        int t1, t2, waiting1, waiting2;
        for (String vertex : graph.vertexSet()) { // loop through all the airports
            if (!vertex.equals(at1) && !vertex.equals(at2)) {
                Route r1 = earliestRoute(at1, vertex, startTime); //earliest route from the first airport
                Route r2 = earliestRoute(at2, vertex, startTime); //earliest route from the second airport
                if (r1!=null && r2!=null) {
                    waiting1 = calcTime(startTime, getDepartureTime(r1.getStops())); // time between startTime and departure time
                    waiting2 = calcTime(startTime, getDepartureTime(r2.getStops())); // time between startTime and departure time
                    t1 = waiting1 + r1.totalTime(); // total time is waiting time plus the route's total time
                    t2 = waiting2 + r2.totalTime();
                    if (t1>t2) { // put latest (will arrive second) route in the map
                        leastTime.put(t1, vertex); // put the time as the key and vertex as value
                    } else {
                        leastTime.put(t2, vertex);
                    }
                }
            }
        }
        int min = Collections.min(leastTime.keySet()); // get the smallest key (time)
        return leastTime.get(min); // return the value (airport) of the smallest key (time)
    }

	/**
	 * Returns the flight routes, in increasing price cost, below or equal to a
	 * given price, from one airport (airport code) to another, excluding a list of
	 * airport (airport codes)
	 */

    public List<Route> allRoutesCost(String from, String to, List<String> excluding, int maxCost) throws AirRoutesException {
        if (!graph.containsVertex(from) || !graph.containsVertex(to)) {
            throw new AirRoutesException("Airport does not exist!");
        }
        KShortestSimplePaths<String, Flight> kPaths = new KShortestSimplePaths<>(graph);
        Boolean contains;
        int k = 1;
        List i = new ArrayList();
        List<List> path_list = new ArrayList<>();
        do {
            contains = false;
            List<GraphPath<String, Flight>> kp = kPaths.getPaths(from, to, k);
            Iterator<GraphPath<String, Flight>> iter = kp.listIterator(k - 1); // iterator for the last route in the K cheapest routes
            while (iter.hasNext()) {
                i = iter.next().getVertexList();
                for (String exclude : excluding) {
                    if (i.contains(exclude)) { // Exclude the airports
                        contains = true;
                    }
                }
                if (!contains && totalPrice(i) <= maxCost) { // if the route not have excluded airports and price less than maxCost
                    path_list.add(i);
                }
                contains = false; // set contains back to false
            }
            k++; // increment K to get the next cheapest route
        } while (totalPrice(i) <= maxCost); // keep searching for routes while the route's price is less than the max cost
        List<Route> routes = new ArrayList<>();
        for (List path : path_list) { // creata a list of routes from the list of paths
            routes.add(createRoute(path));
        }
        return routes;
    }

	/**
	 * Returns the flights routes, in increasing number of hops, below or equal to a
	 * given number of hops, from one airport (airport code) to another, excluding a
	 * list of airport (airport codes)
	 */
    public List<Route> allRoutesHop(String from, String to, List<String> excluding, int maxHop) throws AirRoutesException {
        if (!graph.containsVertex(from) || !graph.containsVertex(to)) {
            throw new AirRoutesException("Airport does not exist!");
        }
        AllDirectedPaths<String, Flight> adp = new AllDirectedPaths<>(graph);
        List<GraphPath<String, Flight>> allPaths;
        allPaths = adp.getAllPaths(from, to, false, maxHop);
        excludeAirports(allPaths, excluding);

        // order the routes in increasing number of hops
        List<List> paths = new ArrayList<>();
        int i = 2;
        while (i <= maxHop + 1) { // search for routes while i less than the maxHop+1 (plus one because one edge path is two airports which is one hop)
            for (GraphPath<String, Flight> path : allPaths) {
                if (path.getVertexList().size() == i) {
                    paths.add(path.getVertexList());
                }
            }
            i++;
        }
        List<Route> routes = new ArrayList<>();
        for (List path : paths) { // create a list of routes from the list of paths
            routes.add(createRoute(path));
        }
        return routes;
    }

    /* MAIN METHOD */
    public static void main(String[] args) {
        AirRoutes sr = new AirRoutes(); // create SkyRoutes object
        try {
            FlightsParser fr = new FlightsParser(FlightsParser.MOREAIRLINECODES);
//            FlightsReader fr = new FlightsReader(FlightsReader.AIRLINECODES);
            sr.populate(fr.getAirlines(), fr.getAirports(), fr.getFlights()); // populate the graph

            Scanner s = new Scanner(System.in);
            String command; // option the user chooses
            Route route;
            String ap1, ap2;
            do {
                System.out.println(); // list of options the user can choose from
                System.out.println("Enter one of the following numbers to find the route you need:");
                System.out.println("(1) Cheapest route from one airport to another.");
                System.out.println("(2) Fewest number of changeovers.");
                System.out.println("(3) Cheapest Route excluding one or more airports.");
                System.out.println("(4) Fewest number of changeovers, excluding one or more airports.");
                System.out.println("(5) List of cheapest routes below a price excluding one or more airports");
                System.out.println("(6) List of least changeovers routes excluding one or more airports.");
                System.out.println("(7) Cheapest meet up airport given two airports.");
                System.out.println("(8) Least hop meet up airport given two airports.");
                System.out.println("(9) Least time meet up airport between two airports given a starting time.");
                System.out.println("(0) End.");
                command = s.nextLine();
                System.out.println();

                switch (command) { // using a switch to find the appropriate route
                    case "1":
                        sr.readAirports("departure", "arrival");
                        ap1 = sr.getAp1();
                        ap2 = sr.getAp2();
                        System.out.println("Route for " + ap1 + " to " + ap2);
                        route = sr.leastCost(ap1, ap2);
                        List<String> stops = route.getStops();
                        if (stops != null) { // print the path if it exists
                            sr.displayData(route);
                            System.out.println("Press any key to continue.");
                            command = s.nextLine();
                        } else {
                            System.out.println("No route exists between these two airports.");
                        }
                        break;

                    case "2":
                        sr.readAirports("departure", "arrival");
                        ap1 = sr.getAp1();
                        ap2 = sr.getAp2();
                        System.out.println("A route with fewest number of changeovers from " + ap1 + " to " + ap2 + " :");
                        System.out.println();
                        route = sr.leastHop(ap1, ap2);
                        List<String> hops = route.getStops();
                        if (hops != null) { // print the path if it exists
                            sr.displayData(route);
                            System.out.println();
                            System.out.println("Press any key to continue.");
                            command = s.nextLine();
                        } else {
                            System.out.println("No route exists between these two airports.");
                        }
                        break;

                    case "3":
                        sr.readAirports("departure", "arrival");
                        ap1 = sr.getAp1();
                        ap2 = sr.getAp2();
                        System.out.println("Please, enter 1 or more airports you which to exclude from the route separated by a space then press ENTER");
                        String exclude = s.nextLine();
                        // split the airports to be excluded on spaces and put in them in a list
                        List<String> l = new ArrayList<>(Arrays.asList(exclude.split(" ")));
                        sr.populate(fr.getAirlines(), fr.getAirports(), fr.getFlights());
                        route = sr.leastCost(ap1, ap2, l);
                        System.out.println("Cheapest route from " + ap1 + " to " + ap2 + " excluding ( " + exclude + " ): ");
                        sr.displayData(route);
                        System.out.println("Press any key to continue.");
                        command = s.nextLine();
                        break;

                    case "4":
                        sr.readAirports("departure", "arrival");
                        ap1 = sr.getAp1();
                        ap2 = sr.getAp2();
                        System.out.println("Please, enter 1 or more airports you wish to exclude from the route separated by a space then press ENTER");
                        String excluded = s.nextLine();
                        // split the airports to be excluded on spaces and put in them in a list
                        List<String> excluding = new ArrayList<>(Arrays.asList(excluded.split(" ")));
                        System.out.println("A Route with fewest number of changeovers from " + ap1 + " to " + ap2 + " excluding ( " + excluded + " )");
                        route = sr.leastHop(ap1, ap2, excluding);
                        sr.displayData(route);
                        System.out.println();
                        System.out.println("Press any key to continue.");
                        command = s.nextLine();
                        break;

                    case "5":
                        sr.readAirports("departure", "arrival");
                        ap1 = sr.getAp1();
                        ap2 = sr.getAp2();
                        System.out.println("Please, enter 1 or more airports you which to exclude from the route separated by a space then press ENTER");
                        String excluded_airports = s.nextLine();
                        // split the airports to be excluded on spaces and put in them in a list
                        List<String> lExcluded = new ArrayList<>(Arrays.asList(excluded_airports.split(" ")));
                        System.out.println("Enter the maximum price.");
                        String max_price = s.nextLine();
                        System.out.println("This will take a few moments. Please wait...");
                        List<Route> ir_list = sr.allRoutesCost(ap1, ap2, lExcluded, Integer.parseInt(max_price));
                        System.out.println("Cheapest routes from " + ap1 + " to " + ap2 + " excluding ( " + excluded_airports + " ) and maximum price of " + max_price + " :");
                        for (Route ir : ir_list) { // iterate over the routes and print them all
                            sr.displayData(ir);
                            System.out.println();
                        }
                        System.out.println("Press any key to continue.");
                        command = s.nextLine();
                        break;

                    case "6":
                        sr.readAirports("departure", "arrival");
                        ap1 = sr.getAp1();
                        ap2 = sr.getAp2();
                        System.out.println("Please, enter 1 or more airports you which to exclude from the route separated by a space then press ENTER");
                        String excluded_airs = s.nextLine();
                        List<String> lEx = new ArrayList<>(Arrays.asList(excluded_airs.split(" ")));
                        System.out.println("Enter the maximum hop.");
                        String max_hop = s.nextLine();
                        List<Route> routes_list = sr.allRoutesHop(ap1, ap2, lEx, Integer.parseInt(max_hop));
                        System.out.println("Least hops routes from " + ap1 + " to " + ap2 + " excluding ( " + excluded_airs + " ) and maximum hops of " + max_hop + " :");
                        for (Route r : routes_list) { // iterate over the routes and print them all
                            sr.displayData(r);
                            System.out.println();
                        }
                        System.out.println("Press any key to continue.");
                        command = s.nextLine();
                        break;

                    case "7":
                        sr.readAirports("first", "second");
                        ap1 = sr.getAp1();
                        ap2 = sr.getAp2();
                        String meetUp = sr.leastCostMeetUp(ap1, ap2);
                        System.out.println("Cheapest meet up airport for " + ap1 + " and " + ap2 + " is " + meetUp);

                        // print the cheapest route from the first airport to the meetup airport
                        System.out.println();
                        System.out.println("Route for " + ap1 + " to " + meetUp);
                        route = sr.leastCost(ap1, meetUp);
                        sr.displayData(route);

                        // print the cheapest route from the second airport to the meetup airport
                        System.out.println();
                        System.out.println("Route for " + ap2 + " to " + meetUp);
                        route = sr.leastCost(ap2, meetUp);
                        sr.displayData(route);
                        System.out.println("Press any key to continue.");
                        command = s.nextLine();
                        break;

                    case "8":
                        sr.readAirports("first", "second");
                        ap1 = sr.getAp1();
                        ap2 = sr.getAp2();
                        String meetUp_airport = sr.leastHopMeetUp(ap1, ap2);
                        System.out.println("Least hop meet up airport for " + ap1 + " and " + ap2 + " is " + meetUp_airport);

                        // print the least hops route from the first airport to the meetup airport
                        System.out.println();
                        System.out.println("Route for " + ap1 + " to " + meetUp_airport);
                        route = sr.leastHop(ap1, meetUp_airport);
                        sr.displayData(route);

                        // print the least hops route from the second airport to the meetup airport
                        System.out.println();
                        System.out.println("Route for " + ap2 + " to " + meetUp_airport);
                        route = sr.leastHop(ap2, meetUp_airport);
                        sr.displayData(route);
                        System.out.println("Press any key to continue.");
                        command = s.nextLine();
                        break;

                    case "9":
                        sr.readAirports("first", "second");
                        ap1 = sr.getAp1();
                        ap2 = sr.getAp2();
                        System.out.println("Enter the starting time: ");
                        String starting_time = s.nextLine();
                        String meetUp_air = sr.leastTimeMeetUp(ap1, ap2, starting_time);
                        System.out.println("Least time meet up airport for " + ap1 + " and " + ap2 + " for the time of " + starting_time +  " is " + meetUp_air);

                        // print the earlist route from the first airport to the meetup airport
                        route = sr.earliestRoute(ap1, meetUp_air, starting_time);
                        System.out.println();
                        System.out.println("Earliest route from " + ap1+ " to " + meetUp_air + " starting at time " + starting_time + " is:");
                        sr.displayData(route);

                        // print the earlist route from the second airport to the meetup airport
                        route = sr.earliestRoute(ap2, meetUp_air, starting_time);
                        System.out.println();
                        System.out.println("Earliest route from " + ap2 + " to " + meetUp_air + " starting at time " + starting_time + " is:");
                        sr.displayData(route);
                        System.out.println("Press any key to continue.");
                        command = s.nextLine();
                        break;
                }
            } while (!command.equals("0"));
        } catch (FileNotFoundException | AirRoutesException e) {
            System.out.println(e);
        }
    }
}