package common.Models;

import common.Model;

import java.util.ArrayList;
import java.util.List;

public class Supplier extends Model {

	private Number distance;
	private List<Ingredient> suppliedIngredients;
	
	public Supplier(String name, Number distance) {
		this.name = name;
		this.distance = distance;
		suppliedIngredients = new ArrayList<Ingredient>();
	}
	
	public Supplier(String name, float distance, List<Ingredient> suppliedIngredients) {
		this.name = name;
		this.distance = distance;
		this.suppliedIngredients = suppliedIngredients;
	}
	
	public void addSuppliedIngredient(Ingredient i) {
		suppliedIngredients.add(i);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public Number getDistance() {
		return distance;
	}
	
	public List<Ingredient> getSuppliedIngredients() {
		return suppliedIngredients;
	}

}
