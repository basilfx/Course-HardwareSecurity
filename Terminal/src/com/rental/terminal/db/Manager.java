package com.rental.terminal.db;

import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.rental.terminal.model.Car;
import com.rental.terminal.model.Customer;
import com.rental.terminal.model.SmartCard;

public class Manager {
	private final static String DATABASE_URL = "jdbc:sqlite:database.db";
	
	private JdbcConnectionSource connection;
	
	private Dao<Car, Integer> carDao;
	private Dao<SmartCard, Integer> smartCardDao;
	private Dao<Customer, Integer> customerDao;

	public Manager() throws SQLException {
		connection = new JdbcConnectionSource(DATABASE_URL);

		// Setup DAO
		this.carDao = DaoManager.createDao(connection, Car.class);
		this.smartCardDao = DaoManager.createDao(connection, SmartCard.class);
		this.customerDao = DaoManager.createDao(connection, Customer.class);
		

        // Setup the table
        TableUtils.createTableIfNotExists(this.connection, Car.class);
        TableUtils.createTableIfNotExists(this.connection, SmartCard.class);
        TableUtils.createTableIfNotExists(this.connection, Customer.class);
	}
	
	public Dao<Car, Integer> getCarDao() {
		return this.carDao;
	}
	
	public Dao<SmartCard, Integer> getSmartCardDao() {
		return this.smartCardDao;
	}
	
	public Dao<Customer, Integer> getCustomerDao() {
		return this.customerDao;
	}
}
