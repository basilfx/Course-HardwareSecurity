package com.rental.terminal.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "cars")
public class Car {

	@DatabaseField(generatedId = true)
	private int id;

	@DatabaseField(canBeNull = false)
	private String name;

	@DatabaseField(canBeNull = true)
	private String publicKey;

	@DatabaseField(canBeNull = true)
	private String privateKey;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
}