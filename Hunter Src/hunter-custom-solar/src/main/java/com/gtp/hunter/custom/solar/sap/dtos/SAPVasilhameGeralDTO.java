package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SAPVasilhameGeralDTO {

	@Expose
	@SerializedName("MATNR_ORIG")
	private String	matnr_orig;

	@Expose
	@SerializedName("MATNR_SWITCH")
	private String	matnr_switch;

	/**
	 * @return the matnr_orig
	 */
	public String getMatnr_orig() {
		return matnr_orig;
	}

	/**
	 * @param matnr_orig the matnr_orig to set
	 */
	public void setMatnr_orig(String matnr_orig) {
		this.matnr_orig = matnr_orig;
	}

	/**
	 * @return the matnr_switch
	 */
	public String getMatnr_switch() {
		return matnr_switch;
	}

	/**
	 * @param matnr_switch the matnr_switch to set
	 */
	public void setMatnr_switch(String matnr_switch) {
		this.matnr_switch = matnr_switch;
	}

	@Override
	public String toString() {
		return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
	}
}
