package common.Models;

import common.Model;

import java.io.Serializable;

public class User extends Model implements Serializable {


	private static final long serialVersionUID = -2862732782989916014L;

	private String username;
	private String password;
	private String address;
	private Postcode postcode;
	
	public User(String username, String password, String address, Postcode postcode) {
		this.username = username;
		this.password = password;
		this.address = address;
		this.postcode = postcode;
	}

	@Override
	public String getName() {		
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getAddress() {
		return address;
	}
	
	public Postcode getPostcode() {
		return postcode;
	}

}
