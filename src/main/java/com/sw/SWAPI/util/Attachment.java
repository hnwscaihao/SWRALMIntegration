package com.sw.SWAPI.util;

/**
 * @author: liuxiaoguang
 * @Date: 2020/7/16 15:28
 * @Description: 附件信息
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
