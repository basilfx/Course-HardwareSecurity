package com.rental.terminal.db;

import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class Manager {
	private final static String DATABASE_URL = "jdbc:sqlite:database.db";
	
	private JdbcConnectionSource connection;
	
	private Dao<Car, Integer> carDao;
	private Dao<SmartCardDB, Integer> smartCardDao;

	public Manager() throws SQLException {
		connection = new JdbcConnectionSource(DATABASE_URL);

		// Setup DAO
		this.carDao = DaoManager.createDao(connection, Car.class);
		this.smartCardDao = DaoManager.createDao(connection, SmartCardDB.class);
		

        // Setup the table
        TableUtils.createTableIfNotExists(this.connection, Car.class);
        TableUtils.createTableIfNotExists(this.connection, SmartCardDB.class);
	}
	
	public Dao<Car, Integer> getCarDao() {
		return this.carDao;
	}
	
	public Dao<SmartCardDB, Integer> getSmartCardDao() {
		return this.smartCardDao;
	}
}
