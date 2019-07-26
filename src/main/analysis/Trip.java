package main.analysis;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;

import java.util.ArrayList;
import java.util.List;

class Trip {

	private Id<Link> departureLink;
	private Id<Link> arrivalLink;

	private double departureTime;
	private double arrivalTime;

	private String mainMode = TransportMode.other;

	private List<Leg> legs = new ArrayList<>();

	Trip(double departureTime, Id<Link> departureLink) {
		this.departureLink = departureLink;
		this.departureTime = departureTime;
	}

	public Id<Link> getDepartureLink() {
		return departureLink;
	}

	public void setDepartureLink(Id<Link> departureLink) {
		this.departureLink = departureLink;
	}

	public Id<Link> getArrivalLink() {
		return arrivalLink;
	}

	public void setArrivalLink(Id<Link> arrivalLink) {
		this.arrivalLink = arrivalLink;
	}

	public double getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(double departureTime) {
		this.departureTime = departureTime;
	}

	public double getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(double arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public String getMainMode() {
		return mainMode;
	}

	public void setMainMode(String mainMode) {
		this.mainMode = mainMode;
	}

	public List<Leg> getLegs() {
		return legs;
	}

	public void setLegs(List<Leg> legs) {
		this.legs = legs;
	}

	void addLeg(Leg leg) {

		this.mainMode = MainMode.getHigherRankinMode(mainMode, leg.getMode());
		this.legs.add(leg);
	}
}
