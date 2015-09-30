$(window).ready(function () {
	
	function callGraphServlet (){

		var formDelay = 2000;
		console.log(document.getElementById("formDelay").value);

		var img = document.getElementById("JFreeGraphServlet");
		var div = document.getElementById("servletGraph");
		
		console.log(img.id);
		
		var url = "JFreeGraphServlet"
		$.ajax({
			url: "JFreeGraphServlet", 
			type: "GET",
			success: function( notification ) {
				console.log("JFreeGraphServlet");
				if (typeof img !== "undefined"){
					console.log("img exists !")
					div.removeChild(div.childNodes[0]);
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
		callGraphServlet();
	}	
	
	setInterval(callServlet,2000);

});