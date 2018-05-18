package server.StockManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Models.Dish;
import common.Models.Ingredient;
import common.Models.Order;
import server.Drone;
import server.Staff;
import server.ServerInterface.UnableToDeleteException;

//Manages the stock level of all dishes and ingredients.
//Also manages how the drones and staff act given a situation
public class StockManager implements Runnable {

	private List<MoniteredDish> moniteredDishes;
	private List<MoniteredIngredient> moniteredIngredients;
	private List<Drone> drones;
	private List<Staff> staffList;
	private List<Order> orders;
	
	//Not in spec implied by interface methods :D
	private boolean restockIngredients = true;
	private boolean restockDishes = true;
	
	public StockManager() {
		moniteredDishes = new ArrayList<MoniteredDish>();
		moniteredIngredients = new ArrayList<MoniteredIngredient>();
		drones = new ArrayList<Drone>();
		staffList = new ArrayList<Staff>();
		orders = new ArrayList<Order>();
	}

	//Check stock levels every 3 seconds
	@Override
	public void run() {
		while(true) {
			long startTime = System.currentTimeMillis();
			
			//Restock dishes check
			if(restockDishes) {
				synchronized(moniteredDishes) {
					for(MoniteredDish d : moniteredDishes) {
						if( d.getStockedAmount().intValue() < d.getResupplyThreshold().intValue() && !d.isBeingRestocked() && canBeMade(d.getDish())) {
							makeDish(d.getDish());
							synchronized(staffList) {
								for(Staff s : staffList) {
									if(s.getStatus().equals("Idle")) {
										s.giveTask(d);
									}
								}
							}
						}
					}
				}	
			}
			
			//Restock Ingredients check
			if(restockIngredients) {
				synchronized(moniteredIngredients) {
					for(MoniteredIngredient i : moniteredIngredients) {
						if(i.getStockedAmount().floatValue() < i.getResupplyThreshold().floatValue() && !i.isBeingRestocked()) {
							synchronized(drones) {
								for(Drone d : drones) {
									if(d.getStatus().equals("Idle")) {
										d.giveTask(i);
									}
								}
							}
						}
					}
				}
			}
			
			//Order check
			synchronized(orders) {
				for(Order o : orders) {
					if(o.getStatus().equals("Ordered")) {
						synchronized(drones) {
							for(Drone d : drones) {
								if(d.getStatus().equals("Idle")) {
									d.giveTask(o);
								}
							}
						}
					}
				}
			}
			
			long sleepTime = (3000 - (System.currentTimeMillis() - startTime));
			if(sleepTime > 0)
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) { }
		}
	}
	
	//Check if a dish has the needed ingredients to be made
	private synchronized boolean canBeMade(Dish dish) {
		Map<Ingredient, Number> ingredients = dish.getIngredientAmounts();
		
		for(Ingredient i : ingredients.keySet()) {
			MoniteredIngredient mi = findIngredient(i);
			if(mi == null) {
				return false;
			} else {
				if(mi.getStockedAmount().floatValue() < ingredients.get(i).floatValue()) {
					return false;
				}
			}
		}
		return true;
	}
	
	//Remove the relevant ingredients user to make the dish
	private synchronized void makeDish(Dish dish) {
		Map<Ingredient, Number> ingredients = dish.getIngredientAmounts();
		
		for(Ingredient i : ingredients.keySet()) {
			MoniteredIngredient mi = findIngredient(i);
			
			mi.setStock(mi.getStockedAmount().floatValue() - ingredients.get(i).floatValue());
		}
	}
	
	private synchronized MoniteredDish findDish(Dish dish) {
		for(MoniteredDish md : moniteredDishes)
			if(md.getDish() == dish) 
				return md;
		return null;
	}
	
	private synchronized MoniteredIngredient findIngredient(Ingredient ingredient) {
		for(MoniteredIngredient mi : moniteredIngredients)
			if(mi.getIngredient() == ingredient) 
				return mi;
		return null;
	}
	
	
	//Lots of getters and setters
	
	public synchronized List<Drone> getDrones() {
		return drones;
	}
	
	public synchronized List<Staff> getStaff() {
		return staffList;
	}
	
	public synchronized List<Dish> getDishes() {
		List<Dish> dishes = new ArrayList<Dish>();
		for(MoniteredDish md : moniteredDishes) {
			dishes.add(md.getDish());
		}
		return dishes;
	}
	
	public synchronized Dish addDish(Dish dish, Number restockThreshold, Number restockAmount) {
		moniteredDishes.add(new MoniteredDish(dish, 0, restockThreshold, restockAmount));
		return dish;
	}
	
	public synchronized void removeDish(Dish dish) throws UnableToDeleteException {
		moniteredDishes.remove(findDish(dish));
	}
	
	public synchronized void setDishStock(Dish dish, Number stock) {
		MoniteredDish md = findDish(dish);
		if(md != null)
			md.setStock(stock);
	}
	
	public synchronized void setDishRecipe(Dish dish, Map<Ingredient, Number> recipe) {
		MoniteredDish md = findDish(dish);
		if(md != null)
			md.getDish().setRecipe(recipe);
	}
	
	public synchronized void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {
		MoniteredDish md = findDish(dish);
		if(md != null)
			md.getDish().addIngredient(ingredient, quantity);
	}
	
	public synchronized void removeIngredientFromDish(Dish dish, Ingredient ingredient) {
		MoniteredDish md = findDish(dish);
		if(md != null)
			md.getDish().removeIngredient(ingredient);
	}
	
	public synchronized void setDishRestockLevels(Dish dish, Number threshold, Number restockAmt) {
		MoniteredDish md = findDish(dish);
		if(md != null) { 
			md.setThreshold(threshold);
			md.setResupplyAmt(restockAmt);
		}
	}
	
	public synchronized Number getRestockThreshold(Dish dish) {
		MoniteredDish md = findDish(dish);
		if(md != null)
			return md.getResupplyThreshold();
		return null;
	}
	
	public synchronized Number getRestockAmount(Dish dish) {
		MoniteredDish md = findDish(dish);
		if(md != null)
			return md.getResupplyAmount();
		return null;
	}
	
	public synchronized Map<Ingredient, Number> getRecipe(Dish dish) {
		MoniteredDish md = findDish(dish);
		if(md != null)
			return md.getDish().getIngredientAmounts();
		return null;
	}
	
	public synchronized Map<Dish, Number> getDishStockLevels() {
		HashMap<Dish, Number> dishStocks = new HashMap<Dish, Number>();
		for(MoniteredDish md : moniteredDishes) {
			dishStocks.put(md.getDish(), md.getStockedAmount());
		}
		return dishStocks;
	}
	
	public synchronized boolean setDishStock(String dishName, Number restockAmt) {
		for(MoniteredDish d : moniteredDishes) {
			if(d.getDish().getName().equals(dishName)) {
				d.setStock(restockAmt);
				return true;
			}
		}
		return false;
	}
	
	public synchronized Dish dishExists(String dishName) {
		for(MoniteredDish d : moniteredDishes) {
			if(d.getDish().getName().equals(dishName)) {
				return d.getDish();
			}
		}
		return null;
	}
	
	public synchronized void willRestockDishes(boolean enabled) {
		this.restockDishes = enabled;
	}

	
	public synchronized void clearDishes() {
		moniteredDishes.clear();
	}
	
	
	
	public synchronized List<Ingredient> getIngredients() {
		List<Ingredient> ingredients = new ArrayList<Ingredient>();
		for(MoniteredIngredient mi : moniteredIngredients) {
			ingredients.add(mi.getIngredient());
		}
		return ingredients;
	}
	
	public synchronized void setIngredientStock(Ingredient ingredient, Number stock) {
		MoniteredIngredient mi = findIngredient(ingredient);
		if(mi != null)
			mi.setStock(stock);
	}
	
	public synchronized Ingredient addIngredient(Ingredient ingredient, Number restockThreshold, Number restockAmount) {
		moniteredIngredients.add(new MoniteredIngredient(ingredient, 0, restockThreshold, restockAmount));
		return ingredient;
	}
	
	public synchronized void removeIngredient(Ingredient ingredient) throws UnableToDeleteException {
		moniteredIngredients.remove(findIngredient(ingredient));
	}
	
	public synchronized void setIngredientRestockLevels(Ingredient ingredient, Number threshold, Number restockAmt) {
		MoniteredIngredient mi = findIngredient(ingredient);
		if(mi != null) { 
			mi.setThreshold(threshold);
			mi.setResupplyAmt(restockAmt);
		}
	}
	
	public synchronized Number getRestockThreshold(Ingredient ingredient) {
		MoniteredIngredient mi = findIngredient(ingredient);
		if(mi != null)
			return mi.getResupplyThreshold();
		return null;
	}
	
	public synchronized Number getRestockAmount(Ingredient ingredient) {
		MoniteredIngredient mi = findIngredient(ingredient);
		if(mi != null)
			return mi.getResupplyAmount();
		return null;
	}
	
	public synchronized Map<Ingredient, Number> getIngredientStockLevels() {
		HashMap<Ingredient, Number> ingredientStocks = new HashMap<Ingredient, Number>();
		for(MoniteredIngredient mi : moniteredIngredients) {
			ingredientStocks.put(mi.getIngredient(), mi.getStockedAmount());
		}
		return ingredientStocks;
	}
	
	public synchronized boolean setIngredientStock(String ingredientName, Number restockAmt) {
		for(MoniteredIngredient d : moniteredIngredients) {
			if(d.getIngredient().getName().equals(ingredientName)) {
				d.setStock(restockAmt);
				return true;
			}
		}
		return false;
	}
	
	public synchronized Ingredient ingredientExists(String ingredientName) {
		for(MoniteredIngredient d : moniteredIngredients) {
			if(d.getIngredient().getName().equals(ingredientName)) {
				return d.getIngredient();
			}
		}
		return null;
	}
	
	public synchronized void willRestockIngredients(boolean enabled) {
		this.restockIngredients = enabled;
	}
	
	public synchronized void clearIngredients() {
		moniteredIngredients.clear();
	}

	public synchronized void addOrder(Order o) {
		orders.add(o);
	}

	public synchronized void removeOrder(Order o) {
		orders.remove(o);
	}

	public synchronized ArrayList<Order> getOrders() {
		return (ArrayList<Order>) orders;
	}

	public synchronized List<MoniteredDish> getMoniteredDishes() {
		return (ArrayList<MoniteredDish>) moniteredDishes;
	}

	public synchronized List<MoniteredIngredient> getMoniteredIngredients() {
		return moniteredIngredients;
	}
	
	public synchronized void clearStaff() {
		staffList.clear();
	}
	
	public synchronized void clearDrones() {
		drones.clear();
	}
	
	public synchronized void clearOrders() {
		orders.clear();
	}
	
}
