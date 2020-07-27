package com.sw.SWAPI.util;
/**
 * 附件PO对象
 * @author WangWei
 * @time 2018年1月9日 下午1:55:18
 */
public class Attachment {
	private String name;//name
	private String path;//路径
	public Attachment() {
	}
	public Attachment(String name, String path) {
		this.name = name;
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	@Override
	public String toString() {
		return "Attachment [name=" + name + ", path=" + path + "]";
	}
}
