package net.brewspberry.ws.publisher;

import javax.xml.ws.Endpoint;

import net.brewspberry.ws.impl.TemperatureWSImpl;


public class TemperatureWSPublisher {

	
	
	public static void main(String[] args){
		Endpoint.publish("http://127.0.0.1:54321/ws/TemperatureWS", new TemperatureWSImpl());
	}
}
