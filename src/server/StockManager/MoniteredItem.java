package server.StockManager;

import java.io.Serializable;

//Provides functionality to monitor the stock level of a given item
public abstract class MoniteredItem implements Serializable {
	
	private static final long serialVersionUID = -6132110613878030107L;
	
	protected Number stockedAmount;
	protected Number resupplyAmount;
	protected Number resupplyThreshold;
	protected boolean beingRestocked = false;
	
	protected MoniteredItem(Number stockedAmount, Number resupplyAmount, Number resupplyThreshold) {
		this.stockedAmount = stockedAmount;
		this.resupplyAmount = resupplyAmount;
		this.resupplyThreshold = resupplyThreshold;
	}
	
	public synchronized void setStock(Number stock) {
		this.stockedAmount = stock;
	}
	
	public synchronized void setThreshold(Number threshold) {
		this.resupplyThreshold = threshold;
	}
	
	public synchronized void setResupplyAmt(Number amt) {
		this.resupplyAmount = amt;
	}
	
	public synchronized void setBeingRestocked(boolean b) {
		this.beingRestocked = b;
	}
	
	public synchronized Number getStockedAmount() {
		return stockedAmount;
	}
	
	public synchronized Number getResupplyAmount() {
		return resupplyAmount;
	}
	
	public synchronized Number getResupplyThreshold() {
		return resupplyThreshold;
	}
	
	public synchronized boolean isBeingRestocked() {
		return beingRestocked;
	}
	
}