package server.StockManager;

import java.io.Serializable;

import common.Models.Dish;

//Monitors the stock level of a dish
public final class MoniteredDish extends MoniteredItem implements Serializable {

	private static final long serialVersionUID = -4792863995292009380L;
	
	private Dish dish;
	
	public MoniteredDish(Dish dish, Number stockedAmount, Number restockThreshold, Number restockAmount) {
		super(stockedAmount, restockThreshold, restockAmount);
		this.dish = dish;
	}
	
	public Dish getDish() {
		return dish;
	}
}
