package com.rental.terminal.model;

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

/**
 * Car model
 * 
 * @author Bas Stottelaar
 * @author Jeroen Senden
 */
@DatabaseTable(tableName = "cars")
public class Car {

	@DatabaseField(generatedId = true)
	private int id;

	@DatabaseField(canBeNull = false)
	private String name;

	@DatabaseField(canBeNull = false, dataType=DataType.SERIALIZABLE)
	private RSAPublicKey publicKey;

	@DatabaseField(canBeNull = false, dataType=DataType.SERIALIZABLE)
	private RSAPrivateKey privateKey;
	
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

	public RSAPublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(RSAPublicKey publicKey) {
		this.publicKey = publicKey;
	}
	
	public void setPublicKeyFromFile(String file) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		this.publicKey = new RSAHandler().readPublicKeyFromFileSystem(file);
	}

	public RSAPrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(RSAPrivateKey privateKey) {
		this.privateKey = privateKey;
	}
	
	public void setPrivateKeyFromFile(String file) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		this.privateKey = new RSAHandler().readPrivateKeyFromFileSystem(file);
	}
	
	public String toString() {
		return String.format("%s (%d KM)", this.name, this.mileage);
	}
}