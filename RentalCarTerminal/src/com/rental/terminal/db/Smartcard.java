package com.rental.terminal.db;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.rental.terminal.encryption.RSAHandler;

/**
 * Smart card model
 * 
 * @author Bas Stottelaar
 * @author Jeroen Senden
 */
@DatabaseTable(tableName = "smartcards")
public class Smartcard {

	@DatabaseField(generatedId = true)
	private int id;

	@DatabaseField(canBeNull = false, unique = true)
	private short cardId;

	@DatabaseField(canBeNull = true, dataType=DataType.SERIALIZABLE)
	private RSAPublicKey publicKey;
	
	@DatabaseField(canBeNull = true, dataType=DataType.SERIALIZABLE)
	private RSAPrivateKey privateKey;
	
	@DatabaseField(canBeNull = true)
	private byte[] signature;
	
	@DatabaseField(canBeNull = true, foreign = true)
	private Car car;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}
	
	public short getCardId() {
		return cardId;
	}

	public void setCardId(Short cardId) {
		this.cardId = cardId;
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
}
