/*
 * socket.io-java-client IOAcknowledge.java
 *
 * Copyright (c) 2012, Enno Boland
 * PROJECT DESCRIPTION
 * 
 * See LICENSE file for more information
 */
package scripts.io.socket;

import scripts.io.util.json.JSONArray;

/**
 * The Interface IOAcknowledge.
 */
public interface IOAcknowledge {

	/**
	 * Acknowledges a socket.io message.
	 *
	 * @param args may be all types which can be serialized by {@link JSONArray#put(Object)}
	 */
	void ack(Object... args);
}
