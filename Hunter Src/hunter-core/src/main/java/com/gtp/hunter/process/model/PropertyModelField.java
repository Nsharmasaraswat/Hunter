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
@Table(name = "propertymodelfield")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PropertyModelField extends UUIDAuditModel {

	@ManyToOne
	@JoinColumn(name = "propertymodel_id")
	@Expose(serialize = false)
	@JsonIgnore
	private PropertyModel	model;

	@Enumerated(EnumType.STRING)
	@Expose()
	private FieldType		type;

	@Expose
	@Column(name = "visible", nullable = false, columnDefinition = "BIT(1) DEFAULT 1")
	private Boolean			visible	= true;

	@Expose
	@Column(name = "ordem", nullable = false, columnDefinition = "INT(11) DEFAULT 0")
	private int				ordem;

	public PropertyModelField() {
	}

	public PropertyModelField(String name, PropertyModel model, FieldType type) {
		this.setName(name);
		this.setModel(model);
		this.setType(type);
	}

	public PropertyModelField(String name, PropertyModel model, FieldType type, String metaname) {
		this.setName(name);
		this.setModel(model);
		this.setType(type);
		this.setMetaname(metaname);
	}

	public PropertyModel getModel() {
		return model;
	}

	public void setModel(PropertyModel model) {
		this.model = model;
	}

	public FieldType getType() {
		return type;
	}

	public void setType(FieldType type) {
		this.type = type;
	}

	public Boolean getVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	/**
	 * @return the ordem
	 */
	public int getOrdem() {
		return ordem;
	}

	/**
	 * @param ordem the ordem to set
	 */
	public void setOrdem(int ordem) {
		this.ordem = ordem;
	}

}
