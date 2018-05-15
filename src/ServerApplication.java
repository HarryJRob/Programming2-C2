import server.*;
import server.StockManager.StockManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import common.Models.*;
import common.communcation.Comms;
import common.communcation.Message;
import common.UpdateListener;


public class ServerApplication implements ServerInterface {
	
	private StockManager stockManager;
	private List<User> users;
	private List<Postcode> postcodes;
	private List<Order> orders;
	private List<Supplier> suppliers;
	private List<Drone> drones;
	private List<Staff> staffList;
	
	public static void main(String[] args) {
		ServerApplication serverApplication = new ServerApplication();
		serverApplication.launchGUI(serverApplication.initialise());
	}

	public ServerInterface initialise() {
		new Thread(new ConnectionManager()).start();
		
		stockManager = new StockManager();
		users = new ArrayList<User>();
		postcodes = new ArrayList<Postcode>();
		orders = new ArrayList<Order>();
		suppliers = new ArrayList<Supplier>();
		drones = new ArrayList<Drone>();
		staffList = new ArrayList<Staff>();
		
		return this;
	}
	
	public void launchGUI(ServerInterface s) {
		new ServerWindow(s);
	}
	

	private class ConnectionManager implements Runnable {

		List<Socket> connectionList;
		
		public ConnectionManager() {
			connectionList = new ArrayList<Socket>();
			new Thread(new ConnectionMaker()).start();
		}
		
		//Establishes connections with the clients
		private class ConnectionMaker implements Runnable {

			@Override
			public void run() {
				try {
					ServerSocket serverSocket = new ServerSocket(Comms.PORT_NUM);
					Socket tempRef;
					while(true) {
						try {
							tempRef = serverSocket.accept();
							System.out.println(tempRef.toString());
							synchronized (connectionList) {
								connectionList.add(tempRef);
							}
							tempRef = null;
						} catch (IOException e) { }
						serverSocket.close();
					}
				} catch (IOException e) {
					System.out.println("Failed to establish Server Socket");
					e.printStackTrace();
				}
			}
			
		}

		//Processes the inputs from the clients
		@Override
		public void run() {
			long startTime;
			while(true) {
				startTime = System.currentTimeMillis();
				
				synchronized (connectionList) {
					for(Socket curSoc : connectionList) {
						
						try {
							if(curSoc.getInputStream().read() == -1) {
								curSoc.close();
								connectionList.remove(curSoc);
							}
							else {
								Message curMessage = null;
								do {
									curMessage = (Message) Comms.recieveMessage(curSoc);

									
									if(curMessage != null)
										parseMessage(curMessage, curSoc);
									
								} while(curMessage != null);
							}
						} catch (IOException e) { }
						
					}
				}
				try {
					long sleepTime = (500 - System.currentTimeMillis() - startTime);
					if(sleepTime > 0)
						Thread.sleep(sleepTime);
				} catch (InterruptedException e) { }
			}
		}
		
		private void parseMessage(Message m, Socket s) {
			System.out.println(m.getMessageType());
			
			switch(m.getMessageType()) {
				
				case "REGISTER_USER":
				{
					User u = (User) m.getMessageContents();
					synchronized (users) {
						users.add(u);
					}
					Comms.sendMessage(m, s);
					
					break;
				}
				case "LOGIN_USER":
				{
					User u = (User) m.getMessageContents();
					boolean userExists = false;
					synchronized(users) {
						if (users.contains(u)) {
							userExists = true;
						} 
					}
					
					if (userExists) {
						Comms.sendMessage(m, s);
					} else {
						Comms.sendMessage(null, s);
					}
					
					break;
				}
				case "GET_POSTCODES":
				{
					synchronized(postcodes) {
						Comms.sendMessage(new Message("RETURN", postcodes.size()), s);
						for(Postcode p : postcodes) {
							Comms.sendMessage(new Message("RETURN", p ), s);
						}
					}
					break;
				}
				case "GET_DISHES":
				{
					synchronized(stockManager) {
						List<Dish> dishes = stockManager.getDishes(); 
						Comms.sendMessage(new Message("RETURN", dishes.size()), s);
						for(Dish d : dishes) {
							Comms.sendMessage(new Message("RETURN", d), s);
						}
					}
					break;
				}
				case "NEW_ORDER":
				{
					Order o = (Order) m.getMessageContents();
					synchronized (orders) {
						orders.add(o);
					}
					Comms.sendMessage(m, s);
					break;
				}
				case "GET_ORDERS":
				{
					synchronized(orders) {
						Comms.sendMessage(new Message("RETURN", orders.size()), s);
						for(Order o : orders) {
							Comms.sendMessage(new Message("RETURN", o), s);
						}
					}
					break;
				}
				case "CANCEL_ORDER":
				{
					synchronized(orders) {
						orders.remove((Order) m.getMessageContents());
					}
					break;
				}
			
			}
			
		}
	}
	
	//This makes no sense but specification says so. ¯\_(ツ)_/¯
	private class Configuration {
		
		public void loadConfiguration(String filename) throws FileNotFoundException {
			
			try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
				String curLine;
				while((curLine = reader.readLine()) != null) {
					//Parse curLine
					String[] parts = curLine.split(":");
					
					switch(parts[0]) {
					
					case "SUPPLIER":
						suppliers.add(new Supplier(parts[1], Float.valueOf(parts[2])));
						break;
						
					case "INGREDIENT":
						//Whether a ingredient can be if a invalid supplier is given is unspecified...
						//In my implementation I will assume the supplier must be valid
						for(Supplier s : suppliers) {
							if(s.getName().equals(parts[3])) {
								
								break;
							}
						}
						break;
						
					case "DISH":
						//TODO: Implement stuff
						break;
						
					case "USER":
						//Whether a user can be if a invalid postcode is given is unspecified...
						//In my implementation I will assume a user can only be added given a valid postcode
						for(Postcode p : postcodes) {
							if(p.getName().equals(parts[4])) {
								users.add(new User(parts[1], parts[2], parts[3], p));
								break;
							}
						}
						break;
						
					case "POSTCODE":
						postcodes.add(new Postcode(parts[1], Float.valueOf(parts[2])));
						break;	
						
					case "STAFF":
						//TODO: Implement stuff
						break;
					case "DRONE":
						//TODO: Implement stuff
						break;
					case "ORDER":
						//TODO: Implement stuff
						break;
					case "STOCK":
						//TODO: Implement stuff
						break;
					}
				}
			} catch (IOException e) {
				// (☞ﾟヮﾟ)☞  GUI should handle this but it only handles FileNotFoundException ☜(ﾟヮﾟ☜)
				System.out.println("There was an error while processing your configuration file \nThe GUI should handle this but it doesn't :( ");
			}
		}
	}
	
	
	//					--- Interface Methods ---
	
	
	@Override
	public void loadConfiguration(String filename) throws FileNotFoundException {
		new Configuration().loadConfiguration(filename);
	}
	
	@Override
	public void setRestockingIngredientsEnabled(boolean enabled) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setRestockingDishesEnabled(boolean enabled) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setStock(Dish dish, Number stock) {
		stockManager.setDishStock(dish, stock);
	}

	@Override
	public void setStock(Ingredient ingredient, Number stock) {
		stockManager.setIngredientStock(ingredient, stock);
		
	}

	@Override
	public List<Dish> getDishes() {
		return stockManager.getDishes();
	}

	@Override
	public Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
		return stockManager.addDish(new Dish(name, description, price), restockThreshold, restockAmount);
	}

	@Override
	public void removeDish(Dish dish) throws UnableToDeleteException {
		stockManager.removeDish(dish);
	}

	@Override
	public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {
		stockManager.addIngredientToDish(dish, ingredient, quantity);
	}

	@Override
	public void removeIngredientFromDish(Dish dish, Ingredient ingredient) {
		stockManager.removeIngredientFromDish(dish, ingredient);
		
	}

	@Override
	public void setRecipe(Dish dish, Map<Ingredient, Number> recipe) {
		stockManager.setDishRecipe(dish, recipe);
		
	}

	@Override
	public void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount) {
		stockManager.setDishRestockLevels(dish, restockThreshold, restockAmount);
	}

	@Override
	public Number getRestockThreshold(Dish dish) {
		return stockManager.getRestockAmount(dish);
	}

	@Override
	public Number getRestockAmount(Dish dish) {
		return stockManager.getRestockAmount(dish);
	}

	@Override
	public Map<Ingredient, Number> getRecipe(Dish dish) {
		return stockManager.getRecipe(dish);
	}

	@Override
	public Map<Dish, Number> getDishStockLevels() {
		return stockManager.getDishStockLevels();
	}

	@Override
	public List<Ingredient> getIngredients() {
		return stockManager.getIngredients();
	}

	@Override
	public Ingredient addIngredient(String name, String unit, Supplier supplier, Number restockThreshold, Number restockAmount) {
		return stockManager.addIngredient(new Ingredient(name, unit, supplier), restockThreshold, restockAmount);
	}

	@Override
	public void removeIngredient(Ingredient ingredient) throws UnableToDeleteException {
		stockManager.removeIngredient(ingredient);
	}

	@Override
	public void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount) {
		stockManager.setIngredientRestockLevels(ingredient, restockThreshold, restockAmount);
	}

	@Override
	public Number getRestockThreshold(Ingredient ingredient) {
		return stockManager.getRestockThreshold(ingredient);
	}

	@Override
	public Number getRestockAmount(Ingredient ingredient) {
		return stockManager.getRestockAmount(ingredient);
	}

	@Override
	public Map<Ingredient, Number> getIngredientStockLevels() {
		return stockManager.getIngredientStockLevels();
	}

	@Override
	public List<Supplier> getSuppliers() {
		return suppliers;
	}
	
	@Override
	public Supplier addSupplier(String name, Number distance) {
		Supplier s = new Supplier(name, distance);
		suppliers.add(s);
		return s;
	}

	@Override
	public void removeSupplier(Supplier supplier) throws UnableToDeleteException {
		suppliers.remove(supplier);
	}

	@Override
	public Number getSupplierDistance(Supplier supplier) {
		return supplier.getDistance();
	}

	@Override
	public List<Drone> getDrones() {
		return drones;
	}

	@Override
	public Drone addDrone(Number speed) {
		Drone d = new Drone(speed);
		drones.add(d);
		return d;
	}

	@Override
	public void removeDrone(Drone drone) throws UnableToDeleteException {
		drones.remove(drone);
	}

	@Override
	public Number getDroneSpeed(Drone drone) {
		return drone.getSpeed();
	}

	@Override
	public String getDroneStatus(Drone drone) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Staff> getStaff() {
		return staffList;
	}

	@Override
	public Staff addStaff(String name) {
		Staff s = new Staff(name);
		staffList.add(s);
		return s;
	}

	@Override
	public void removeStaff(Staff staff) throws UnableToDeleteException {
		staffList.remove(staff);
	}

	@Override
	public String getStaffStatus(Staff staff) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Order> getOrders() {
		return orders;
	}

	@Override
	public void removeOrder(Order order) throws UnableToDeleteException {
		orders.remove(order);	
	}

	@Override
	public Number getOrderDistance(Order order) {
		return order.getDistance();
	}

	@Override
	public boolean isOrderComplete(Order order) {
		return order.isFinished();
	}

	@Override
	public String getOrderStatus(Order order) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Number getOrderCost(Order order) {
		return order.getCost();
	}

	@Override
	public List<Postcode> getPostcodes() {
		return postcodes;
	}

	//So every other add method returns what was added but this one doesn't (ಠ.ಠ)
	@Override
	public void addPostcode(String code, Number distance) {
		postcodes.add(new Postcode(code, distance));
	}

	@Override
	public void removePostcode(Postcode postcode) throws UnableToDeleteException {
		postcodes.remove(postcode);
	}

	@Override
	public List<User> getUsers() {
		return users;
	}

	@Override
	public void removeUser(User user) throws UnableToDeleteException {
		users.remove(user);
	}

	@Override
	public void addUpdateListener(UpdateListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyUpdate() {
		// TODO Auto-generated method stub
	}
}
