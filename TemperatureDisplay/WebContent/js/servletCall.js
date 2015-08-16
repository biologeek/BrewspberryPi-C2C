$(document).ready(function () {
	
	function callGraphServlet (){

		var formDelay = 2000;
		console.log(document.getElementById("formDelay").value);

		var img = document.getElementById("JFreeGraphServlet");
		
		console.log(img.id);
		
		var url = "JFreeGraphServlet?d="+formDelay
		$.ajax({
			url: "JFreeGraphServlet", 
			type: "GET",
			success: function( notification ) {
				console.log("JFreeGraphServlet?r="+Math.random());

				img.parentNode.removeChild(img);
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
		$.ajax({
			url: "TemperatureServlet", 
			type: "GET",
			success: function( notification ) {
				console.log("TemperatureServlet?r="+Math.random());

				txt.parentNode.removeChild(txt);
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
		callGraphServlet();
	}	
	
	setInterval(callServlet,1000);

});