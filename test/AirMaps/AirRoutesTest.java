package AirMaps;

import java.util.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;

public class AirRoutesTest {

    AirRoutes sr = new AirRoutes();
    AirRoutes srMore = new AirRoutes();
    FlightsParser fr, frMore;

    @Before public void initialize() {
        try {
            fr = new FlightsParser(FlightsParser.AIRLINECODES);
            sr.populate(fr.getAirlines(), fr.getAirports(), fr.getFlights());
            frMore = new FlightsParser(FlightsParser.MOREAIRLINECODES);
            srMore.populate(frMore.getAirlines(), frMore.getAirports(), frMore.getFlights());
        } catch (FileNotFoundException | AirRoutesException e) {
            e.printStackTrace();
            fail();
        }
    }


    @Test
    public void leastCostTest() {
        try {
            Route ir1 = sr.leastCost("EDI", "DXB");
            Route ir2 = srMore.leastCost("EDI", "DXB");
            assertEquals(364,ir1.totalCost()); // cheapest route from EDI to DXB when AIRLINESCODES is used costs 364
            assertEquals(363,ir2.totalCost()); // cheapest route from EDI to DXB when MOREAIRLINECODES is used costs 363
        } catch (AirRoutesException e) {
            fail();
        }
    }

    @Test
    public void leastHopTest() {
        try {
            Route ir = srMore.leastHop("DXB", "LGA");
            assertEquals(2,ir.totalHop());
        } catch (AirRoutesException e) {
            fail();
        }
    }

    @Test
    public void leastCostExcludingTest() {
        try {
            List<String> exclude = new ArrayList<>(Arrays.asList("LHR", "FRA"));
            Route ir = srMore.leastCost("EDI", "DXB", exclude);
            assertEquals(369,ir.totalCost());
        } catch (AirRoutesException e) {
            fail();
        }
    }



    @Test
    public void leastHopExcludingTest() {
        try {
            List<String> exclude = new ArrayList<>(Arrays.asList("LHR", "LGW", "FRA", "IST", "CDG", "AMS"));
            Route ir = srMore.leastHop("DXB", "EDI", exclude);
            assertEquals(3,ir.totalHop());
        } catch (AirRoutesException e) {
            fail();
        }
    }

    @Test
    public void allRoutesCostTest() {
        try {
            List<String> exclude = new ArrayList<>(Arrays.asList("LGW", "NCL"));
            List<Route> ir_list = sr.allRoutesCost("DXB", "EDI", exclude, 405);
            assertEquals(3,ir_list.size());
        } catch (AirRoutesException e) {
            fail();
        }
    }

    @Test
    public void allRoutesHopTest() {
        try {
            List<String> exclude = new ArrayList<>(Arrays.asList("LHR", "EWR"));
            List<Route> ir_list = srMore.allRoutesHop("LGA", "BCN", exclude, 3);
            assertEquals(497, ir_list.size());
        } catch (AirRoutesException e) {
            fail();
        }
    }

    @Test
    public void leastCostMeetUpTest() {
        try {
            String meetUp1 = sr.leastCostMeetUp("DXB", "EDI");
            String meetUp2 = sr.leastCostMeetUp("LHR", "TXL");
            assertEquals("LHR", meetUp1);
            assertEquals("RTM", meetUp2);
        } catch (AirRoutesException e) {
            fail();
        }
    }

    @Test
    public void leastHopMeetUpTest() {
        try {
            String first_airport = "CDG"; // first person airport
            String second_airport = "CAI"; // second person airport
            String meetUp = sr.leastCostMeetUp(first_airport, second_airport);
            int hops1 = sr.leastHop(first_airport, meetUp).totalHop(); // total hops from first airport to meetUp airport
            int hops2 = sr.leastHop(second_airport, meetUp).totalHop(); // total hops from second airport to meetUp airport
            assertEquals(1, hops1);
            assertEquals(1, hops2);
        } catch (AirRoutesException e) {
            fail();
        }
    }

    @Test
    public void leastTimeMeetUpTest() {
        try {
            String meetUp1 = sr.leastTimeMeetUp("DXB", "EDI", "0900");
            String meetUp2 = sr.leastTimeMeetUp("DXB", "EDI", "2100");
            assertEquals("LHR", meetUp1);
            assertEquals("LCY", meetUp2);
        } catch (AirRoutesException e) {
            fail();
        }
    }
}
