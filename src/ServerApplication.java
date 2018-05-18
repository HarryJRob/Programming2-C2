import server.*;
import server.StockManager.MoniteredDish;
import server.StockManager.MoniteredIngredient;
import server.StockManager.MoniteredItem;
import server.StockManager.StockManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
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
	private List<Supplier> suppliers;

	
	public static void main(String[] args) {
		ServerApplication serverApplication = new ServerApplication();
		serverApplication.launchGUI(serverApplication.initialise());
	}

	public ServerInterface initialise() {
		stockManager = new StockManager();
		users = new ArrayList<User>();
		postcodes = new ArrayList<Postcode>();
		suppliers = new ArrayList<Supplier>();
		
		new PersistenceLayer().loadPersistence();
		
		new Thread(new ConnectionManager()).start();
		new Thread(stockManager).start();
		
		return this;
	}
	
	public void launchGUI(ServerInterface s) {
		new ServerWindow(s);
	}
	

	//Listens on a server socket and creates new connections
	private class ConnectionManager implements Runnable {

		//A thread for each connection
		//Note: I wanted to have a thread per 25 connections but the user of the Comms class causes issues with this
		private class Connection implements Runnable {

			Socket socket;
			
			public Connection(Socket s) {
				socket = s;
			}
			
			@Override
			public void run() {
				while(true) {
					Message m = Comms.recieveMessage(socket);
					if(m != null)
						System.out.println(socket.toString() + "\t" + m.getMessageType());
					else
						break;
					parseMessage(m, socket);
				}
			}
			
			//Parses a input message
			private void parseMessage(Message m, Socket s) {
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
							Comms.sendMessage(new Message(postcodes.toString(), (ArrayList<Postcode>) postcodes), s);
						}
						break;
					}
					case "GET_DISHES":
					{
						synchronized(stockManager) {
							Comms.sendMessage(new Message("RETURN", (ArrayList<Dish>) stockManager.getDishes()), s);
						}
						break;
					}
					case "NEW_ORDER":
					{
						Order o = (Order) m.getMessageContents();
						stockManager.addOrder(o);
						Comms.sendMessage(m, s);
						break;
					}
					case "GET_ORDERS":
					{

						Comms.sendMessage(new Message("RETURN", (ArrayList<Order>) stockManager.getOrders()), s);
						break;
					}
					case "CANCEL_ORDER":
					{
						Order o = (Order) m.getMessageContents();
						stockManager.removeOrder(o);
						break;
					}
				
				}
				
			}
		}
		
		//Create new connections
		@Override
		public void run() {
			try {
				ServerSocket serverSocket = new ServerSocket(Comms.PORT_NUM);
				while(true) {
					try {
						new Thread (new Connection(serverSocket.accept())).start();;
					} catch (IOException e) { serverSocket.close(); }
				}
			} catch (IOException e) {
				System.out.println("Failed to establish Server Socket");
				e.printStackTrace();
			}
		}
	}
	
	//This makes no sense but specification says so. ¯\_(ツ)_/¯
	//Should just be one method
	private class Configuration {
		
		public void loadConfiguration(String filename) {
			
			users.clear();
			postcodes.clear();
			suppliers.clear();
			stockManager.clearDishes();
			stockManager.clearIngredients();
			stockManager.clearStaff();
			stockManager.clearDrones();
			stockManager.clearOrders();
			
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
						//Whether a ingredient can be added if a invalid supplier is given is unspecified...
						//In my implementation I will assume the supplier must be valid
						for(Supplier s : suppliers) {
							if(s.getName().equals(parts[3])) {
								stockManager.addIngredient(new Ingredient(parts[1], parts[2], s), Float.valueOf(parts[4]), Float.valueOf(parts[5]));
								break;
							}
						}
						break;
						
					case "DISH":
						//Whether a dish can be added if a invalid ingredient is given is unspecified...
						//In my implementation I will assume a dish can only be added given valid ingredients
						Map<Ingredient, Number> ingredientList = new HashMap<Ingredient, Number>();
						for(String curPart : parts[6].split(",")) {
							String[] quants = curPart.split(" * ");
							
							Ingredient i = stockManager.ingredientExists(quants[0].trim());
							if(i == null) {
								break;
							}
							ingredientList.put(i, Float.valueOf(quants[1]));
						}
						stockManager.addDish(new Dish(parts[1], parts[2], Float.valueOf(parts[3]), ingredientList), Float.valueOf(parts[4]), Float.valueOf(parts[5]));
						break;
						
					case "USER":
						//Whether a user can be added if a invalid postcode is given is unspecified...
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
						stockManager.getStaff().add(new Staff(parts[1]));
						break;
					case "DRONE":
						stockManager.getDrones().add(new Drone(Float.valueOf(parts[1])));
						break;
					case "ORDER":
						//Whether a order can be added if a invalid dish is given is unspecified...
						//In my implementation I will assume a order can only be added given valid dishes
						HashMap<Dish, Number> dishList = new HashMap<Dish, Number>();
						for(String curPart : parts[2].split(",")) {
							String[] quants = curPart.split(" * ");
							
							Dish i = stockManager.dishExists(quants[0].trim());
							if(i == null) {
								break;
							}
							dishList.put(i, Float.valueOf(quants[1]));
						}
						for(User u : users) {
							if(u.getName() == parts[1]) {
								stockManager.addOrder(new Order(u, Float.valueOf(parts[2]), dishList));
								break;
							}
						}
						
						break;
					case "STOCK":
						if(!stockManager.setDishStock(parts[1], Float.valueOf(parts[2]))) {
							stockManager.setIngredientStock(parts[1], Float.valueOf(parts[2]));
						}
						break;
					}
				}
			} catch (IOException e) {
				// (☞ﾟヮﾟ)☞  GUI should handle this but it only handles FileNotFoundException ☜(ﾟヮﾟ☜)
				System.out.println("There was an error while processing your configuration file \nThe GUI should handle this but it doesn't :( ");
			}
		}
	}
	
	//This makes no sense but specification says so. ¯\_(ツ)_/¯
	//Should just be two methods which the interface should provide since the backend doesn't know when the front end finishes loading / exits.
	private class PersistenceLayer {
		
		//No Method provided for when the client closes so the backend doesn't know when to do this???
		public void savePersistence() {
			File file = new File("persistence.txt");
			
			try {
				if(file.exists()) {
					file.delete();
					file.createNewFile();
				}
				
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file.getName()));
				
				synchronized(users) {
					for(User u : users) {
						oos.writeObject(u);
					}
				}
				
				oos.writeObject("SPACER");
				
				synchronized(postcodes) {
					for(Postcode p : postcodes) {
						oos.writeObject(p);
					}
				}
				
				oos.writeObject("SPACER");
				
				synchronized(suppliers) {
					for(Supplier s : suppliers) {
						oos.writeObject(s);
					}
				}
	
				oos.writeObject("SPACER");
				
				ArrayList<MoniteredIngredient> items = (ArrayList<MoniteredIngredient>) stockManager.getMoniteredIngredients();
				synchronized(items) {
					for(MoniteredItem i : items) {
						oos.writeObject(i);
					}
				}
				
				oos.writeObject("SPACER");
				
				ArrayList<MoniteredDish> dishes = (ArrayList<MoniteredDish>) stockManager.getMoniteredDishes();
				synchronized(dishes) {
					for(MoniteredDish d : dishes) {
						oos.writeObject(d);
					}
				}
				
				oos.writeObject("SPACER");
				
				ArrayList<Order> orders = stockManager.getOrders();
				
				synchronized(orders) {
					for(Order o : orders) {
						oos.writeObject(o);
					}
				}
				
				oos.writeObject("SPACER");
				
				ArrayList<Staff> staffList = (ArrayList<Staff>) stockManager.getStaff();
				
				synchronized(staffList) {
					for(Staff s : staffList) {
						oos.writeObject(s);
					}
				}
				
				oos.writeObject("SPACER");
				
				ArrayList<Drone> drones = (ArrayList<Drone>) stockManager.getDrones();
				
				synchronized(drones) {
					for(Drone d : drones) {
						oos.writeObject(d);
					}
				}
				
				oos.close();
			} catch(IOException e) {
				System.out.println("Error while saving persistence: \n"+ e.getMessage());
			}
		}
		
		//Load objects from the file detecting their type
		public void loadPersistence() {
			
			File file = new File("persistence.txt");
			
			if(file.exists()) {
				try {
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file.getName()));
					
					Object curObj = null;
					
					do {
						
						curObj = ois.readObject();
						
						if(curObj instanceof User) {
							users.add((User) curObj);
						} else if(curObj instanceof Postcode) {
							postcodes.add((Postcode) curObj);
						} else if(curObj instanceof Supplier) {
							suppliers.add((Supplier) curObj);
						} else if(curObj instanceof MoniteredDish) {
							MoniteredDish d = (MoniteredDish) curObj;
							stockManager.addDish(d.getDish(), d.getResupplyThreshold(), d.getResupplyAmount());
							stockManager.setDishStock(d.getDish(), d.getStockedAmount());
						} else if(curObj instanceof MoniteredIngredient) {
							MoniteredIngredient i = (MoniteredIngredient) curObj;
							stockManager.addIngredient(i.getIngredient(), i.getResupplyThreshold(), i.getResupplyAmount());
							stockManager.setIngredientStock(i.getIngredient(), i.getStockedAmount());
						} else if(curObj instanceof Order) {
							stockManager.addOrder((Order) curObj);
						} else if(curObj instanceof Staff) {
							stockManager.getStaff().add((Staff) curObj);
						} else if(curObj instanceof Drone) {
							stockManager.getDrones().add((Drone) curObj);
						}
						
					} while(curObj != null);
					
					ois.close();
				} catch(IOException | ClassNotFoundException e) {
					System.out.println("Error while loading persistence: \n"+ e.getMessage());
				}
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
		stockManager.willRestockIngredients(enabled);
	}

	@Override
	public void setRestockingDishesEnabled(boolean enabled) {
		stockManager.willRestockDishes(enabled);
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
		return stockManager.getDrones();
	}

	@Override
	public Drone addDrone(Number speed) {
		Drone d = new Drone(speed);
		stockManager.getDrones().add(d);
		return d;
	}

	@Override
	public void removeDrone(Drone drone) throws UnableToDeleteException {
		stockManager.getDrones().remove(drone);
	}

	@Override
	public Number getDroneSpeed(Drone drone) {
		return drone.getSpeed();
	}

	@Override
	public String getDroneStatus(Drone drone) {
		return drone.getStatus();
	}

	@Override
	public List<Staff> getStaff() {
		return stockManager.getStaff();
	}

	@Override
	public Staff addStaff(String name) {
		Staff s = new Staff(name);
		stockManager.getStaff().add(s);
		return s;
	}

	@Override
	public void removeStaff(Staff staff) throws UnableToDeleteException {
		stockManager.getStaff().remove(staff);
	}

	@Override
	public String getStaffStatus(Staff staff) {
		return staff.getStatus();
	}

	@Override
	public List<Order> getOrders() {
		return stockManager.getOrders();
	}

	@Override
	public void removeOrder(Order order) throws UnableToDeleteException {
		stockManager.removeOrder(order);;	
	}

	@Override
	public Number getOrderDistance(Order order) {
		return order.getDistance();
	}

	@Override
	public boolean isOrderComplete(Order order) {
		return order.getStatus().equals("Complete");
	}

	@Override
	public String getOrderStatus(Order order) {
		return order.getStatus();
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
