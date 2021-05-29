package com.gtp.hunter.process.service;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.core.service.AuthService;
import com.gtp.hunter.core.service.CredentialService;
import com.gtp.hunter.core.service.DeviceService;
import com.gtp.hunter.core.service.GroupService;
import com.gtp.hunter.core.service.PrefixService;
import com.gtp.hunter.core.service.RawDataService;
import com.gtp.hunter.core.service.SourceService;
import com.gtp.hunter.core.service.UnitService;
import com.gtp.hunter.core.service.UserService;
import com.gtp.hunter.process.service.solar.AGLConvertService;
import com.gtp.hunter.process.service.solar.WMSService;

@Stateless
public class RegisterService {

	@EJB(lookup = "java:global/hunter-core/AuthService!com.gtp.hunter.core.service.AuthService")
	private AuthService					authSvc;

	@EJB(lookup = "java:global/hunter-core/CredentialService!com.gtp.hunter.core.service.CredentialService")
	private CredentialService			crdSvc;

	@EJB(lookup = "java:global/hunter-core/DeviceService!com.gtp.hunter.core.service.DeviceService")
	private DeviceService				devSvc;

	@EJB(lookup = "java:global/hunter-core/GroupService!com.gtp.hunter.core.service.GroupService")
	private GroupService				grpSvc;

	@EJB(lookup = "java:global/hunter-core/PrefixService!com.gtp.hunter.core.service.PrefixService")
	private PrefixService				pfxSvc;

	@EJB(lookup = "java:global/hunter-core/RawDataService!com.gtp.hunter.core.service.RawDataService")
	private RawDataService				rdSvc;

	@EJB(lookup = "java:global/hunter-core/SourceService!com.gtp.hunter.core.service.SourceService")
	private SourceService				srcSvc;

	@EJB(lookup = "java:global/hunter-core/UserService!com.gtp.hunter.core.service.UserService")
	private UserService					usrSvc;

	@EJB(lookup = "java:global/hunter-core/UnitService!com.gtp.hunter.core.service.UnitService")
	private UnitService					uSvc;

	@Inject
	private ActionService				actSvc;

	@Inject
	private AddressService				addSvc;

	@Inject
	private AddressModelService			admSvc;

	@Inject
	private AddressModelFieldService	amfSvc;

	@Inject
	private AddressFieldService			adfSvc;

	@Inject
	private AGLConvertService			aglSvc;

	@Inject
	private AlertService				alertSvc;

	@Inject
	private DashboardService			dshSvc;

	@Inject
	private DashboardWidgetService		dswSvc;

	@Inject
	private DocumentService				dcSvc;

	@Inject
	private DocumentFieldService		dfSvc;

	@Inject
	private DocumentItemService			diSvc;

	@Inject
	private DocumentModelFieldService	dmfSvc;

	@Inject
	private DocumentModelService		dmSvc;

	@Inject
	private DocumentTransportService	dtrSvc;

	@Inject
	private DocumentThingService		dtSvc;

	@Inject
	private LocationService				locSvc;

	@Inject
	private OriginService				orgSvc;

	@Inject
	private ProductFieldService			pfSvc;

	@Inject
	private ProductModelFieldService	pmfSvc;

	@Inject
	private ProductModelService			pmSvc;

	@Inject
	private ProcessService				prcSvc;

	@Inject
	private ProductService				prdSvc;

	@Inject
	private PropertyModelService		prmSvc;

	@Inject
	private PropertyModelFieldService	prmfSvc;

	@Inject
	private PropertyService				prSvc;

	@Inject
	private PersonFieldService			psfSvc;

	@Inject
	private PersonModelFieldService		psmfSvc;

	@Inject
	private PersonModelService			psmSvc;

	@Inject
	private PersonService				psSvc;

	@Inject
	private ThingService				thSvc;

	@Inject
	private TaskService					tskSvc;

	@Inject
	private CustomService					uiSvc;

	@Inject
	private WidgetService				wdgSvc;

	@Inject
	private WMSService					wmsSvc;

	public ActionService getActSvc() {
		return actSvc;
	}

	public CredentialService getCrdSvc() {
		return crdSvc;
	}

	public AddressService getAddSvc() {
		return addSvc;
	}

	public AGLConvertService getAglSvc() {
		return aglSvc;
	}

	public AlertService getAlertSvc() {
		return alertSvc;
	}

	public AuthService getAuthSvc() {
		return authSvc;
	}

	public DocumentService getDcSvc() {
		return dcSvc;
	}

	public DeviceService getDevSvc() {
		return devSvc;
	}

	public DocumentFieldService getDfSvc() {
		return dfSvc;
	}

	public DocumentItemService getDiSvc() {
		return diSvc;
	}

	public DocumentModelFieldService getDmfSvc() {
		return dmfSvc;
	}

	public DocumentModelService getDmSvc() {
		return dmSvc;
	}

	public DocumentTransportService getDtrSvc() {
		return dtrSvc;
	}

	public DocumentThingService getDtSvc() {
		return dtSvc;
	}

	public DashboardService getDshSvc() {
		return dshSvc;
	}

	public DashboardWidgetService getDswSvc() {
		return dswSvc;
	}

	public GroupService getGrpSvc() {
		return grpSvc;
	}

	public LocationService getLocSvc() {
		return locSvc;
	}

	public OriginService getOrgSvc() {
		return orgSvc;
	}

	public ProductFieldService getPfSvc() {
		return pfSvc;
	}

	public PrefixService getPfxSvc() {
		return pfxSvc;
	}

	public ProductModelFieldService getPmfSvc() {
		return pmfSvc;
	}

	public ProductModelService getPmSvc() {
		return pmSvc;
	}

	public ProcessService getPrcSvc() {
		return prcSvc;
	}

	public ProductService getPrdSvc() {
		return prdSvc;
	}

	public PropertyModelFieldService getPrmfSvc() {
		return prmfSvc;
	}

	public PropertyService getPrSvc() {
		return prSvc;
	}

	public PersonFieldService getPsfSvc() {
		return psfSvc;
	}

	public PersonModelFieldService getPsmfSvc() {
		return psmfSvc;
	}

	public PersonModelService getPsmSvc() {
		return psmSvc;
	}

	public PersonService getPsSvc() {
		return psSvc;
	}

	public SourceService getSrcSvc() {
		return srcSvc;
	}

	public ThingService getThSvc() {
		return thSvc;
	}

	public TaskService getTskSvc() {
		return tskSvc;
	}

	public CustomService getCustomSvc() {
		return uiSvc;
	}

	public CustomService getUISvc() {
		return uiSvc;
	}

	public UnitService getUnSvc() {
		return uSvc;
	}

	public UserService getUsrSvc() {
		return usrSvc;
	}

	public WidgetService getWdgSvc() {
		return wdgSvc;
	}

	public WMSService getWmsSvc() {
		return wmsSvc;
	}

	public void setAuthSvc(AuthService authSvc) {
		this.authSvc = authSvc;
	}

	public PropertyModelService getPrmSvc() {
		return prmSvc;
	}

	public AddressModelService getAdmSvc() {
		return admSvc;
	}

	public AddressModelFieldService getAmfSvc() {
		return amfSvc;
	}

	public AddressFieldService getAdfSvc() {
		return adfSvc;
	}

	public RawDataService getRdSvc() {
		return rdSvc;
	}

}
