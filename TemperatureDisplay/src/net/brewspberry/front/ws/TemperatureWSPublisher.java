package net.brewspberry.front.ws;

import javax.xml.ws.Endpoint;

public class TemperatureWSPublisher {

	
	public static void main (String[] args){
		
		Endpoint.publish("http://localhost:8080/ws/TemperatureWS", new TemperatureWSImpl());
	}
}
