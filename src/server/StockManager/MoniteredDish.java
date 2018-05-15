package server.StockManager;

import common.Models.Dish;

public final class MoniteredDish extends MoniteredItem {

	private Dish dish;
	
	public MoniteredDish(Dish dish, Number stockedAmount, Number restockThreshold, Number restockAmount) {
		super(stockedAmount, restockThreshold, restockAmount);
		this.dish = dish;
	}
	
	public Dish getDish() {
		return dish;
	}
}
