package server.StockManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Models.Dish;
import common.Models.Ingredient;
import server.ServerInterface.UnableToDeleteException;

public class StockManager {

	private List<MoniteredDish> moniteredDishes;
	private List<MoniteredIngredient> moniteredIngredients;
	
	public StockManager() {
		moniteredDishes = new ArrayList<MoniteredDish>();
		moniteredIngredients = new ArrayList<MoniteredIngredient>();
	}

	private MoniteredDish findDish(Dish dish) {
		for(MoniteredDish md : moniteredDishes)
			if(md.getDish() == dish) 
				return md;
		return null;
	}
	
	private MoniteredIngredient findIngredient(Ingredient ingredient) {
		for(MoniteredIngredient mi : moniteredIngredients)
			if(mi.getIngredient() == ingredient) 
				return mi;
		return null;
	}
	
	public List<Dish> getDishes() {
		List<Dish> dishes = new ArrayList<Dish>();
		for(MoniteredDish md : moniteredDishes) {
			dishes.add(md.getDish());
		}
		return dishes;
	}
	
	public Dish addDish(Dish dish, Number restockThreshold, Number restockAmount) {
		moniteredDishes.add(new MoniteredDish(dish, 0, restockThreshold, restockAmount));
		return dish;
	}
	
	public void removeDish(Dish dish) throws UnableToDeleteException {
		moniteredDishes.remove(findDish(dish));
	}
	
	public void setDishStock(Dish dish, Number stock) {
		MoniteredDish md = findDish(dish);
		if(md != null)
			md.setStock(stock);
	}
	
	public void setDishRecipe(Dish dish, Map<Ingredient, Number> recipe) {
		MoniteredDish md = findDish(dish);
		if(md != null)
			md.getDish().setRecipe(recipe);
	}
	
	public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {
		MoniteredDish md = findDish(dish);
		if(md != null)
			md.getDish().addIngredient(ingredient, quantity);
	}
	
	public void removeIngredientFromDish(Dish dish, Ingredient ingredient) {
		MoniteredDish md = findDish(dish);
		if(md != null)
			md.getDish().removeIngredient(ingredient);
	}
	
	public void setDishRestockLevels(Dish dish, Number threshold, Number restockAmt) {
		MoniteredDish md = findDish(dish);
		if(md != null) { 
			md.setThreshold(threshold);
			md.setResupplyAmt(restockAmt);
		}
	}
	
	public Number getRestockThreshold(Dish dish) {
		MoniteredDish md = findDish(dish);
		if(md != null)
			return md.getResupplyThreshold();
		return null;
	}
	
	public Number getRestockAmount(Dish dish) {
		MoniteredDish md = findDish(dish);
		if(md != null)
			return md.getResupplyAmount();
		return null;
	}
	
	public Map<Ingredient, Number> getRecipe(Dish dish) {
		MoniteredDish md = findDish(dish);
		if(md != null)
			return md.getDish().getIngredientAmounts();
		return null;
	}
	
	public Map<Dish, Number> getDishStockLevels() {
		HashMap<Dish, Number> dishStocks = new HashMap<Dish, Number>();
		for(MoniteredDish md : moniteredDishes) {
			dishStocks.put(md.getDish(), md.getStockedAmount());
		}
		return dishStocks;
	}
	
	public void clearDishes() {
		moniteredDishes.clear();
	}
	
	
	
	public List<Ingredient> getIngredients() {
		List<Ingredient> ingredients = new ArrayList<Ingredient>();
		for(MoniteredIngredient mi : moniteredIngredients) {
			ingredients.add(mi.getIngredient());
		}
		return ingredients;
	}
	
	public void setIngredientStock(Ingredient ingredient, Number stock) {
		MoniteredIngredient mi = findIngredient(ingredient);
		if(mi != null)
			mi.setStock(stock);
	}
	
	public Ingredient addIngredient(Ingredient ingredient, Number restockThreshold, Number restockAmount) {
		moniteredIngredients.add(new MoniteredIngredient(ingredient, 0, restockThreshold, restockAmount));
		return ingredient;
	}
	
	public void removeIngredient(Ingredient ingredient) throws UnableToDeleteException {
		moniteredIngredients.remove(findIngredient(ingredient));
	}
	
	public void setIngredientRestockLevels(Ingredient ingredient, Number threshold, Number restockAmt) {
		MoniteredIngredient mi = findIngredient(ingredient);
		if(mi != null) { 
			mi.setThreshold(threshold);
			mi.setResupplyAmt(restockAmt);
		}
	}
	
	public Number getRestockThreshold(Ingredient ingredient) {
		MoniteredIngredient mi = findIngredient(ingredient);
		if(mi != null)
			return mi.getResupplyThreshold();
		return null;
	}
	
	public Number getRestockAmount(Ingredient ingredient) {
		MoniteredIngredient mi = findIngredient(ingredient);
		if(mi != null)
			return mi.getResupplyAmount();
		return null;
	}
	
	public Map<Ingredient, Number> getIngredientStockLevels() {
		HashMap<Ingredient, Number> ingredientStocks = new HashMap<Ingredient, Number>();
		for(MoniteredIngredient mi : moniteredIngredients) {
			ingredientStocks.put(mi.getIngredient(), mi.getStockedAmount());
		}
		return ingredientStocks;
	}
	
	public void clearIngredients() {
		moniteredIngredients.clear();
	}
	
	
}
