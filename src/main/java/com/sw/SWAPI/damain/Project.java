package com.sw.SWAPI.damain;

import lombok.Data;

@Data
public class Project {

    private String project;

    private String PID;

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getPID() {
		return PID;
	}

	public void setPID(String pID) {
		PID = pID;
	}
    
    
}
