package server;

import java.io.Serializable;

import common.Model;
import common.Models.Order;
import server.StockManager.MoniteredIngredient;

public class Drone extends Model implements Runnable, Serializable {

	private static final long serialVersionUID = -8168930988958718156L;
	
	private Number speed;
	private MoniteredIngredient curIngredient;
	private Order curOrder;
	private String status  = "Idle";
	
	
	public Drone(Number speed) {
		this.speed = speed;
	}

	//A Thread which waits a amount of time before restocking or delivering a order.
	@Override
	public void run() {
		
		if(status.equals("Restocking")) {
			synchronized(curIngredient) {
				try {
					Thread.sleep((long) ((2*curIngredient.getIngredient().getSupplier().getDistance().floatValue() / speed.floatValue()) * 1000));
				} catch (InterruptedException e) { }
				curIngredient.setStock(curIngredient.getStockedAmount().floatValue() + curIngredient.getResupplyAmount().floatValue());
				curIngredient.setBeingRestocked(false);
			}
		} else if (status.equals("Delivering")) {
			synchronized(curOrder) {
				curOrder.setStatus("Delivering");
				try {
					Thread.sleep((long) (curOrder.getDistance().floatValue() / speed.floatValue()) * 1000);
				} catch (InterruptedException e) { } 
				curOrder.setStatus("Complete");
			}
		}

		status = "Idle";
	}
	
	@Override
	public synchronized String getName() {
		return null;
	}
	
	public synchronized Number getSpeed() {
		return speed;
	}

	public synchronized String getStatus() {
		return status;
	}

	public synchronized void giveTask(MoniteredIngredient i) {
		status = "Restocking";
		curIngredient = i;
		i.setBeingRestocked(true);
		new Thread(this).start();
	}
	
	public synchronized void giveTask(Order o) {
		status = "Delivering";
		curOrder = o;
		new Thread(this).start();
	}

}
