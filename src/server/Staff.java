package server;

import common.Model;

public class Staff extends Model implements Runnable {
	
	public Staff(String name) {
		this.name = name;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return name;
	}

}
