package common.Models;

import java.io.Serializable;

import common.Model;

public class Postcode extends Model implements Serializable {

	private static final long serialVersionUID = -2125008660985860360L;
	
	private Number distance;

	public Postcode(String postcode, Number distance) {
		this.name = postcode;
		this.distance = distance;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public Number getDistance() {
		return distance;
	}

}
