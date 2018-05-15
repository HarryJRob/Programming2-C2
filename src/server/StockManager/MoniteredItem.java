package server.StockManager;

public abstract class MoniteredItem {
	
	protected Number stockedAmount;
	protected Number resupplyAmount;
	protected Number resupplyThreshold;
	
	protected MoniteredItem(Number stockedAmount, Number resupplyAmount, Number resupplyThreshold) {
		this.stockedAmount = stockedAmount;
		this.resupplyAmount = resupplyAmount;
		this.resupplyThreshold = resupplyThreshold;
	}
	
	public void setStock(Number stock) {
		this.stockedAmount = stock;
	}
	
	public void setThreshold(Number threshold) {
		this.resupplyThreshold = threshold;
	}
	
	public void setResupplyAmt(Number amt) {
		this.resupplyAmount = amt;
	}
	
	public Number getStockedAmount() {
		return stockedAmount;
	}
	
	public Number getResupplyAmount() {
		return resupplyAmount;
	}
	
	public Number getResupplyThreshold() {
		return resupplyThreshold;
	}
}