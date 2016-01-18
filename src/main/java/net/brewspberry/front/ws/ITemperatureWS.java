package net.brewspberry.front.ws;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import net.brewspberry.business.beans.TemperatureMeasurement;

@WebService
@SOAPBinding(style=Style.RPC)
public interface ITemperatureWS {

	
	@WebMethod TemperatureMeasurement getTemperatureMeasurementByProbe (String uuidOrname, Boolean uuid);
	@WebMethod List<TemperatureMeasurement> getAllTemperatureMeasurements(
			List<String> uuidOrname, Boolean uuid) throws Exception;
	
	@WebMethod TemperatureMeasurement getCSVTemperatureMeasurementByProbe (String uuidOrname, Boolean uuid);
	@WebMethod List<TemperatureMeasurement> getAllCSVTemperatureMeasurements(
			List<String> uuidOrname, Boolean uuid) throws Exception;
	
}
