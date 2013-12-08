package com.rental.terminal.db;

import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.rental.terminal.model.CarDB;
import com.rental.terminal.model.SmartCardDB;

public class Manager {
	private final static String DATABASE_URL = "jdbc:sqlite:database.db";
	
	private JdbcConnectionSource connection;
	
	private Dao<CarDB, Integer> carDao;
	private Dao<SmartCardDB, Integer> smartCardDao;

	public Manager() throws SQLException {
		connection = new JdbcConnectionSource(DATABASE_URL);

		// Setup DAO
		this.carDao = DaoManager.createDao(connection, CarDB.class);
		this.smartCardDao = DaoManager.createDao(connection, SmartCardDB.class);
		

        // Setup the table
        TableUtils.createTableIfNotExists(this.connection, CarDB.class);
        TableUtils.createTableIfNotExists(this.connection, SmartCardDB.class);
	}
	
	public Dao<CarDB, Integer> getCarDao() {
		return this.carDao;
	}
	
	public Dao<SmartCardDB, Integer> getSmartCardDao() {
		return this.smartCardDao;
	}
}
