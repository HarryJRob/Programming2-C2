package common.Models;

import common.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//Models a supplier
public class Supplier extends Model implements Serializable {

	private static final long serialVersionUID = -2388140445781607416L;
	
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
