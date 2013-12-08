package com.rental.terminal.db;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.rental.terminal.encryption.RSAHandler;


@DatabaseTable(tableName = "cars")
public class Car {

	@DatabaseField(generatedId = true)
	private int id;

	@DatabaseField(canBeNull = false)
	private String name;

	@DatabaseField(canBeNull = false)
	private String publicKey;

	@DatabaseField(canBeNull = false)
	private String privateKey;
	
	@DatabaseField(canBeNull = true, dataType=DataType.SERIALIZABLE)
	private Calendar date;

	@DatabaseField(canBeNull = true)
	private int startMileage;
	
	@DatabaseField(canBeNull = true)
	private int mileage;
	
	@DatabaseField(canBeNull = true)
	private int starts;

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

	public RSAPublicKey getPublicKeyInstance() throws NoSuchAlgorithmException, InvalidKeySpecException {
		RSAHandler rsaHandler = new RSAHandler();

		if (this.publicKey != null) {
			try {
				return rsaHandler.readPublicKeyFromFileSystem(this.publicKey);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public RSAPrivateKey getPrivateKeyInstance() throws NoSuchAlgorithmException, InvalidKeySpecException {
		RSAHandler rsaHandler = new RSAHandler();

		if (this.publicKey != null) {
			try {
				return rsaHandler.readPrivateKeyFromFileSystem(this.privateKey);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
		
		return null;
	}
	
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	
	public Calendar getDate() {
		return date;
	}

	public void setDate(Calendar date) {
		this.date = date;
	}
	
	public int getStartMileage() {
		return startMileage;
	}

	public void setStartMileage(int startMileage) {
		this.startMileage = startMileage;
	}

	public int getMileage() {
		return mileage;
	}

	public void setMileage(int mileage) {
		this.mileage = mileage;
	}
	
	public int getStarts() {
		return starts;
	}

	public void setStarts(int starts) {
		this.starts = starts;
	}
}