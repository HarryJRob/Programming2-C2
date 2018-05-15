package server.StockManager;

import common.Models.Ingredient;

public class MoniteredIngredient extends MoniteredItem {

	private Ingredient ingredient;
	
	public MoniteredIngredient(Ingredient ingredient, Number stockedAmount, Number resupplyAmount, Number resupplyThreshold) {
		super(stockedAmount, resupplyAmount, resupplyThreshold);
		this.ingredient = ingredient;
	}
	
	public Ingredient getIngredient() {
		return ingredient;
	}
}
