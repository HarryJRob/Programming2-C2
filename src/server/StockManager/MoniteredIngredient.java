package server.StockManager;

import java.io.Serializable;

import common.Models.Ingredient;

//Moniters the stock level of a ingredient
public class MoniteredIngredient extends MoniteredItem implements Serializable {


	private static final long serialVersionUID = -5863971222756670156L;
	
	private Ingredient ingredient;
	
	public MoniteredIngredient(Ingredient ingredient, Number stockedAmount, Number resupplyAmount, Number resupplyThreshold) {
		super(stockedAmount, resupplyAmount, resupplyThreshold);
		this.ingredient = ingredient;
	}
	
	public Ingredient getIngredient() {
		return ingredient;
	}
}
