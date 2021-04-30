package AirMaps;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class AirRoutesTest2 {
	
	Routes fi = new AirRoutes();
	FlightsParser fr;

	@Before 
	public void initialize() {
		try {
			fr = new FlightsParser(FlightsParser.MOREAIRLINECODES);
			fi.populate(fr.getAirlines(), fr.getAirports(), fr.getFlights());
		} catch (FileNotFoundException | AirRoutesException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testLeastCost() {
		try {
			Route i = fi.leastCost("DXB", "NRT");
			assertEquals(2, i.totalHop());
			assertEquals(483, i.totalCost());
			assertEquals(522, i.airTime());
			assertEquals(849, i.connectingTime());
			assertEquals(1371, i.totalTime());
		} catch (AirRoutesException e) {
			System.err.println(e);
		}
	}
	
	@Test
	public void testLeastHop() {
		try {
			Route i = fi.leastHop("DXB", "NRT");
			assertEquals(1, i.totalHop());
			assertEquals(518, i.totalCost());
			assertEquals(485, i.airTime());
			assertEquals(0, i.connectingTime());
			assertEquals(485, i.totalTime());
		} catch (AirRoutesException e) {
			System.err.println(e);
		}
	}
	
	@Test
	public void testleastCostExcluding() {
		try {
			Route iNotExcluding = fi.leastCost("EDI", "NRT");
			assertEquals(2, iNotExcluding.totalHop());
			assertTrue(iNotExcluding.getStops().contains("AMS"));
			
			List<String> excluding = new ArrayList<String>();
			excluding.add("AMS");
			excluding.add("LHR");
			Route iExcluding = fi.leastCost("EDI", "NRT" ,excluding);
			assertEquals(3, iExcluding.totalHop());
			assertFalse(iExcluding.getStops().contains("AMS"));
			assertFalse(iExcluding.getStops().contains("LHR"));
		} catch (AirRoutesException e) {
			System.err.println(e);
		}
	}
	
	@Test
	public void testLeastHopExcluding() {
		try {
			Route iNotExcluding = fi.leastHop("EDI", "NRT");
			assertEquals(2, iNotExcluding.totalHop());
			
			List<String> excluding = new ArrayList<String>();
			excluding.add("EWR");
			Route iExcluding = fi.leastHop("EDI", "NRT", excluding);
			assertEquals(2, iExcluding.totalHop());
			assertFalse(iExcluding.getStops().contains("EWR"));
		} catch (AirRoutesException e) {
			System.err.println(e);
		}
	}
	
	@Test
	public void testGetStops() {
		try {
			Route i = fi.leastCost("DXB", "MAD");
			
			List<String> expectedStops = new ArrayList<String>();
			expectedStops.add("DXB");
			expectedStops.add("IST");
			expectedStops.add("MAD");
			
			List<String> actualStops = i.getStops();
			
			assertTrue(actualStops.equals(expectedStops));
			
		} catch (AirRoutesException e) {
			System.err.println(e);
		}
	}
	
	@Test
	public void testGetFlights() {
		try {
			Route i = fi.leastHop("DXB", "MAD");
			
			List<String> expectedFlights = new ArrayList<String>();
			expectedFlights.add("EK5624");
			
			List<String> actualFlights = i.getFlights();
			
			assertTrue(actualFlights.equals(expectedFlights));
			
		} catch (AirRoutesException e) {
			System.err.println(e);
		}
	}
}
