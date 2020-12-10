package com.sw.SWAPI.damain;

import lombok.Data;

/**
 * @author: liuxiaoguang
 * @Date: 2020/7/16 15:28
 * @Description: 项目信息类
 */
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
