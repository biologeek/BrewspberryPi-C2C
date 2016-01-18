package net.brewspberry.front;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.brewspberry.util.ConfigLoader;
import net.brewspberry.util.Constants;

/**
 * Servlet implementation class TemperatureServlet
 */
public class TemperatureServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	
	public static String _BCHRECTEMP_FIC_ = ConfigLoader.getConfigByKey(Constants.CONFIG_PROPERTIES, "files.measurements.temperature");
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TemperatureServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String typeOfOutput = "";
		String getIt = "";
		
		String resp = "";
		String[] res = parseCSVFile(new File(_BCHRECTEMP_FIC_));
		for (int i = 5 ;i < res.length ; i++){
			resp = resp+" "+res[i];
		}
		
		
		if (request.getParameter("type") != null){
			typeOfOutput = request.getParameter("type");
		
			
			switch (typeOfOutput) {
			
			case "html" :
				
				getIt = generateHTML(res);
				break;
				
			case "txt" : 
				getIt = resp;
				break;
				
			default :
				getIt = resp;
				break;
			}
		} else {
			getIt = resp;
		}
		
		System.out.println("Fichier : "+_BCHRECTEMP_FIC_);
		PrintWriter output = response.getWriter();
		output.write(getIt);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	public String[] parseCSVFile (File file) throws IOException {
		
		String[] last;
		String line, lastLine = null;
				
		BufferedReader reader = new BufferedReader(new FileReader(file));
		

	    while ((line = reader.readLine()) != null) {
	    	lastLine = line;
	    }
	    last = lastLine.split(";");

	    for (int j = 4; j< last.length ; j+=2) {
	    	float nbr = Float.parseFloat(last[j+1]);
	    	
	    	last[j] = Float.toString(nbr);
	    }
		return last;
	}
	
	String generateHTML (String[] data) {
		String htmlCode = "<tr>";
		for (int i = 1;i < data.length;i++) {
			htmlCode = htmlCode+"<td>PROBE"+ i+ " : "+data[i]+"</td>";
		}
		htmlCode = htmlCode+"</tr>";
		return htmlCode;
	}
	
	
}
