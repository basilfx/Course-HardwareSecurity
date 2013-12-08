package com.rental.terminal.db;

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
	private Car car;

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
	
	public Short getCardId() {
		return cardId;
	}

	public void setCardId(Short cardId) {
		this.cardId = cardId;
	}
}
