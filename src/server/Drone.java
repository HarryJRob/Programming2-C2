package server;

import common.Model;

public class Drone extends Model {

	Number speed;
	
	public Drone(Number speed) {
		this.speed = speed;
	}

	@Override
	public String getName() {
		return null;
	}
	
	public Number getSpeed() {
		return speed;
	}

}
