
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.*;

import common.*;
import common.Models.*;
import common.communcation.*;

//Testing VSCode
public class ClientApplication implements ClientInterface {

	Socket clientSocket;
	HashMap<User, HashMap<Dish, Number>> baskets;
	List<Order> orders;
	
	/*Notes: 
	* 1 If a connection to the server fails then the client will throw an error.
	* 	The GUI should handle this but it doesn't so ¯\_(ツ)_/¯
	* 2 A thread is needed to constantly check for updates from the server.
	* 	I did not have time to implement this and so some functionality will not work
	*/
	public static void main(String[] args) throws IOException {
		ClientApplication clientApp = new ClientApplication();
		clientApp.launchGUI(clientApp.initialise());
	}
	
	public ClientInterface initialise() throws IOException {
		clientSocket = new Socket("localhost", Comms.PORT_NUM);
		baskets = new HashMap<User, HashMap<Dish, Number>>();
		return this;
	}
	
	public void launchGUI(ClientInterface c) {
		new ClientWindow(c);
	}
	
	@Override
	public User register(String username, String password, String address, Postcode postcode) {
		Comms.sendMessage(new Message("REGISTER_USER", new User(username, password, address, postcode)), clientSocket);
		return (User) Comms.recieveMessage(clientSocket).getMessageContents();
	}

	@Override
	public User login(String username, String password) {
		Comms.sendMessage(new Message("LOGIN_USER", new User(username, password, null, null)), clientSocket);
		return (User) Comms.recieveMessage(clientSocket).getMessageContents();
	}

	//Why u spamerino??
	@Override
	public List<Postcode> getPostcodes() {
		Comms.sendMessage(new Message("GET_POSTCODES", null), clientSocket);
		return (List<Postcode>) Comms.recieveMessage(clientSocket).getMessageContents();
	}

	@Override
	public List<Dish> getDishes() {
		Comms.sendMessage(new Message("GET_DISHES", null), clientSocket);
		return (List<Dish>) Comms.recieveMessage(clientSocket).getMessageContents();
	}

	@Override
	public String getDishDescription(Dish dish) {
		return dish.getDescription();
	}

	@Override
	public Number getDishPrice(Dish dish) {
		return dish.getPrice();
	}

	@Override
	public Map<Dish, Number> getBasket(User user) {
		return baskets.get(user);
	}

	@Override
	public Number getBasketCost(User user) {
		Map<Dish, Number> dishes = baskets.get(user); 
		float totalCost = 0;
		for(Dish d : dishes.keySet()) {
			totalCost += d.getPrice().floatValue() * dishes.get(d).floatValue();
		}
		return totalCost;
	}

	@Override
	public void addDishToBasket(User user, Dish dish, Number quantity) {
		if(!baskets.containsKey(user)) {
			baskets.put(user, new HashMap<Dish, Number>());
		}
		
		Map<Dish, Number> dishes = baskets.get(user);
		if(!dishes.containsKey(dish))
			dishes.put(dish, quantity);
	}

	@Override
	public void updateDishInBasket(User user, Dish dish, Number quantity) {
		Map<Dish, Number> dishes = baskets.get(user);
		if(dishes.containsKey(dish))
			dishes.put(dish, quantity);
	}

	@Override
	public Order checkoutBasket(User user) {
		Order o = new Order(user,0 ,baskets.get(user));
		Comms.sendMessage(new Message("NEW_ORDER", o), clientSocket);
		return o;
	}

	@Override
	public void clearBasket(User user) {
		baskets.remove(user);
	}

	@Override
	public List<Order> getOrders(User user) {
		Comms.sendMessage(new Message("GET_ORDERS", user), clientSocket);
		return (List<Order>) Comms.recieveMessage(clientSocket).getMessageContents();
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
	public void cancelOrder(Order order) {
		Comms.sendMessage(new Message("CANCEL_ORDER", order), clientSocket);
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
