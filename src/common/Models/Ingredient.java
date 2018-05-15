package common.Models;

import common.Model;

public class Ingredient extends Model {

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
