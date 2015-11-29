package net.brewspberry.front.ws;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jws.WebMethod;
import javax.jws.WebService;

import net.brewspberry.business.ISpecificTemperatureMeasurementService;
import net.brewspberry.business.beans.TemperatureMeasurement;
import net.brewspberry.business.service.TemperatureMeasurementServiceImpl;

@WebService
public class TemperatureWSImpl implements ITemperatureWS {

	ISpecificTemperatureMeasurementService specTMesService = new TemperatureMeasurementServiceImpl();

	String uuidRegex = "[0-9a-f]{2}-[0-9a-f]{12}";

	@Override
	@WebMethod
	public TemperatureMeasurement getTemperatureMeasurementByProbe(
			String uuidOrname, Boolean uuid) {

		
		TemperatureMeasurement result = new TemperatureMeasurement();

		if (uuidOrname != null && uuid != null) {

			if (uuid) {
				// Search by UUID

				Pattern p = Pattern.compile(uuidRegex);
				Matcher m = p.matcher(uuidOrname);

				System.out.println(uuidOrname);
				if (m.matches()) {

					try {
						result = specTMesService
								.getLastTemperatureMeasurementByUUID(uuidOrname);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else {
					System.out.println("Input doesn't match !!!");
				}

			} else {
				try {
					result = specTMesService
							.getLastTemperatureMeasurementByName(uuidOrname);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	@Override
	@WebMethod
	public List<TemperatureMeasurement> getAllTemperatureMeasurements(
			List<String> uuidOrname, Boolean uuid) throws Exception {
		List<TemperatureMeasurement> result = new ArrayList<TemperatureMeasurement>();

		try {
			result = specTMesService.getAllLastTemperatureMeasurements(
					uuidOrname, uuid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (result.size() <= 0) {

			throw new Exception("Result list empty");

		}

		return result;
	}

	@Override
	public TemperatureMeasurement getCSVTemperatureMeasurementByProbe(
			String uuidOrname, Boolean uuid) {
		TemperatureMeasurement result = new TemperatureMeasurement();
		
		if (uuid != null){
			
			try {
				
				result = specTMesService.getLastTemperatureMeasurementsByNameFromCSV(uuidOrname,uuid);
				
				
			} catch (Exception e ){
				
				e.printStackTrace();
				
			}			
		}	
		
		return result;
	}

	@Override
	public List<TemperatureMeasurement> getAllCSVTemperatureMeasurements(
			List<String> uuidOrname, Boolean uuid) throws Exception {
		List<TemperatureMeasurement> result = new ArrayList<TemperatureMeasurement>();
		
		if (uuid != null){
			
			try {
				
				result = specTMesService.getAllLastTemperatureMeasurementsFromCSV(uuidOrname,uuid);
				
				
			} catch (Exception e ){
				
				e.printStackTrace();
				
			}
			
		}
		
		
		return result;
	}

}
