package com.gtp.hunter.process.jsonstubs;

import java.util.List;

import com.google.gson.annotations.Expose;

public class WMSRule {

	@Expose
	private int				id;

	@Expose
	private String			name;

	@Expose
	private List<String>	conds;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getConds() {
		return conds;
	}

	public void setConds(List<String> conds) {
		this.conds = conds;
	}
}
