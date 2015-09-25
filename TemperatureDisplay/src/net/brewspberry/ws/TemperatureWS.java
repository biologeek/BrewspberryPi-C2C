package net.brewspberry.ws;

import java.util.List;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import net.brewspberry.beans.Temperature;

@WebService
@SOAPBinding(style=Style.RPC)
public interface TemperatureWS {

	
	public List<Temperature> getAllLastTemperature();
	public Temperature getLastTemperature(Integer probe);

}
