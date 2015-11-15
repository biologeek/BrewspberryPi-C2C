package net.brewspberry.front;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
 * Software :JFreeGraphServlet is a software for generating JFreeChart plots using a CSV file containing timestamps and temperature values
 * 
 * Author : Xavier CARON
 * Version : 1.0
 * License : free
 */
public class JFreeGraphServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static final int DEFAULT_WIDTH = 800;
	public static final int DEFAULT_HEIGHT = 300;
	
	
	public static String PROJECT_ROOT_PATH = "/var/lib/tomcat7/webapps/ROOT/"+"/";
	
	public static String JAVA_ROOT_PATH = PROJECT_ROOT_PATH+"TemperatureDisplay/";
	public static String FIC_ROOT_PATH = PROJECT_ROOT_PATH+"fic/";
	
	public static String BCHRECTEMP_FIC = FIC_ROOT_PATH+"ds18b20_raw_measurements.csv";
	


	static Date firstTime = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public JFreeGraphServlet() {
        super();
        PROJECT_ROOT_PATH = "/var/lib/tomcat7/webapps/ROOT/";
        // TODO Auto-generated constructor stub
    }

    
    
    
    
	public static String getPROJECT_ROOT_PATH() {
		if (PROJECT_ROOT_PATH == null)
			PROJECT_ROOT_PATH = "/var/lib/tomcat7/webapps/ROOT/";
		
		return PROJECT_ROOT_PATH;
	}

	public static void setPROJECT_ROOT_PATH(String pROJECT_ROOT_PATH) {
		if (PROJECT_ROOT_PATH == null)
			PROJECT_ROOT_PATH = "/var/lib/tomcat7/webapps/ROOT/";
		PROJECT_ROOT_PATH = pROJECT_ROOT_PATH;
	}
	public static String getJAVA_ROOT_PATH() {
		if (PROJECT_ROOT_PATH == null)
			PROJECT_ROOT_PATH = "/var/lib/tomcat7/webapps/ROOT/";
		return JAVA_ROOT_PATH;
	}

	public static void setJAVA_ROOT_PATH(String jAVA_ROOT_PATH) {
		if (PROJECT_ROOT_PATH == null)
			PROJECT_ROOT_PATH = "/var/lib/tomcat7/webapps/ROOT/";
		JAVA_ROOT_PATH = jAVA_ROOT_PATH;
	}

	public static String getFIC_ROOT_PATH() {
		
		if (PROJECT_ROOT_PATH == null)
			PROJECT_ROOT_PATH = "/var/lib/tomcat7/webapps/ROOT/";
		return FIC_ROOT_PATH;
	}

	public static void setFIC_ROOT_PATH(String fIC_ROOT_PATH) {
		if (PROJECT_ROOT_PATH == null)
			PROJECT_ROOT_PATH = "/var/lib/tomcat7/webapps/ROOT/";
		FIC_ROOT_PATH = fIC_ROOT_PATH;
	}

	public static String getBCHRECTEMP_FIC() {
		if (PROJECT_ROOT_PATH == null)
			PROJECT_ROOT_PATH = "/var/lib/tomcat7/webapps/ROOT/";
		return BCHRECTEMP_FIC;
	}

	public static void setBCHRECTEMP_FIC(String bCHRECTEMP_FIC) {
		if (PROJECT_ROOT_PATH == null)
			PROJECT_ROOT_PATH = "/var/lib/tomcat7/webapps/ROOT/";
		BCHRECTEMP_FIC = bCHRECTEMP_FIC;
	}
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		// If the request has either width or height attributes, the chart will be attribute-sized
		if (!(request.getAttribute("width") != null)){
			try {
				int width = Integer.parseInt((String) request.getAttribute("width"));
			}
			catch (Exception e){
				int width = DEFAULT_WIDTH;
				
			}
		}
		else {
			int width = DEFAULT_WIDTH;
		}
		if (!(request.getAttribute("height") != null)){
			try {
				int height = Integer.parseInt((String) request.getAttribute("height"));
			}
			catch (Exception e){
				int height = DEFAULT_HEIGHT;
			}
		}
		// else default sizes are applied
		else {
			int height = DEFAULT_HEIGHT;
		}
		response.setContentType("image/png");

		OutputStream outputStream = response.getOutputStream();
		
		JFreeChart chart = null;
		try {
			chart = generateChartFromTimeSeries(createDataset(parseCSVFile(new File(BCHRECTEMP_FIC))), "DS18B20", "Time", "Temperature", true);
		} catch (NumberFormatException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ChartUtilities.writeChartAsPNG(outputStream, chart , DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	
	
	/***************************************
	 * parseCSVFile reads and formats CSV 
	 * file created by BCHRECTEMP batch
	 * - IN : The CSV File to read
	 * - OUT a list of arrays of strings :
	 * {[DATETIME, SENSOR1, ...], [...], ...}
	 ***************************************/
	public List<String[]> parseCSVFile (File file) throws IOException {
		
		List<String[]> result = new ArrayList<String[]>();
		String[] lineList;
				
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		
		while ((line = reader.readLine()) != null){
			
			lineList = line.split(";", -1);
			result.add(lineList);
		}
		return result;
	}
	
	
	/**
	 * This method creates a TimeSeriesCollection from raw String values 
	 * @param data
	 * @return
	 * @throws NumberFormatException
	 * @throws ParseException
	 */
	public TimeSeriesCollection createDataset(List<String[]> data) throws NumberFormatException, ParseException {
		
		
		TimeSeriesCollection dataSet = new TimeSeriesCollection();
		

		int compteur = data.size();
		List<TimeSeries> serie = new ArrayList<TimeSeries>();
	
		// On cree autant de series qu'il y a de sondes
		for (int k = 0; k < data.get(0).length - 1 ; k++){
			serie.add(new TimeSeries("PROBE"+k));
		}
		
		/*
		 * For each line like [2015-07-21 12:34:56, 12345, 54321]
		 */
		for(int i=0 ; i < compteur ; i++){
	
			
			/*
			 * for each temperature value
			 */
			for(int j=1 ; j < data.get(i).length ; j++){
				
				// Adds [time, temperature] to the corresponding (i) serie
				serie.get(j-1).addOrUpdate(new Second((new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).parse(data.get(i)[0])), Double.parseDouble(data.get(i)[j]));
			}
		}
		
		// Adds each serie to the dataset
		for (int l = 0; l < serie.size() ; l++){
			dataSet.addSeries(serie.get(l));
		}
		
		return dataSet;
		
	}
	
	/**
	 * This method generates a Chart using a TimeSeriesCollection. Legend, title, X-Axis and Y-Axis labels can be modified
	 * @param series
	 * @param title
	 * @param xAxisLabel
	 * @param yAxisLabel
	 * @param legend
	 * @return
	 */
	public JFreeChart generateChartFromTimeSeries(TimeSeriesCollection series, String title, String xAxisLabel, String yAxisLabel, boolean legend){
		
		
		JFreeChart chart = null;
		boolean defaultTooltips = false;
		boolean defaultURLs = false;
		
		
		chart = ChartFactory.createTimeSeriesChart(title, xAxisLabel, yAxisLabel, series, legend, defaultTooltips, defaultURLs);
		
		return chart;
	}
}
