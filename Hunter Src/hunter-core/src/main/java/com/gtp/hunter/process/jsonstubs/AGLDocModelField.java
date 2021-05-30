package com.gtp.hunter.process.jsonstubs;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AGLDocModelField implements Comparable<AGLDocModelField> {

	@Expose
	private String						attrib;
	@Expose
	private String						name;
	@Expose
	private String						type;
	@Expose
	private String						value;
	@Expose
	private int							ordem;
	@Expose
	private List<AGLDocModelComboItem>	options;
	@Expose(serialize = false)
	private boolean						lido	= false;

	public String getAttrib() {
		return attrib;
	}

	public void setAttrib(String attrib) {
		this.attrib = attrib;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<AGLDocModelComboItem> getOptions() {
		return options;
	}

	public void setOptions(List<AGLDocModelComboItem> options) {
		this.options = options;
	}

	public boolean isLido() {
		return lido;
	}

	public void setLido(boolean lido) {
		this.lido = lido;
	}

	public int getOrdem() {
		return ordem;
	}

	public void setOrdem(int ordem) {
		this.ordem = ordem;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int compareTo(AGLDocModelField outroDmf) {
		if (this.getOrdem() < outroDmf.getOrdem()) {
			return -1;
		}
		if (this.getOrdem() > outroDmf.getOrdem()) {
			return 1;
		}
		return 0;
	}
}
