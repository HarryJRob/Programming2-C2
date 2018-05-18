package common.Models;

import java.io.Serializable;

import common.Model;

//Models a ingredient
public class Ingredient extends Model implements Serializable {

	private static final long serialVersionUID = -6039388936067769695L;
	
	private String unit;
	private Supplier supplier;
	
	public Ingredient(String name, String unit, Supplier supplier) {
		this.name = name;
		this.unit = unit;
		this.supplier = supplier;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public String getUnit() {
		return unit;
	}
	
	public Supplier getSupplier() {
		return supplier;
	}

}
