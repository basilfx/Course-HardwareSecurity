package com.rental.terminal;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * @author Erwin
 *
 */
public class Car {
	
	private short id;
	private RSAPublicKey public_key;
	private RSAPrivateKey private_key;
	private byte[] date;
	private int startMileage;
	private int finalMileage;
	

	public void setPublicKey(RSAPublicKey public_key) {
		this.public_key = public_key;
	}
	public RSAPublicKey getPublicKey() {
		return public_key;
	}
	public void setDate(byte[] date) {
		this.date = date;
	}
	public byte[] getDate() {
		return date;
	}
	public void setId(short id) {
		this.id = id;
	}
	public short getId() {
		return id;
	}
	public void setStartMileage(int startMileage) {
		this.startMileage = startMileage;
	}
	public int getStartMileage() {
		return startMileage;
	}
	public void setFinalMileage(int finalMileage) {
		this.finalMileage = finalMileage;
	}
	public int getFinalMileage() {
		return finalMileage;
	}
	public void setPrivateKey(RSAPrivateKey private_key) {
		this.private_key = private_key;
	}
	public RSAPrivateKey getPrivateKey() {
		return private_key;
	}
	

}
