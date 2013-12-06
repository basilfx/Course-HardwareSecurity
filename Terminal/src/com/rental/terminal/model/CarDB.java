package com.rental.terminal.model;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;

import terminal.Car;
import terminal.JCUtil;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import encryption.RSAHandler;

@DatabaseTable(tableName = "cars")
public class CarDB {

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
	private int finalMileage;

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

	public int getFinalMileage() {
		return finalMileage;
	}

	public void setFinalMileage(int finalMileage) {
		this.finalMileage = finalMileage;
	}

	public Car toCar() throws Exception {
		Car car = new Car();
		
		car.setId((short) this.id);
		car.setDate(this.date != null ? JCUtil.dateToBytes(this.date) : null);
		car.setStartMileage(this.startMileage);
		car.setFinalMileage(this.finalMileage);
		
		RSAHandler rsaHandler = new RSAHandler();

		if (this.publicKey != null) {
			car.setPublicKey(rsaHandler.readPublicKeyFromFileSystem(this.publicKey));
		}
 
		if (this.privateKey != null) {
			car.setPrivateKey(rsaHandler.readPrivateKeyFromFileSystem(this.privateKey));
		}
		
		return car;
	}
}