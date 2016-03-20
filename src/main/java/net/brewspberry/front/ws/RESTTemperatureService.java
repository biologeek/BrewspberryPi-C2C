package net.brewspberry.front.ws;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.brewspberry.business.IGenericService;
import net.brewspberry.business.ISpecificTemperatureMeasurementService;
import net.brewspberry.business.beans.Etape;
import net.brewspberry.business.beans.TemperatureMeasurement;
import net.brewspberry.business.service.EtapeServiceImpl;
import net.brewspberry.business.service.TemperatureMeasurementServiceImpl;
import net.brewspberry.util.LogManager;

@Path("/")
public class RESTTemperatureService {

	IGenericService<TemperatureMeasurement> tmesService = new TemperatureMeasurementServiceImpl();
	ISpecificTemperatureMeasurementService tmesSpecService = new TemperatureMeasurementServiceImpl();
	IGenericService<Etape> stepService = new EtapeServiceImpl();
	private Etape currentStep;
	
	Logger logger = LogManager.getInstance(RESTTemperatureService.class.getName());
	@GET
	@Path("/initTemperatures/e/{e}/u/{u}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response initTemperatureForStep(@PathParam("u") String uuid,
			@PathParam("e") long stepID) throws JSONException {

		JSONObject json = new JSONObject();
		JSONArray result = null;

		List<TemperatureMeasurement> filteredList = new ArrayList<TemperatureMeasurement>();

		if (stepID > 0) {

			currentStep = stepService.getElementById(stepID);

			List<TemperatureMeasurement> tmesList = tmesSpecService
					.getTemperatureMeasurementByEtape(currentStep);

			if (uuid != null) {

				if (uuid != "all") {
					Iterator<TemperatureMeasurement> it = tmesList.iterator();

					while (it.hasNext()) {
						TemperatureMeasurement next = it.next();
						if (next.getTmes_probeUI() == uuid) {
							filteredList.add(next);
						}

					}
					result = this.convertListToJSONObject(filteredList);

				} else {
					result = this.convertListToJSONObject(tmesList);
				}
			} else {
				result = this.convertListToJSONObject(tmesList);
			}

		}

		return Response.status(200).entity(result.toString()).build();
	}

	@GET
	@Path("/updateTemperatures/e/{e}/u/{u}/l/{l}")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Returns tempreatures for stepID step, eventually probe with UUID uuid or all for all probes
	 * since last ID in graph (lastID)
	 * @param stepID
	 * @param uuid
	 * @param lastID
	 * @return
	 */
	public Response updateTemperatureForStep(@PathParam("e") long stepID,
			@PathParam("u") String uuid, @PathParam("l") long lastID) {

		List<TemperatureMeasurement> result = new ArrayList<TemperatureMeasurement>();
		
		if (lastID > 0){
			
			Etape etape = new Etape();
			
			etape.setEtp_id(stepID);
			
			result = tmesSpecService.getTemperatureMeasurementsAfterID(etape, uuid, lastID);
			
			
		} else {
			
			logger.warning("You are trying to update with ID 0");
		}
		

		return Response.status(200).entity(result).build();
	}

	@GET
	@Path("/test")
	public Response testWS() {

		return Response.status(200).entity("Test OK").build();

	}

	JSONArray convertListToJSONObject(List<TemperatureMeasurement> toConvert)
			throws JSONException {
		JSONArray json = new JSONArray();

		if (toConvert != null && toConvert.size() > 0) {

			Iterator<TemperatureMeasurement> it = toConvert.iterator();

			while (it.hasNext()) {

				TemperatureMeasurement tmes = it.next();

				JSONObject js = this.convertToJSONObject(tmes);

				json.put(js);
			}
		}

		return json;
	}

	JSONObject convertToJSONObject(TemperatureMeasurement toConvert)
			throws JSONException {
		JSONObject json = null;

		if (toConvert != null) {

			json = new JSONObject();

			json.put("id", toConvert.getTmes_id());
			json.put("uuid", toConvert.getTmes_probeUI());
			json.put("date", toConvert.getTmes_date());
			json.put("name", toConvert.getTmes_probe_name());
			json.put("brew", toConvert.getTmes_brassin().getBra_id());
			json.put("step", toConvert.getTmes_etape().getEtp_id());
			json.put("temp", toConvert.getTmes_value());

		}

		return json;
	}

}
