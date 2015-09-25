$(document).ready(function () {
	
	function callGraphServlet (){

		var formDelay = 2000;
		console.log(document.getElementById("formDelay").value);

		var img = document.getElementById("JFreeGraphServlet");
		
		console.log(img.id);
		
		var url = "JFreeGraphServlet"
		$.ajax({
			url: "JFreeGraphServlet", 
			type: "GET",
			success: function( notification ) {
				console.log("JFreeGraphServlet");
				if (img.hasChildNodes()){
					console.log("img exists !")
					img.parentNode.removeChild(img);
				}
				
				var imgGraph = document.createElement("img");
				imgGraph.id ="JFreeGraphServlet";
				document.getElementById("servletGraph").appendChild(imgGraph);
				imgGraph.src = "JFreeGraphServlet";
			},
			error: function(data){
				//handle any error 
			}
		});
		
	}
	
	function callTemperatureServlet (){
		
		var txt = document.getElementById("probeTemperaturesTab");
		if (txt.hasChildNodes()){
			txt.parentNode.removeChild(txt);
		}
		$.ajax({
			url: "TemperatureServlet", 
			type: "GET",
			success: function( notification ) {
				console.log("TemperatureServlet");

				var txt = document.createElement("table");
				txt.id= "probeTemperaturesTab";
				document.getElementById("probeTemperatures").appendChild(txt);
			},
			error: function(data){
				console.log("Error : "+data); 
			}
		});
	}
	
	function callServlet () {
		callTemperatureServlet();
	}	
	
	setInterval(callServlet,2000);

});