package net.brewspberry.ws.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.bind.annotation.XmlElement;

import net.brewspberry.beans.Temperature;
import net.brewspberry.ws.TemperatureWS;

@WebService(endpointInterface="net.brewspberry.ws.impl.TemperatureWSImpl")
public class TemperatureWSImpl implements TemperatureWS {

	private String csvPath = "/home/pi/brewhouse/fic/ds18b20_raw_measurements.csv";

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSSSS");
	private Temperature last = new Temperature();

	@WebMethod
	public List<Temperature> getAllLastTemperature() {

		String lastTemperature = returnLastLineFromCSV();
		String[] lastTemperatureArray = lastTemperature.split(";");
		
		List<Temperature> lastTemperatureList = new ArrayList<Temperature>();

		int i = 1;
		int max = lastTemperatureArray.length - 1;
		System.out.println("Max "+max);
		if (max > 0) {
			
			for (i = 1; i <= max; i++) {
	
				if (last != null) {
					System.out.println("i "+i);

					try {
						System.out.println(lastTemperatureArray[0]);
						last.setDate(sdf.parse(lastTemperatureArray[0]));
						System.out.println(lastTemperatureArray[i]);
						
						last.setTemperature(Double.parseDouble(lastTemperatureArray[i]));
						last.setProbe("PROBE" + (i-1));
	
					} catch (ParseException e) {
						e.printStackTrace();
					}
	
				} else {
					last.setDate(Calendar.getInstance().getTime());
	
					last.setTemperature(0.0);
					last.setProbe("PROBE-1");
				}
				
				lastTemperatureList.add(last);
			}
		}
		else {
			last.setDate(Calendar.getInstance().getTime());
			
			last.setTemperature(0.0);
			last.setProbe("PROBE-1");
			lastTemperatureList.add(last);
		}
		return lastTemperatureList;
	}

	@WebMethod
	public Temperature getLastTemperature(@XmlElement(required=true) Integer probe) {

		String lastTemperature = returnLastLineFromCSV();
		String[] lastTemperatureArray = lastTemperature.split(";");
		System.out.println(lastTemperatureArray[0] +" "+lastTemperatureArray[1]);

		if (last != null) {

			try {
				last.setDate(sdf.parse(lastTemperatureArray[0]));

				if (probe<lastTemperatureArray.length){
					last.setTemperature(Double.parseDouble(lastTemperatureArray[probe+1]));
					last.setProbe("PROBE" + probe);
			}

			} catch (ParseException e) {
				e.printStackTrace();
			}

		} else {
			last.setDate(Calendar.getInstance().getTime());

			last.setTemperature(0.0);
			last.setProbe("PROBE-1");
		}

		return last;

	}

	@SuppressWarnings("resource")
	public String returnLastLineFromCSV() {

		BufferedReader br = null;
		String lastLine = "";

		try {
			br = new BufferedReader(new FileReader(new File(csvPath)));

			String line;
			while ((line = br.readLine()) != null) {
				lastLine = line;
			}

			return lastLine;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "ERROR OPENING FILE";

		}

	}
}
