package com.gtp.hunter.ui.json;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.Thing;

public class PalletHistory {

	@Expose
	private Thing			thing;

	@Expose
	private List<Document>	parents;

	/**
	 * @return the thing
	 */
	public Thing getThing() {
		return thing;
	}

	/**
	 * @param thing the thing to set
	 */
	public void setThing(Thing thing) {
		this.thing = thing;
	}

	/**
	 * @return the parents
	 */
	public List<Document> getParents() {
		return parents;
	}

	/**
	 * @param parents the parents to set
	 */
	public void setParents(List<Document> parents) {
		this.parents = parents;
	}
}
