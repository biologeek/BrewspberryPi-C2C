package net.brewspberry.front;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * Software :JFreeGraphServlet is a software used to get current Temperature for
 * one or more probes
 * 
 * ******************************************************************************************************
 * Version 		* 	Date 	* 		Author		*				 Comment 								*
 * ****************************************************************************************************** 
 * 	1.0 		* 20150810 	*		Biologeek 	* 					Init 								*
 * ---------------------------------------------------------------------------------------------------- *
 * ******************************************************************************************************
 **/
public class TemperatureServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(TemperatureServlet.class.getName());

	public static String _BCHRECTEMP_FIC_ = "/home/xavier/fic/ds18b20_raw_measurements.csv";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public TemperatureServlet() {
		super();
		logger.setLevel(Level.FINEST);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String[] temp = parseCSVFile(new File(_BCHRECTEMP_FIC_));
		String getIt = generateHTML(temp);

		logger.info("Current temperature : " + getIt);
		PrintWriter output = response.getWriter();
		output.write(getIt);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	public String[] parseCSVFile(File file) throws IOException {

		String[] last;
		String line, lastLine = null;

		BufferedReader reader = new BufferedReader(new FileReader(file));

		while ((line = reader.readLine()) != null) {
			lastLine = line;
		}
		last = lastLine.split(";");

		for (int j = 1; j < last.length; j++) {
			float nbr = Float.parseFloat(last[j]);
			logger.log(Level.CONFIG, "parsing float : " + nbr);

			last[j] = Float.toString(nbr);
		}
		return last;
	}

	String generateHTML(String[] data) {

		if (data == null) {
			return "<tr><td>PROBE</td></tr>";
		} else {

			if (data.length == 0) {
				return "<tr><td>PROBE</td></tr>";
			} else {
				String htmlCode = "<tr>";
				for (int i = 1; i < data.length; i++) {
					htmlCode = htmlCode + "<td>PROBE" + i + " : " + data[i]
							+ "</td>";
				}
				htmlCode = htmlCode + "</tr>";
				return htmlCode;
			}
		}
	}

}
