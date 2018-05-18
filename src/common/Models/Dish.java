package common.Models;

import common.Model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

//Models a dish
public class Dish extends Model implements Serializable {

	private static final long serialVersionUID = 1043495467678552016L;
	
	private String description;
	private Number price;
	private Map<Ingredient, Number> ingredientAmounts;
	
	public Dish(String name, String description, Number price) {
		this.name = name;
		this.description = description;
		this.price = price;
		ingredientAmounts = new HashMap<Ingredient, Number>();
	}
	
	public Dish(String name, String description, Number price, Map<Ingredient, Number> ingredientAmounts) {
		this.name = name;
		this.description = description;
		this.price = price;
		this.ingredientAmounts = ingredientAmounts;
	}
	
	
	public void addIngredient(Ingredient i, Number quantity) {
		ingredientAmounts.put(i, quantity);
	}
	
	public void removeIngredient(Ingredient ingredient) {
		ingredientAmounts.remove(ingredient);
	}
	
	public void setRecipe(Map<Ingredient, Number> recipe) {
		ingredientAmounts = recipe;
	}
	
	public Map<Ingredient, Number> getIngredientAmounts() {
		return ingredientAmounts;
	}
	
	
	@Override
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Number getPrice() {
		return price;
	}

	
}
