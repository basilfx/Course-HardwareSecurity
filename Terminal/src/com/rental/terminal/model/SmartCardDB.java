package com.rental.terminal.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "smartcards")
public class SmartCardDB {

	@DatabaseField(generatedId = true)
	private int id;

	@DatabaseField(canBeNull = false, unique = true)
	private Short cardId;

	@DatabaseField(canBeNull = true)
	private String publicKey;
	
	@DatabaseField(canBeNull = true, foreign = true)
	private CarDB car;
	
	@DatabaseField(canBeNull = true, foreign = true)
	private CustomerDB customer;

	//Getters and Setters
	public String getPublicKey() {
		return publicKey;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public CarDB getCar() {
		return car;
	}

	public void setCar(CarDB car) {
		this.car = car;
	}

	public CustomerDB getCustomer() {
		return customer;
	}

	public void setCustomer(CustomerDB customer) {
		this.customer = customer;
	}
	
	public Short getCardId() {
		return cardId;
	}

	public void setCardId(Short cardId) {
		this.cardId = cardId;
	}
}