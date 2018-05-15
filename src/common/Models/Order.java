package common.Models;

import java.io.Serializable;
import java.util.Map;

import common.Model;

public class Order extends Model implements Serializable {

	private static final long serialVersionUID = -1688260084018912759L;

	private User user;
	private Number distance;
	private Map<Dish, Number> orderedDishes;
	private boolean isFinished;
	
	public Order(User user, Number distance, Map<Dish, Number> orderedDishes) {
		this.user = user;
		this.distance = distance;
		this.orderedDishes = orderedDishes;
		this.isFinished = false;
	}
	
	public User getUser() {
		return user;
	}
	
	public Number getDistance() {
		return distance;
	}
	
	public Map<Dish, Number> getDishes() {
		return orderedDishes;
	}
	
	public Number getCost() {
		Number total = 0;
		for(Number n : orderedDishes.values()) {
			total = (float) total + (float) n;
		}
		return total;
	}
	
	public boolean isFinished() {
		return isFinished;
	}
	
	@Override
	public String getName() {
		return null;
	}

}