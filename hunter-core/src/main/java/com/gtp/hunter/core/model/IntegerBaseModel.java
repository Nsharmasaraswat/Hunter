package com.gtp.hunter.core.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class IntegerBaseModel extends BaseModel<Integer> {
	@Id
	@Column(name = "id", nullable = false)
	private Integer id;

	@Override
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

}
