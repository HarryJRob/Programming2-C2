package common.Models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import common.Model;

//Models a Order
public class Order extends Model implements Serializable {

	private static final long serialVersionUID = -1688260084018912759L;

	private User user;
	private Number distance;
	private HashMap<Dish, Number> orderedDishes;
	private String status = "Ordered";
	
	public Order(User user, Number distance, HashMap<Dish, Number> orderedDishes) {
		this.user = user;
		this.distance = distance;
		this.orderedDishes = orderedDishes;
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
			total = total.floatValue() + n.floatValue();
		}
		return total;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	@Override
	public String getName() {
		return null;
	}

}