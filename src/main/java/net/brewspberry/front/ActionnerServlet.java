package net.brewspberry.front;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.brewspberry.business.IGenericService;
import net.brewspberry.business.ISpecificActionerService;
import net.brewspberry.business.beans.Actioner;
import net.brewspberry.business.beans.Brassin;
import net.brewspberry.business.beans.Etape;
import net.brewspberry.business.service.ActionerServiceImpl;
import net.brewspberry.business.service.BrassinServiceImpl;
import net.brewspberry.business.service.EtapeServiceImpl;
import net.brewspberry.util.Constants;
import net.brewspberry.util.DeviceParser;
import net.brewspberry.util.LogManager;

/**
 * Servlet implementation class Actionner
 */
@WebServlet("/Actionner")
public class ActionnerServlet extends HttpServlet {

	/**
	 * This servlet allows user to either activate, deactivate or use features
	 * of actionners
	 */
	private static final long serialVersionUID = 1L;
	private ISpecificActionerService actionerService = new ActionerServiceImpl();
	private ActionerServiceImpl genActionerService = new ActionerServiceImpl();

	private IGenericService<Brassin> brassinService = new BrassinServiceImpl();
	private IGenericService<Etape> etapeService = new EtapeServiceImpl();
	static Logger logger = LogManager.getInstance(ActionnerServlet.class
			.toString());

	List<Actioner> actioners = new ArrayList<Actioner>();

	Brassin currentBrew = null;
	private Etape currentStep = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ActionnerServlet() {
		super();
	}

	/**
	 * @throws IOException
	 * @throws ServletException
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		if (request.getParameter("bid") != null) {

			try {
				long id = Long.parseLong(request.getParameter("bid"));
				currentBrew = brassinService.getElementById(id);
				
				logger.fine("Got brew "+currentBrew.toString());

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		if (request.getParameter("eid") != null) {

			try {
				long etp_id = Long.parseLong(request.getParameter("eid"));
				currentStep = etapeService.getElementById(etp_id);
				logger.fine("Got brew "+currentStep.toString());

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		if (request.getParameter("type") != null) {

			String actionType = request.getParameter("type");

			switch (actionType) {

			case "listActioner":

				logger.info("Entering actioner listing.");

				/*
				 * option used to list all actioners parametered in
				 * devices.properties If actioner has ID in config file,
				 */
				actioners = DeviceParser.getInstance().getDevices(
						Constants.DEVICES_PROPERTIES);

				for (Actioner actioner : actioners) {

					logger.log(Level.INFO,
							"Nom de l'actionner : " + actioner.getAct_nom());
				}

				request.setAttribute("actioners", actioners);

				break;

			case "activate":

				logger.info("Entering actioner activation");

				/*
				 * Activation of Actioner : sets actioner_activated to True sets
				 * Actioner ID in props file
				 * 
				 * When is it used ?
				 * 
				 * When a step begins, you activate actionners you want to use,
				 * it starts them, meaning for example starting measuring
				 * temperatures or chaging the state of a relay (starting the
				 * pump, ...)
				 * 
				 * Programatically, - Select parametered device in device.properties
				 * (thinking of JSON or XML instead of properties...) 
				 * - Change its status 
				 * - Save it in database
				 */

				String uuid = null;
				Actioner actioner = new Actioner();
				if (request.getParameter("uuid") != null) {

					uuid = request.getParameter("uuid");
					
					// Getting device by UUID

					actioner = DeviceParser.getInstance().getDeviceByUUID(
							Constants.DEVICES_PROPERTIES, uuid);

					
					// If launched in record mode, check if brew and step are filled
					if (currentBrew != null && currentStep != null) {
						
						
						actioner.setAct_brassin(currentBrew);
						actioner.setAct_etape(currentStep);
						actioner.setAct_activated(false); //@see ActionerServiceImpl.startActionInDatabase

						
						try {
							logger.info("Saving Actioner "
									+ actioner.getAct_uuid());
							actioner = actionerService.startAction(actioner); // Checked
							logger.info("Saved actioner ID : "
									+ actioner.getAct_id());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					/*
					 * 
					 * New business rule : No more updating in props file
					 * 
					 * if (actioner.getAct_id() != 0) { try { logger.info(
					 * "Trying to set ID to actioner in devices.properties");
					 * DeviceParser.getInstance() .setIdToActioner(actioner); }
					 * catch (Exception e) { e.printStackTrace();
					 * 
					 * } } else { logger.info("Arf shit ! actioner has id=0"); }
					 */
					
					/*
					 * // Start action if type=ds18b20 if
					 * (actioner.getAct_type().equals(Constants.ACT_DS18B20)) {
					 * 
					 * try { actioner = actionerService.startAction(actioner); }
					 * catch (Exception e) { // TODO Auto-generated catch block
					 * e.printStackTrace(); } }
					 */
					actioners = DeviceParser.getInstance().getDevices(
							Constants.DEVICES_PROPERTIES);

					// Update actioner ID if exists in DB
					for (Actioner act : actioners){

						act = actionerService.isAlreadyStoredAndActivated (act);
						
					}
					// Returning actioners to page

					request.setAttribute("actioners", actioners);

				}

				break;

			case "deactivate":

				logger.info("Entering actioner deactivation");

				long did = 0;
				Actioner dactioner = new Actioner();
				if (request.getParameter("id") != null) {

					try {
						did = Long.parseLong(request.getParameter("id"));
					} catch (Exception e){
						
						logger.severe("Could not convert actioner ID to long");
					}
					
					
					dactioner = genActionerService.getElementById(did);
					
					if (dactioner.getAct_id() != 0) {

						try {
							logger.info("Saved actioner ID : "
									+ dactioner.getAct_id());
							actionerService.stopAction(dactioner);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							logger.info("Trying to set ID to actioner");
							DeviceParser.getInstance().setIdToActioner(
									dactioner);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						// Start action if type=ds18b20
						if (dactioner.getAct_type().equals(
								Constants.ACT_DS18B20)) {

							try {
								dactioner = actionerService
										.startAction(dactioner);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							try {
								throw new Exception(
										"Could not found ID for UUID"
												+ dactioner.getAct_uuid());
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}
					actioners = DeviceParser.getInstance().getDevices(
							Constants.DEVICES_PROPERTIES);

					request.setAttribute("actioners", actioners);

				}

				break;
			}

		}
		request.getRequestDispatcher("actioners.jsp")
				.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
