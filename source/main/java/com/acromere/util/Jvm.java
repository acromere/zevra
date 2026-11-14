package com.acromere.util;

import java.security.SecureRandom;

/**
 * JVM-specific constants and helpers for the current Java virtual machine.
 */
public class Jvm {

	/**
	 * A unique identifier for the current JVM instance. Generated randomly at
	 * class load time, so it differs between JVM instances but is constant
	 * within a single run.
	 */
	public static final long ID = Math.abs( new SecureRandom().nextLong() );

}
