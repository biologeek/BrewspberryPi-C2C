package net.brewspberry.front.ws;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.criteria.CriteriaBuilder.In;
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

	Logger logger = LogManager.getInstance(RESTTemperatureService.class
			.getName());

	@GET
	@Path("/initTemperatures/e/{e}/u/{u}/limitTo/{limitTo}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response initTemperatureForStep(@PathParam("u") String uuid,
			@PathParam("e") long stepID, @PathParam("limitTo") int limit)
			throws JSONException {

		JSONObject json = new JSONObject();
		JSONArray result = null;

		List<TemperatureMeasurement> filteredList = new ArrayList<TemperatureMeasurement>();

		if (stepID > 0) {

			currentStep = stepService.getElementById(stepID);

			List<TemperatureMeasurement> tmesList = tmesSpecService
					.getTemperatureMeasurementByEtape(currentStep);
			logger.fine("List size : "+tmesList.size());

			if (uuid != null) {

				if (!uuid.equals("all")) {
					Iterator<TemperatureMeasurement> it = tmesList.iterator();

					while (it.hasNext()) {
						TemperatureMeasurement next = it.next();
						if (next.getTmes_probeUI().equals(uuid)) {
							filteredList.add(next);
						}

					}
					tmesList = filteredList;
				}

			}

			logger.fine("List size : "+tmesList.size());

			while (tmesList.size() > limit) {
				
				logger.fine("List size : "+tmesList.size());
				tmesList.remove(0);
			}
			result = this.convertListToJSONObject(tmesList);
		}
		return Response.status(200).entity(result.toString()).build();
	}

	@GET
	@Path("/initTemperatures/e/{e}")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * 
	 * @param stepID
	 * @return
	 * @throws JSONException
	 */
	public Response initTemperature(@PathParam("e") long stepID)
			throws JSONException {

		JSONObject json = new JSONObject();
		JSONArray result = null;

		if (stepID > 0) {

			currentStep = stepService.getElementById(stepID);

			List<TemperatureMeasurement> tmesList = tmesSpecService
					.getTemperatureMeasurementByEtape(currentStep);

			result = this.convertListToJSONObject(tmesList);

		}

		return Response.status(200).entity(result.toString()).build();

	}

	@GET
	@Path("/updateTemperatures/e/{e}/u/{u}/l/{l}/d/{d}")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Returns tempreatures for stepID step, eventually probe with UUID uuid or all for all probes
	 * since last ID in graph (lastID)
	 * @param stepID
	 * @param uuid
	 * @param lastID
	 * @param delayInMinutes
	 * @return
	 */
	public Response updateTemperatureForStep(@PathParam("e") long stepID,
			@PathParam("u") String uuid, @PathParam("l") long lastID,
			@PathParam("d") int delayInMinutes) throws JSONException {

		List<TemperatureMeasurement> result = new ArrayList<TemperatureMeasurement>();
		JSONArray jsonResult = null;
		if (lastID > 0) {

			Etape etape = new Etape();

			etape.setEtp_id(stepID);

			result = tmesSpecService.getTemperatureMeasurementsAfterID(etape,
					uuid, lastID, delayInMinutes);
			jsonResult = this.convertListToJSONObject(result);

		} else {

			logger.warning("You are trying to update with ID 0");
		}

		return Response.status(200).entity(jsonResult.toString()).build();
	}

	/**
	 * Method used to return most recent measured temperature for step and
	 * eventually UUID.
	 * 
	 * If you want to get temperatures for all available
	 * 
	 * @param stepID
	 * @param uuid
	 * @return
	 * @throws JSONException
	 */
	@GET
	@Path("/lastTemperatureValue")
	public Response getLastTemperatureValue(@PathParam("e") long stepID,
			@PathParam("u") String uuid) throws JSONException {

		List<TemperatureMeasurement> response = new ArrayList<TemperatureMeasurement>();
		JSONArray jsonResult = null;

		if (stepID > 0 && uuid != null) {

			Etape step = stepService.getElementById(stepID);

			response = tmesSpecService.getLastTemperatureByStepAndUUID(step,
					uuid);

			if (!uuid.equalsIgnoreCase("all") && response.size() == 1) {
				logger.fine("Got " + response.size() + " for UUID " + uuid
						+ " and step " + stepID);
			}

		}

		jsonResult = this.convertListToJSONObject(response);

		return Response.status(200).entity(jsonResult.toString()).build();
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

	/**
	 * Puts object attributes in JSON object
	 * 
	 * @param toConvert
	 * @return
	 * @throws JSONException
	 */
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
