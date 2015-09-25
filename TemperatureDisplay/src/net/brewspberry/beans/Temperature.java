package net.brewspberry.beans;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class Temperature {


	private Date date;
	private Double temperature;
	private String probe;
	public Temperature() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public String toString() {
		return "Temperature [date=" + date + ", temperature=" + temperature
				+ ", probe=" + probe + "]";
	}
	
	
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Double getTemperature() {
		return temperature;
	}
	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}
	public String getProbe() {
		return probe;
	}
	public void setProbe(String probe) {
		this.probe = probe;
	}

}
