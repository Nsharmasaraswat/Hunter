package com.gtp.hunter.process.jsonstubs;

import java.util.Date;

import com.google.gson.annotations.Expose;

public class StockDueDateJson {

	@Expose
	private String	sku;

	@Expose
	private String	name;

	@Expose
	private String	status;

	@Expose
	private String	addr;

	@Expose
	private Date	man;

	@Expose
	private Date	exp;

	@Expose
	private int		fab;

	@Expose
	private int		due;

	@Expose
	private int		count;

	/**
	 * @return the sku
	 */
	public String getSku() {
		return sku;
	}

	/**
	 * @param sku the sku to set
	 */
	public void setSku(String sku) {
		this.sku = sku;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the addr
	 */
	public String getAddr() {
		return addr;
	}

	/**
	 * @param addr the addr to set
	 */
	public void setAddr(String addr) {
		this.addr = addr;
	}

	/**
	 * @return the man
	 */
	public Date getMan() {
		return man;
	}

	/**
	 * @param man the man to set
	 */
	public void setMan(Date man) {
		this.man = man;
	}

	/**
	 * @return the exp
	 */
	public Date getExp() {
		return exp;
	}

	/**
	 * @param exp the exp to set
	 */
	public void setExp(Date exp) {
		this.exp = exp;
	}

	/**
	 * @return the fab
	 */
	public int getFab() {
		return fab;
	}

	/**
	 * @param fab the fab to set
	 */
	public void setFab(int fab) {
		this.fab = fab;
	}

	/**
	 * @return the due
	 */
	public int getDue() {
		return due;
	}

	/**
	 * @param due the due to set
	 */
	public void setDue(int due) {
		this.due = due;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}
}
