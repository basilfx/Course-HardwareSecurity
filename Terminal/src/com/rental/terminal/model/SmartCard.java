package com.rental.terminal.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "smartcards")
public class SmartCard {

	@DatabaseField(generatedId = true)
	private int id;

	@DatabaseField(canBeNull = false, unique = true)
	private String cardId;

	@DatabaseField(canBeNull = true)
	private String publicKey;
	
	@DatabaseField(canBeNull = true, foreign = true)
	private Car car;
	
	@DatabaseField(canBeNull = true, foreign = true)
	private Customer customer;

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

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	
	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}
}