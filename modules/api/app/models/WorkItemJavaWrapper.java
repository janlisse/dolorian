package models;

public class WorkItemJavaWrapper {
	
	private String description;
	private String date;
	private String hours;
	
	public WorkItemJavaWrapper(String description, String date, String hours) {
		this.description = description;
		this.date = date;
		this.hours = hours;
	}

	public String getDescription() {
		return description;
	}

	public String getDate() {
		return date;
	}

	public String getHours() {
		return hours;
	}
	
	
	
}
