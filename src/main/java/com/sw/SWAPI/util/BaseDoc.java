package com.sw.SWAPI.util;

import java.util.Map;

/**
 * 导入Word的PO对象
 * @author WangWei
 * @time 2018年1月9日 下午1:54:03
 */
public class BaseDoc {
	private String name;
	private String summary;
	private String section;
	private String id;
	private String reImportID;
	private String category;
	private String state;
	private Map<String, String> img;
	private Integer level;
	private String parentId;
	private String allowEdit;
	
	
	public String getAllowEdit() {
		return allowEdit;
	}

	public void setAllowEdit(String allowEdit) {
		this.allowEdit = allowEdit;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public BaseDoc() {
	}

	public BaseDoc(String name, String summary, String section, String id, String reImportID, String category,
			String state, Map<String, String> img) {
		this.name = name;
		this.summary = summary;
		this.section = section;
		this.id = id;
		this.reImportID = reImportID;
		this.category = category;
		this.state = state;
		this.img = img;
	}
	

	public String getCategory() {
		return category;
	}


	public void setCategory(String category) {
		this.category = category;
	}


	public Map<String, String> getImg() {
		return img;
	}

	
	public String getReImportID() {
		return reImportID;
	}

	public void setReImportID(String reImportID) {
		this.reImportID = reImportID;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setImg(Map<String, String> img) {
		this.img = img;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}



	@Override
	public String toString() {
		return "BaseDoc [name=" + name + ", summary=" + summary + ", section=" + section + ", id=" + id
				+ ", reImportID=" + reImportID + ", category=" + category + ", state=" + state + ", img=" + img
				+ ", level=" + level + ", parentId=" + parentId + ", allowEdit=" + allowEdit + "]";
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	
}
