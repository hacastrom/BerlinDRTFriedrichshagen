package main.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.class2019.analysis.Trip;
import org.matsim.class2019.analysis.TripEventHandler;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;

public class ObtainModalSplit {
	
	public static void main() {
	
	String caseOfInterest = "";
	String nutzer = "";
	String rootPath = null;
	
	switch(nutzer) {
	
	case("Hugo"):
	    rootPath = "";
	case ("Hugor2"):
		rootPath = "";	
    default:
    System.out.println("Incorrect Path");
	} 
	String inputEvents = rootPath + "/Events-" + caseOfInterest;
	EventsManager manager = EventsUtils.createEventsManager();
	TripEventHandler handler = new TripEventHandler();
	manager.addHandler(handler);
	new MatsimEventsReader(manager).readFile(events.toString());

	Map<Id<Person>, List<Trip>> tripByPerson = handler.getTripToPerson();

	Map<String, Double> countByMode = new HashMap<>();
	int totalTrips = 0;

	for (List<Trip> trips : tripByPerson.values()) {
		for (Trip trip : trips) {
			/*
			The main mode of a trip is e.g. pt
			if the trip consists of legs 'access_walk' -> 'pt' -> 'pt' -> egress_walk the main mode is pt.
			Main modes are sorted as in the MiD (Mobilität in Deutschland)
			See class Trip.addLeg for more details
			 */
			String modeOfTrip = trip.getMainMode();
			countByMode.merge(modeOfTrip, 1., Double::sum);
			totalTrips++;
		}
	}

	System.out.println("The modal split is:");
	for (Map.Entry<String, Double> entry : countByMode.entrySet()) {
		System.out.println(entry.getKey() + ": " + entry.getValue() + " / " + 100 * entry.getValue() / totalTrips + "%");
	}
	
}
