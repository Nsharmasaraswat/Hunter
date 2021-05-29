package com.gtp.hunter.process.jsonstubs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AGLDocModelForm extends AGLBase {

	@Expose
	private String						id;
	@Expose
	private String						name;
	@Expose
	private String						metaname;
	@Expose
	private String						status;
	@Expose
	private String						user_id;
	@Expose
	private String						code;
	@Expose
	private String						parent_id;
	@Expose
	private String						person_id;
	@Expose
	private SortedSet<AGLDocModelField>	model		= new ConcurrentSkipListSet<AGLDocModelField>();
	@Expose
	private List<AGLDocModelItem>		items		= new CopyOnWriteArrayList<AGLDocModelItem>();
	@Expose
	private List<AGLDocModelForm>		siblings	= new CopyOnWriteArrayList<AGLDocModelForm>();
	@Expose
	private Map<String, String>			props		= new HashMap<String, String>();
	@Expose
	private Set<AGLThing>				things		= new ConcurrentSkipListSet<AGLThing>();
	@Expose
	private AGLDocModelResources		resources	= new AGLDocModelResources();
	@Expose
	private Set<AGLDocTransport>		transport	= new ConcurrentSkipListSet<AGLDocTransport>();
	@Expose
	private Set<AGLAddressProps>		addresses	= new ConcurrentSkipListSet<AGLAddressProps>();

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getParent_id() {
		return parent_id;
	}

	public void setParent_id(String parent_id) {
		this.parent_id = parent_id;
	}

	public List<AGLDocModelItem> getItems() {
		return items;
	}

	public void setItems(List<AGLDocModelItem> items) {
		this.items = items;
	}

	public List<AGLDocModelForm> getSiblings() {
		return siblings;
	}

	public void setSiblings(List<AGLDocModelForm> siblings) {
		this.siblings = siblings;
	}

	public SortedSet<AGLDocModelField> getModel() {
		return model;
	}

	public void setModel(SortedSet<AGLDocModelField> model) {
		this.model = model;
	}

	public Map<String, String> getProps() {
		return props;
	}

	public void setProps(Map<String, String> props) {
		this.props = props;
	}

	public AGLDocModelResources getResources() {
		return resources;
	}

	public void setResources(AGLDocModelResources resources) {
		this.resources = resources;
	}

	public Set<AGLThing> getThings() {
		return things;
	}

	public void setThings(Set<AGLThing> things) {
		this.things = things;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
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
	 * @return the metaname
	 */
	public String getMetaname() {
		return metaname;
	}

	/**
	 * @param metaname the metaname to set
	 */
	public void setMetaname(String metaname) {
		this.metaname = metaname;
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
	 * @return the transport
	 */
	public Set<AGLDocTransport> getTransport() {
		return transport;
	}

	/**
	 * @param transport the transport to set
	 */
	public void setTransport(Set<AGLDocTransport> transport) {
		this.transport = transport;
	}

	/**
	 * @return the addresses
	 */
	public Set<AGLAddressProps> getAddresses() {
		return addresses;
	}

	/**
	 * @param addresses the addresses to set
	 */
	public void setAddresses(Set<AGLAddressProps> addresses) {
		this.addresses = addresses;
	}

	public String getPerson_id() {
		return person_id;
	}

	public void setPerson_id(String person_id) {
		this.person_id = person_id;
	}
}
