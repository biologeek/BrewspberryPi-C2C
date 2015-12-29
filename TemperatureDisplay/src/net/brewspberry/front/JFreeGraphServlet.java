package net.brewspberry.front;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.brewspberry.business.IGenericService;
import net.brewspberry.business.beans.Etape;
import net.brewspberry.business.beans.TemperatureMeasurement;
import net.brewspberry.business.service.EtapeServiceImpl;
import net.brewspberry.util.ConfigLoader;
import net.brewspberry.util.Constants;
import net.brewspberry.util.LogManager;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

/**
 * Software :JFreeGraphServlet is a software for generating JFreeChart plots
 * using a CSV file containing timestamps and temperature values
 * 
 * Author : Xavier CARON Version : 1.0 License : free
 */
public class JFreeGraphServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public static final int DEFAULT_WIDTH = 800;
	public static final int DEFAULT_HEIGHT = 300;

	public String JAVA_ROOT_PATH = Constants.PROJECT_ROOT_PATH + "/"
			+ Constants.BREW_TEMP;
	public String API_ROOT_PATH = Constants.PROJECT_ROOT_PATH + "/"
			+ Constants.BREW_API;
	public String FIC_ROOT_PATH = Constants.PROJECT_ROOT_PATH + "/"
			+ Constants.BREW_BATCHES + "/fic";

	public static final Logger logger = LogManager
			.getInstance(JFreeGraphServlet.class.toString());

	public String BCHRECTEMP_FIC = ConfigLoader.getConfigByKey(
			Constants.CONFIG_PROPERTIES, "files.measurements.temperature");

	public int graphHorizontalTimeLengthInMinutes = Integer
			.parseInt(ConfigLoader.getConfigByKey(Constants.CONFIG_PROPERTIES,
					"param.chart.timeLengthInMinutes"));

	static {
		System.setProperty("java.awt.headless", "true");
	}

	static Date firstTime = null;

	IGenericService<Etape> etapeService = new EtapeServiceImpl();

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public JFreeGraphServlet() {
		super();

		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		
		
		// Set standard HTTP/1.1 no-cache headers.
		response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");

		// Set standard HTTP/1.0 no-cache header.
		response.setHeader("Pragma", "no-cache");
		
		response.setDateHeader("Expires", 0);
		
		
		// If the request has either width or height attributes, the chart will
		// be attribute-sized
		if (!(request.getAttribute("width") != null)) {
			try {
				int width = Integer.parseInt((String) request
						.getAttribute("width"));
			} catch (Exception e) {
				int width = DEFAULT_WIDTH;

			}
		} else {
			int width = DEFAULT_WIDTH;
		}
		if (!(request.getAttribute("height") != null)) {
			try {
				int height = Integer.parseInt((String) request
						.getAttribute("height"));
			} catch (Exception e) {
				int height = DEFAULT_HEIGHT;
			}
		}
		// else default sizes are applied
		else {
			int height = DEFAULT_HEIGHT;
		}

		JFreeChart chart = null;

		if (request.getParameter("type") != null) {

			String type = request.getParameter("type");

			switch (type) {

			case "etp":

				Long etapeID = null;
				List<TemperatureMeasurement> tempList = null;
				if (request.getParameter("eid") != null) {

					String eid = request.getParameter("eid");

					etapeID = Long.parseLong(eid);

					tempList = etapeService.getElementById(etapeID)
							.getEtp_temperature_measurement();
					logger.info("Got " + tempList.size()
							+ " temp measurements for step " + etapeID);
					List<String> probesList = new ArrayList<String>();

					probesList = getDistinctProbes(tempList);
					logger.info("Got " + probesList.size()
							+ " temp measurements for step " + etapeID);
					response.setContentType("image/png");

					try {
						chart = generateChartFromTimeSeries(
								createDataset(parseTemperatureMeasurements(
										tempList, probesList)), "DS18B20",
								"Time", "Temperature", true);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				break;
			}
		} else {
			try {
				chart = generateChartFromTimeSeries(
						createDataset(parseCSVFile(new File(BCHRECTEMP_FIC))),
						"DS18B20", "Time", "Temperature", true);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		response.setContentType("image/png");

		OutputStream outputStream = response.getOutputStream();

		try {
			ChartUtilities.writeChartAsPNG(outputStream, chart, DEFAULT_WIDTH,
					DEFAULT_HEIGHT);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/***************************************
	 * parseCSVFile reads and formats CSV file created by BCHRECTEMP batch - IN
	 * : The CSV File to read - OUT a list of arrays of strings : 
	 * {[DATETIME, BREW, STEP, ACTIONER, SENSOR1, ...], [...], ...}
	 ***************************************/
	public List<String[]> parseCSVFile(File file) throws IOException {

		List<String[]> result = new ArrayList<String[]>();
		String[] lineList;

		logger.info("CSV File " + file.getAbsolutePath());
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;

		while ((line = reader.readLine()) != null) {

			lineList = line.split(";", -1);
			result.add(lineList);
		}
		return result;
	}

	public List<String[]> parseTemperatureMeasurements(
			List<TemperatureMeasurement> tempList, List<String> probesList) {
		List<String[]> result = new ArrayList<String[]>();

		Iterator<TemperatureMeasurement> it = tempList.iterator();

		while (it.hasNext()) {
			String[] array = new String[probesList.size()+5];

			TemperatureMeasurement temp = it.next();

			int index = probesList.indexOf(temp.getTmes_probe_name())+5;

			array[0] = temp.getTmes_date().toString();
			array[1] = temp.getTmes_brassin().getBra_id().toString();
			array[2] = temp.getTmes_etape().getEtp_id().toString();
			array[3] = String.valueOf(temp.getTmes_actioner().getAct_id());
			array[4] = String.valueOf(temp.getTmes_probeUI());
			array[index] = temp.getTmes_value().toString();
			
			String toLog = "Line : ";
			for (String el : array){
				
				toLog = toLog+" | "+el;
				
			}
			
			logger.info(toLog);
			result.add(array);
		}

		return result;

	}

	/**
	 * This method creates a TimeSeriesCollection from raw String values
	 * 
	 * @param data
	 * @return
	 * @throws NumberFormatException
	 * @throws ParseException
	 */
	public TimeSeriesCollection createDataset(List<String[]> data)
			throws NumberFormatException, ParseException {

		TimeSeriesCollection dataSet = new TimeSeriesCollection();

		int compteur = data.size();
		List<TimeSeries> serie = new ArrayList<TimeSeries>();

		logger.info("Compteur "+compteur);
		
		
		// On cree autant de series qu'il y a de sondes
		for (int k = 0; k < data.get(0).length - 5; k++) {
			serie.add(new TimeSeries("PROBE" + k));
			logger.info("Added timeSeries PROBE" + k);
		}

		/*
		 * For each line like [2015-07-21 12:34:56, 12345, 54321]
		 */
		for (int i = 0; i < compteur; i++) {

			/*
			 * for each temperature value
			 */
			for (int j = 5; j < data.get(i).length; j++) {

				Date dataDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
						.parse(data.get(i)[0]);


				Calendar cal1 = Calendar.getInstance();
				cal1.setTime(dataDate);
				Calendar cal2 = Calendar.getInstance();
				cal2.add(Calendar.MINUTE, graphHorizontalTimeLengthInMinutes);

				
				logger.info("Beginning date of chart : "+cal1.getTime().toString());
				logger.info("Beginning date of chart : "+cal2.getTime().toString());
				// If the date of record is within the range we add it to Serie
				// Collection
				if (cal1.after(cal2)) {
					// Adds [time, temperature] to the corresponding (i) serie
					serie.get(j-5).addOrUpdate(new Second(dataDate),
							Double.parseDouble(data.get(i)[j]));
				}
			}
		}

		// Adds each serie to the dataset
		for (int l = 0; l < serie.size(); l++) {
			logger.info("serie size : " + serie.get(l).getItemCount());

			dataSet.addSeries(serie.get(l));
		}

		logger.info("dataSet size : " + dataSet.getSeriesCount());

		return dataSet;

	}

	/**
	 * This method generates a Chart using a TimeSeriesCollection. Legend,
	 * title, X-Axis and Y-Axis labels can be modified
	 * 
	 * @param series
	 * @param title
	 * @param xAxisLabel
	 * @param yAxisLabel
	 * @param legend
	 * @return
	 */
	public JFreeChart generateChartFromTimeSeries(TimeSeriesCollection series,
			String title, String xAxisLabel, String yAxisLabel, boolean legend) {

		JFreeChart chart = null;
		boolean defaultTooltips = false;
		boolean defaultURLs = false;

		logger.info("Series collection size : "+ series.getSeriesCount());
		logger.info("Series size : "+ series.getSeries(0).getItemCount());

		chart = ChartFactory.createTimeSeriesChart(title, xAxisLabel,
				yAxisLabel, series, legend, defaultTooltips, defaultURLs);

		return chart;
	}

	public List<String> getDistinctProbes(List<TemperatureMeasurement> list) {

		List<String> result = new ArrayList<String>();

		if (list != null) {

			Iterator<TemperatureMeasurement> it = list.iterator();

			while (it.hasNext()) {

				TemperatureMeasurement temp = it.next();
				logger.info("Trying probe "+temp.getTmes_probe_name());
				if (!result.contains(temp.getTmes_probe_name())) {
					logger.info("Got probe "+temp.getTmes_probe_name());
					result.add(temp.getTmes_probe_name());
				}

			}

		}

		return result;

	}
}
