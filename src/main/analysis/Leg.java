package main.analysis;


import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import java.util.ArrayList;
import java.util.List;


class Leg {

	private List<Id<Link>> route = new ArrayList<>();

	private double fromTime;
	private double toTime;

	private String mode;
	private boolean isTeleported;

	public List<Id<Link>> getRoute() {
		return route;
	}

	public void setRoute(List<Id<Link>> route) {
		this.route = route;
	}

	public double getFromTime() {
		return fromTime;
	}

	void setFromTime(double fromTime) {
		this.fromTime = fromTime;
	}

	public double getToTime() {
		return toTime;
	}

	void setToTime(double toTime) {
		this.toTime = toTime;
	}

	String getMode() {
		return mode;
	}

	void setMode(String mode) {
		this.mode = mode;
	}

	public boolean isTeleported() {
		return isTeleported;
	}

	void setTeleported(boolean teleported) {
		isTeleported = teleported;
	}

	void addLink(Id<Link> linkId) {
		route.add(linkId);
	}
}
