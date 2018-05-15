
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.*;

import common.*;
import common.Models.*;
import common.communcation.*;


public class ClientApplication implements ClientInterface {

	Socket clientSocket;
	Map<User, Map<Dish, Number>> baskets;
	List<Order> orders;
	
	/*Notes: 
	* 1 If a connection to the server fails then the client will throw an error.
	* 	The GUI should handle this but it doesn't so ¯\_(ツ)_/¯
	*/
	public static void main(String[] args) throws IOException {
		ClientApplication clientApp = new ClientApplication();
		clientApp.launchGUI(clientApp.initialise());
	}
	
	public ClientInterface initialise() throws IOException {
		clientSocket = new Socket("localhost", Comms.PORT_NUM);
		baskets = new HashMap<User, Map<Dish, Number>>();
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

	@Override
	public List<Postcode> getPostcodes() {
		Comms.sendMessage(new Message("GET_POSTCODES", null), clientSocket);
		Integer noRecieving = (Integer) Comms.recieveMessage(clientSocket).getMessageContents();
		ArrayList<Postcode> returnList = new ArrayList<Postcode>();
		for(int i = 0; i < noRecieving; i++) {
			returnList.add((Postcode) Comms.recieveMessage(clientSocket).getMessageContents());
		}
		return returnList;
	}

	@Override
	public List<Dish> getDishes() {
		Comms.sendMessage(new Message("GET_DISHES", null), clientSocket);
		Integer noRecieving = (Integer) Comms.recieveMessage(clientSocket).getMessageContents();
		ArrayList<Dish> returnList = new ArrayList<Dish>();
		for(int i = 0; i < noRecieving; i++) {
			returnList.add((Dish) Comms.recieveMessage(clientSocket).getMessageContents());
		}
		return returnList;
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
			totalCost += (float) d.getPrice() * (float) dishes.get(d);
		}
		return (Number) totalCost;
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
		//TODO: Order takes a distance?
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
		Integer noRecieving = (Integer) Comms.recieveMessage(clientSocket).getMessageContents();
		ArrayList<Order> returnList = new ArrayList<Order>();
		for(int i = 0; i < noRecieving; i++) {
			returnList.add((Order) Comms.recieveMessage(clientSocket).getMessageContents());
		}
		return returnList;
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
		// TODO Auto-generated method stub
		return null;
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
