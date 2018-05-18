package server;

import java.io.Serializable;

import common.Model;
import server.StockManager.MoniteredDish;

public class Staff extends Model implements Runnable, Serializable {
	
	private static final long serialVersionUID = 8864709785877137565L;
	
	private String status = "Idle";
	private MoniteredDish curDish;
	
	public Staff(String name) {
		this.name = name;
	}

	//A Thread which waits a random amount of time between 40 and 60s before restocking the dish
	@Override
	public void run() {
		try {
			Thread.sleep((long) ((20+Math.random()*40)*1000));
			synchronized(curDish) {
				curDish.setStock(curDish.getStockedAmount().intValue() + curDish.getResupplyAmount().intValue());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		status = "Idle";
		curDish.setBeingRestocked(false);
	}

	@Override
	public synchronized String getName() {
		return name;
	}

	public synchronized String getStatus() {
		return status;
	}
	
	public synchronized void giveTask(MoniteredDish d) {
		curDish = d;
		d.setBeingRestocked(true);
		status = "Busy";
		new Thread(this).start();
	}

}
