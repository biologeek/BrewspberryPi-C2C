package net.brewspberry.business.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

import net.brewspberry.adapter.RelayAdapter;
import net.brewspberry.batches.launchers.Batch;
import net.brewspberry.batches.launchers.BatchRecordTemperatures;
import net.brewspberry.business.ISpecificActionerDao;
import net.brewspberry.business.ISpecificActionerLauncherService;
import net.brewspberry.business.ISpecificActionerService;
import net.brewspberry.business.beans.Actioner;
import net.brewspberry.business.beans.Etape;
import net.brewspberry.dao.ActionerDaoImpl;
import net.brewspberry.util.ConfigLoader;
import net.brewspberry.util.Constants;
import net.brewspberry.util.LogManager;

public class BatchLauncherService implements ISpecificActionerLauncherService{

	private Logger logger = LogManager.getInstance(BatchLauncherService.class.getName());
	
	private ISpecificActionerService actionerService = new ActionerServiceImpl();


	Batch temperatureBatch;
	Thread recordTemperatureBatch;


	String commandLineRegexp = "/home/pi/batches/bchrectemp.py [0-9]{0,5} [0-9]{0,5}";

	Pattern pattern = Pattern.compile(commandLineRegexp);

	String getTemperatureRunningGrep = "ps -ef | grep bchrectemp.py";
	
	private RelayAdapter relayAdapter = RelayAdapter.getInstance();

	private final GpioController gpioController = GpioFactory.getInstance();

	@Override
	public Actioner startAction(Actioner actioner) throws Exception {



		Etape currentStep = null;
		String duration = "";
		String[] args = new String[5];
		
		

		if (actioner != null) {

			logger .fine(actioner.getAct_etape() + " "
					+ actioner.getAct_brassin());
			switch (actioner.getAct_type()) {

			case "1":
				// DS18B20

				/*
				 * When loading ds18b20, it executes in a separate thread a time
				 * limited batch to record temperatures
				 * 
				 * Requires act_type, act_brassin, act_etape, name, uuid
				 */

				String durationCoef = ConfigLoader.getConfigByKey(
						Constants.CONFIG_PROPERTIES,
						"param.batches.length.coef");

				logger.info("It's a DS18B20 :");


				logger.fine("Duree : " + actioner.getAct_etape().getEtp_duree());
				try {
					if (actioner.getAct_brassin() != null
							&& actioner.getAct_etape() != null) {

						currentStep = actioner.getAct_etape();

						/*
						 * Duration of step converted in Minutes * Coefficient =
						 * recommended time In case thread is not finished at
						 * the end of real step, a security mechanism is to
						 * interrupt it.
						 * 
						 * Is implemented in
						 * 
						 * @see stopActionInDatabase
						 */

						actioner = actionerService.startActionInDatabase(actioner);

						duration = String.valueOf((double) currentStep
								.getEtp_duree().getMinute()
								* (Double.parseDouble(durationCoef)));

						args[0] = "MINUTE";
						args[1] = String.valueOf(duration);
						args[2] = String.valueOf(actioner.getAct_brassin().getBra_id());
						args[3] = String.valueOf(actioner.getAct_etape().getEtp_id());
						args[4] = String.valueOf(actioner.getAct_id());

						
						logger.fine("Launching batch thread for "+duration+" "+args[0]);
						temperatureBatch = new BatchRecordTemperatures(args);

						recordTemperatureBatch = new Thread((Runnable) temperatureBatch);

						recordTemperatureBatch.start();

					} else {

						logger.warning("TemperatureMeasurement not available without Step and Brew...");
					}

				} catch (Throwable e) {

					e.printStackTrace();
					// TODO: handle exception
				}

				break;

			case "2" :
			case "3" :
				GpioPinDigitalOutput gpio = gpioController
				.provisionDigitalOutputPin(Constants.BREW_GPIO
						.get(actioner.getAct_raspi_pin()));
				// Relay
				logger.info("It's a relay !");

				if (actioner.getAct_raspi_pin() != "") {

					logger.info("Provisionning pin "
							+ actioner.getAct_raspi_pin()
							+ " "
							+ Constants.BREW_GPIO.get(actioner
									.getAct_raspi_pin()));
					try {
						
						// Turning ON or OFF the pin
						PinState state = relayAdapter.changePinState(gpio);

						actioner.setAct_status(Constants.ACT_RUNNING);
						logger.info("Actioner at pin "
								+ actioner.getAct_raspi_pin()
								+ " changed state to "
								+ actioner.getAct_status());

					} catch (Exception e) {

						logger.severe("Couldn't change Pin state..."
								+ actioner.getAct_raspi_pin()
								+ ", setting status to FAILED !");
						actioner.setAct_status(Constants.ACT_FAILED);

					}
					actioner = actionerService.startActionInDatabase(actioner);

				} else {
					throw new Exception("Empty Pin !!");
				}
				
				gpioController.unprovisionPin(gpio);

				break;

			}

		}
		return null;
	}

	public Actioner stopAction(Actioner actioner) throws Exception {
		if (actioner != null && actioner.getAct_id() != 0) {

			if (ConfigLoader.getConfigByKey(Constants.CONFIG_PROPERTIES,
					"param.batches.language").equals("java")) {

				if (recordTemperatureBatch != null) {

					if (recordTemperatureBatch.isAlive()) {

						recordTemperatureBatch.interrupt();

						actionerService.stopActionInDatabase(actioner);

						// Wait for it to end
						recordTemperatureBatch.join();

					} else {
						logger.fine("Thread is dead");

					}

				} else {
					logger.severe("Thread is null !!");
				}

			} else if (ConfigLoader.getConfigByKey(Constants.CONFIG_PROPERTIES,
					"param.batches.language").equals("python")) {
				switch (actioner.getAct_type()) {

				case "1":

					// ds18b20

					// First, checks if a process is currently running
					Process proc = null;
					try {
						proc = Runtime.getRuntime().exec(
								getTemperatureRunningGrep);
					} catch (IOException e) {
						e.printStackTrace();
					}

					BufferedReader br = new BufferedReader(
							new InputStreamReader(proc.getInputStream()));

					String line = null;

					int counter = 0;
					while ((line = br.readLine()) != null) {

						// Checks if it is really the running batch
						if (line.matches(commandLineRegexp)) {

							System.out.println("Matching process found !");

							/*
							 * Returns PID of program using bra_id, etape_id and
							 * act_id So no way we kill the wrong process
							 */
							String pid = actionerService
									.getPIDFromPs("bchrectemp.py "
											+ actioner.getAct_brassin()
													.getBra_id()
											+ " "
											+ actioner.getAct_etape()
													.getEtp_id())
									+ " " + actioner.getAct_id();

							if (pid != "") {

								// Kills the process !
								Runtime.getRuntime().exec(
										"kill -SIGTERM " + pid);
								counter++;
							}

							// records in DB

							actioner = actionerService.stopActionInDatabase(actioner);
						}
					}

					break;

				case "2":

					// Relay

					GpioPinDigitalOutput gpio = null;
					if (actioner.getAct_raspi_pin() != null) {
						gpio = gpioController
								.provisionDigitalOutputPin(Constants.BREW_GPIO
										.get(actioner.getAct_raspi_pin()));

						if (gpio.getState().equals(PinState.HIGH)) {

							relayAdapter.changePinState(gpio);

							// Not recording relay actions as therre may be more
							// than one
							// actioner = this.stopActionInDatabase(actioner);
						}

					}
					

					relayAdapter.setShutdownOptionsandShutdown(gpioController,gpio , PinState.LOW, true);
					break;

				}

			} else {

				logger.severe("Actioner ID is null, can't stop null actioner !!");
				throw new Exception();
			}
		} else {

			logger.warning("Unknown language");

		}
		return actioner;

	}



}
