package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

public class StaticLoggerBinder implements LoggerFactoryBinder {

	private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

	public static final StaticLoggerBinder getSingleton() {
		return SINGLETON;
	}

	public static String REQUESTED_API_VERSION = "1.6.99"; // !final

	private static final String loggerFactoryClassStr = Slft4jLoggerFactory.class.getName();

	public ILoggerFactory getLoggerFactory() {
		return loggerFactory;
	}

	public String getLoggerFactoryClassStr() {

		return loggerFactoryClassStr;
	}

	private final ILoggerFactory loggerFactory;

	private StaticLoggerBinder() {
		loggerFactory = new Slft4jLoggerFactory();
	}

}