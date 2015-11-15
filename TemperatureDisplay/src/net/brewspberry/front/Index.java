package net.brewspberry.front;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Index
 */

	
/*
 * 
 */
@WebServlet("/Index")
public class Index extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private String pythonScriptDirectory = "/home/xavier/ownCloud/Projets/Brewhouse/Code/batches/";
	private String pythonScriptFile = "bchrectemp.py";
	private String csvTemperatureFile = "/home/xavier/ownCloud/Projets/Brewhouse/Code/fic/ds18b20_raw_measurements.csv";
	
    /**
     * Default constructor. 
     */
    public Index() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			Process p = Runtime.getRuntime().exec("python "+pythonScriptDirectory+pythonScriptFile);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
		request.setAttribute("csvList", this.parseCSVFile(new File(csvTemperatureFile)));
		
		request.setAttribute("JSList", this.buildJSTable(request));
		request.getRequestDispatcher("index.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
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
		
		// Ne lit pas bien la dernière ligne
		while ((line = reader.readLine()) != null){
			
			lineList = line.split(";", -1);
			System.out.println(Arrays.toString(lineList));
			result.add(lineList);
		}
		return result;
	}
	
	/**
	 * buildJSTable creates a string formatted for JS script
	 * using a list of arrays
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String buildJSTable (HttpServletRequest request) {
		
		
		int compteur = ((List<String[]>) request.getAttribute("csvList")).size();
		List<String[]> dataTable = (List<String[]>) request.getAttribute("csvList");
		String js = "[";
		
		for(int i=0;i < compteur;i++){
			js += "[";
	
			for(int j=0;j < ((List<String[]>) request.getAttribute("csvList")).get(0).length ; j++){
		
				if (i== 0 && j == 0)
					js += "'DATETIME',";
				else if (i== 0 && j !=0)
					js += "'SENSOR"+j+"',";
				else if (j==0)
					js += "'"+dataTable.get(i-1)[j]+"',";
				else
					js += ""+dataTable.get(i-1)[j]+",";
			}
			
			if (js.substring(js.length()-1).equals(","))	
				js = js.substring(0, js.length()-1);
	
			js += "],";	
		}
			
		if (js.substring(js.length()-1).equals(","))	
			js = js.substring(0, js.length()-1);
		js += "]";
		return js;
	}

}
