package com.gtp.hunter.process.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.common.enums.FieldType;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "documentmodelfield")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentModelField extends UUIDAuditModel implements Comparable<DocumentModelField> {

	@ManyToOne
	@JoinColumn(name = "documentmodel_id")
	@Expose(serialize = false)
	@JsonIgnore
	private DocumentModel	model;

	@Expose
	@Enumerated(EnumType.STRING)
	private FieldType		type;

	@Expose
	private String			params;

	@Expose
	@Column(name = "visible", nullable = false, columnDefinition = "BIT(1) DEFAULT 1")
	private Boolean			visible	= true;

	@Expose
	@Column(name = "ordem", nullable = false, columnDefinition = "INT(11) DEFAULT 0")
	private int				ordem;

	public DocumentModelField() {
	}

	public DocumentModelField(DocumentModel dm, String name, String metaname, FieldType type) {
		this.setModel(dm);
		this.setName(name);
		this.setMetaname(metaname);
		this.setType(type);
	}

	public DocumentModel getModel() {
		return model;
	}

	public void setModel(DocumentModel model) {
		this.model = model;
	}

	public FieldType getType() {
		return type;
	}

	public void setType(FieldType type) {
		this.type = type;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public int getOrdem() {
		return ordem;
	}

	public void setOrdem(int ordem) {
		this.ordem = ordem;
	}

	public int compareTo(DocumentModelField outroDmf) {
		if (this.getOrdem() < outroDmf.getOrdem()) {
			return -1;
		}
		if (this.getOrdem() > outroDmf.getOrdem()) {
			return 1;
		}
		return 0;
	}

	/**
	 * @return the visible
	 */
	public Boolean getVisible() {
		return visible;
	}

	/**
	 * @param visible the visible to set
	 */
	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

}
