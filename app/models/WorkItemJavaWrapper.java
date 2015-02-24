package models;

public class WorkItemJavaWrapper {
	
	private String description;
	private String date;
	private String hours;
    private String startTime;
    private String endTime;
    private String breakTime;
	
	public WorkItemJavaWrapper(String description, String date, String hours, String startTime, String endTime, String breakTime) {
		this.description = description;
		this.date = date;
		this.hours = hours;
        this.startTime = startTime;
        this.endTime = endTime;
        this.breakTime = breakTime;
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

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getBreakTime() {
        return breakTime;
    }
}
