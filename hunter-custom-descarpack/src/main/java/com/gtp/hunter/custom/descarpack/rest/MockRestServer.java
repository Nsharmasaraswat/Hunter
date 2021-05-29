package com.gtp.hunter.custom.descarpack.rest;

import java.io.StringReader;
import java.util.Date;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.core.model.CredentialPassword;
import com.gtp.hunter.core.model.Group;
import com.gtp.hunter.core.model.Permission;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.custom.descarpack.repository.CustomRepository;
import com.gtp.hunter.custom.descarpack.service.IntegrationService;
import com.gtp.hunter.custom.descarpack.stream.IntegrationLogStreamManager;
import com.gtp.hunter.custom.descarpack.timer.SupplierTagCountingScheduler;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.ejbcommon.util.RestUtil;
import com.gtp.hunter.process.model.IntegrationLog;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.PropertyModel;
import com.gtp.hunter.process.model.Thing;

import io.reactivex.subjects.PublishSubject;

@Path("/mock")
@Startup
public class MockRestServer {

	private static final String				BASE_URL	= "http://187.94.62.199:36045/rest";
	private static final String				WS_METHOD	= "PRINT";
	private static final RestUtil			rest		= new RestUtil(BASE_URL);

	@Inject
	private SupplierTagCountingScheduler	stcs;

	@Inject
	private IntegrationLogStreamManager		ilsm;

	@Inject
	private IntegrationService				iSvc;

	@Inject
	private transient Logger				logger;

	@Inject
	private CustomRepository				rep;

	private PublishSubject<IntegrationLog>	obs;

	@PostConstruct
	public void init() {
		obs = ilsm.getStream();
	}

	@GET
	@Path("/enviaemail")
	@Produces(MediaType.TEXT_HTML)
	@PermitAll
	public String enviaemail() {
		stcs.sendMail();
		return "ENVIADO";
	}

	@GET
	@POST
	@PUT
	@DELETE
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public IntegrationReturn mockMethod(@Context HttpServletRequest req) {
		int millis = (new Double(Math.random() * 110).intValue() + 10) * 1000;

		logger.debug("MOCKMETHOD CHAMADO - " + millis);

		IntegrationLog il = new IntegrationLog();
		il.setAddress(req.getRemoteHost());
		il.setMethodtype(req.getMethod());
		il.setCreatedAt(new Date());
		il.setTimeconsumed(millis);

		obs.onNext(il);

		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return IntegrationReturn.OK;
	}

	@POST
	@Path("/testpasdfdsasdfasdfasdfdsarint")
	@PermitAll
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public IntegrationReturn getPrintRest(JsonObject json) {
		logger.info(json.toString());
		return IntegrationReturn.OK;
	}

	@GET
	@Path("/fdsafdsafdsa")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public IntegrationReturn updateProducts() {

		return IntegrationReturn.OK;
	}

	@GET
	@Path("/asfdasdfadsf")
	public String cadastraUsuariosEUnit() {

		//Unit crd1 = new Unit("", UnitType.EPC96);

		Unit crd01 = new Unit("E2801160600002053095DADD", UnitType.EPC96);
		Unit crd02 = new Unit("E2801160600002053097EBE4", UnitType.EPC96);
		Unit crd03 = new Unit("E28011606000020530983B04", UnitType.EPC96);
		Unit crd04 = new Unit("E2801160600002053097FA3E", UnitType.EPC96);
		Unit crd05 = new Unit("E2801160600002090761C8C5", UnitType.EPC96);
		Unit crd06 = new Unit("E28011606000020907628806", UnitType.EPC96);
		Unit crd07 = new Unit("E28011606000020530985D55", UnitType.EPC96);
		Unit crd08 = new Unit("E28011606000020530980544", UnitType.EPC96);
		Unit crd09 = new Unit("E28011606000020530983B74", UnitType.EPC96);
		Unit crd10 = new Unit("E28011606000020530973D54", UnitType.EPC96);
		Unit crd11 = new Unit("E2801160600002053096B2DE", UnitType.EPC96);
		Unit crd12 = new Unit("E280116060000205309618DD", UnitType.EPC96);
		Unit crd13 = new Unit("E2801160600002053095DAEE", UnitType.EPC96);
		Unit crd14 = new Unit("E280116060000205309794AB", UnitType.EPC96);
		Unit crd15 = new Unit("E28011606000020530978B44", UnitType.EPC96);
		Unit crd16 = new Unit("E2801160600002053096B2ED", UnitType.EPC96);
		Unit crd17 = new Unit("E280116060000205309594DD", UnitType.EPC96);
		Unit crd18 = new Unit("E280116060000205309618EE", UnitType.EPC96);
		Unit crd19 = new Unit("E2801160600002090761C8A5", UnitType.EPC96);
		Unit crd20 = new Unit("E280116060000205309580DE", UnitType.EPC96);
		Unit crd21 = new Unit("E280116060000205309594EE", UnitType.EPC96);
		Unit crd22 = new Unit("E2801160600002090761FCD5", UnitType.EPC96);
		Unit crd23 = new Unit("E28011606000020907618CD5", UnitType.EPC96);
		Unit crd24 = new Unit("E28011606000020907618A45", UnitType.EPC96);
		Unit crd25 = new Unit("E28011606000020907618C45", UnitType.EPC96);

		iSvc.getrSvc().getuRep().multiPersist(crd01, crd02, crd03, crd04, crd05, crd06, crd07, crd08, crd09, crd10, crd11, crd12, crd13, crd14, crd15, crd16, crd17, crd18, crd19, crd20, crd21, crd22, crd23, crd24, crd25);

		Product p = iSvc.getrSvc().getPdRep().findByMetaname("CRACHA");

		PropertyModel pm = iSvc.getrSvc().getPpmRep().findByMetaname("AUTH");

		Thing t01 = new Thing("CRACHA01", p, pm, "NOVO");
		t01.getUnits().add(crd01.getId());
		Thing t02 = new Thing("CRACHA02", p, pm, "NOVO");
		t02.getUnits().add(crd02.getId());
		Thing t03 = new Thing("CRACHA03", p, pm, "NOVO");
		t03.getUnits().add(crd03.getId());
		Thing t04 = new Thing("CRACHA04", p, pm, "NOVO");
		t04.getUnits().add(crd04.getId());
		Thing t05 = new Thing("CRACHA05", p, pm, "NOVO");
		t05.getUnits().add(crd05.getId());
		Thing t06 = new Thing("CRACHA06", p, pm, "NOVO");
		t06.getUnits().add(crd06.getId());
		Thing t07 = new Thing("CRACHA07", p, pm, "NOVO");
		t07.getUnits().add(crd07.getId());
		Thing t08 = new Thing("CRACHA08", p, pm, "NOVO");
		t08.getUnits().add(crd08.getId());
		Thing t09 = new Thing("CRACHA09", p, pm, "NOVO");
		t09.getUnits().add(crd09.getId());
		Thing t10 = new Thing("CRACHA10", p, pm, "NOVO");
		t10.getUnits().add(crd10.getId());
		Thing t11 = new Thing("CRACHA11", p, pm, "NOVO");
		t11.getUnits().add(crd11.getId());
		Thing t12 = new Thing("CRACHA12", p, pm, "NOVO");
		t12.getUnits().add(crd12.getId());
		Thing t13 = new Thing("CRACHA13", p, pm, "NOVO");
		t13.getUnits().add(crd13.getId());
		Thing t14 = new Thing("CRACHA14", p, pm, "NOVO");
		t14.getUnits().add(crd14.getId());
		Thing t15 = new Thing("CRACHA15", p, pm, "NOVO");
		t15.getUnits().add(crd15.getId());
		Thing t16 = new Thing("CRACHA16", p, pm, "NOVO");
		t16.getUnits().add(crd16.getId());
		Thing t17 = new Thing("CRACHA17", p, pm, "NOVO");
		t17.getUnits().add(crd17.getId());
		Thing t18 = new Thing("CRACHA18", p, pm, "NOVO");
		t18.getUnits().add(crd18.getId());
		Thing t19 = new Thing("CRACHA19", p, pm, "NOVO");
		t19.getUnits().add(crd19.getId());
		Thing t20 = new Thing("CRACHA20", p, pm, "NOVO");
		t20.getUnits().add(crd20.getId());
		Thing t21 = new Thing("CRACHA21", p, pm, "NOVO");
		t21.getUnits().add(crd21.getId());
		Thing t22 = new Thing("CRACHA22", p, pm, "NOVO");
		t22.getUnits().add(crd22.getId());

		iSvc.getrSvc().getThRep().multiPersist(t01, t02, t03, t04, t05, t06, t07, t08, t09, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21, t22);

		User usr01 = new User("ANDRE BENICIO DA SILVA");
		usr01.getProperties().put("UNIT", "E2801160600002053095DADD");
		User usr02 = new User("CARLOS ALBERTO LAMIN");
		usr02.getProperties().put("UNIT", "E2801160600002053097EBE4");
		User usr03 = new User("GUILHERME BRAZ DE FREITAS");
		usr03.getProperties().put("UNIT", "E28011606000020530983B04");
		User usr04 = new User("JOAO OUGUSTO DA SILVA");
		usr04.getProperties().put("UNIT", "E2801160600002053097FA3E");
		User usr05 = new User("JOSE IGNACIO SIEMENTKOWSKI");
		usr05.getProperties().put("UNIT", "E2801160600002090761C8C5");
		User usr06 = new User("LUIS ALBERTO CAMPOS");
		usr06.getProperties().put("UNIT", "E28011606000020907628806");
		User usr07 = new User("RAFAEL DA SILVA");
		usr07.getProperties().put("UNIT", "E28011606000020530985D55");
		User usr08 = new User("RICARDO ALEXANDRE SABINO");
		usr08.getProperties().put("UNIT", "E28011606000020530980544");
		User usr09 = new User("ROBERTO BATISTA");
		usr09.getProperties().put("UNIT", "E28011606000020530983B74");
		User usr10 = new User("SILVIO MACIEL DOS SANTOS");
		usr10.getProperties().put("UNIT", "E28011606000020530973D54");
		User usr11 = new User("VICTOR ODONES DORNELES LIMA");
		usr11.getProperties().put("UNIT", "E2801160600002053096B2DE");
		User usr12 = new User("ANDRE FELIPE VITORINO");
		usr12.getProperties().put("UNIT", "E280116060000205309618DD");
		User usr13 = new User("CLAUDEMIR DE OLIVEIRA MELO");
		usr13.getProperties().put("UNIT", "E2801160600002053095DAEE");
		User usr14 = new User("DENYS LUIZ DE SOUZA");
		usr14.getProperties().put("UNIT", "E280116060000205309794AB");
		User usr15 = new User("DEVANIR MARTINS");
		usr15.getProperties().put("UNIT", "E28011606000020530978B44");
		User usr16 = new User("ELISANDRO LAURENI ADAO");
		usr16.getProperties().put("UNIT", "E2801160600002053096B2ED");
		User usr17 = new User("GABRIEL ARISTIMUNHO NAZIAZENO");
		usr17.getProperties().put("UNIT", "E280116060000205309594DD");
		User usr18 = new User("PAULO ERISON WALTRICK");
		usr18.getProperties().put("UNIT", "E280116060000205309618EE");
		User usr19 = new User("RAFAEL COELHO");
		usr19.getProperties().put("UNIT", "E2801160600002090761C8A5");
		User usr20 = new User("RODRIGO ARRUDA");
		usr20.getProperties().put("UNIT", "E280116060000205309580DE");
		User usr21 = new User("WASHINGTON LUIZ GODOY DOS SANTOS");
		usr21.getProperties().put("UNIT", "E280116060000205309594EE");
		User usr22 = new User("WELLINTON FERNANDO PEREIRA");
		usr22.getProperties().put("UNIT", "E2801160600002090761FCD5");

		iSvc.getrSvc().getUsrRep().multiPersist(usr01, usr02, usr03, usr04, usr05, usr06, usr07, usr08, usr09, usr10, usr11, usr12, usr13, usr14, usr15, usr16, usr17, usr18, usr19, usr20, usr21, usr22);

		CredentialPassword pwd01 = new CredentialPassword(usr01, "andre.silva", "YtqdU5f8");
		CredentialPassword pwd02 = new CredentialPassword(usr02, "carlos.lamin", "FY6ny7SD");
		CredentialPassword pwd03 = new CredentialPassword(usr03, "guilherme.freitas", "gk8y3fKa");
		CredentialPassword pwd04 = new CredentialPassword(usr04, "joao.silva", "Yp3TJWzR");
		CredentialPassword pwd05 = new CredentialPassword(usr05, "jose.ignacio", "vCw5ap6n");
		CredentialPassword pwd06 = new CredentialPassword(usr06, "luis.campos", "wAa64VU9");
		CredentialPassword pwd07 = new CredentialPassword(usr07, "rafael.silva", "bvCwU6Fq");
		CredentialPassword pwd08 = new CredentialPassword(usr08, "ricardo.sabino", "H6NDaBgL");
		CredentialPassword pwd09 = new CredentialPassword(usr09, "roberto.batista", "ZG3mxDvH");
		CredentialPassword pwd10 = new CredentialPassword(usr10, "silvio.santos", "hZ39S4UH");
		CredentialPassword pwd11 = new CredentialPassword(usr11, "vitor.lima", "YPa8xW97");
		CredentialPassword pwd12 = new CredentialPassword(usr12, "andre.vitorino", "CJtsFM6W");
		CredentialPassword pwd13 = new CredentialPassword(usr13, "claudemir.melo", "ZS7gu3Wr");
		CredentialPassword pwd14 = new CredentialPassword(usr14, "denys.souza", "bsa82XDm");
		CredentialPassword pwd15 = new CredentialPassword(usr15, "devanir.martins", "Ka5RDY6C");
		CredentialPassword pwd16 = new CredentialPassword(usr16, "elisandro.adao", "wu8Fybzp");
		CredentialPassword pwd17 = new CredentialPassword(usr17, "gabriel.naziazeno", "jw5eXfD3");
		CredentialPassword pwd18 = new CredentialPassword(usr18, "paulo.erison", "TvzV8PE6");
		CredentialPassword pwd19 = new CredentialPassword(usr19, "rafael.coelho", "J2XsTBfN");
		CredentialPassword pwd20 = new CredentialPassword(usr20, "rodrigo.arruda", "njM3Uk9q");
		CredentialPassword pwd21 = new CredentialPassword(usr21, "washington.santos", "JFz5XSnM");
		CredentialPassword pwd22 = new CredentialPassword(usr22, "wellinton.pereira", "pj29N3xw");

		iSvc.getrSvc().getCrRep().multiPersist(pwd01, pwd02, pwd03, pwd04, pwd05, pwd06, pwd07, pwd08, pwd09, pwd10, pwd11, pwd12, pwd13, pwd14, pwd15, pwd16, pwd17, pwd18, pwd19, pwd20, pwd21, pwd22);

		Permission ppick = iSvc.getrSvc().getPrmRep().findByMetaname("PICK");

		Group gc = new Group("CONFERENCISTAS", "CONF");
		gc.getPermissions().add(ppick);
		Group gs = new Group("SEPARADORES", "SEP");
		gs.getPermissions().add(ppick);

		iSvc.getrSvc().getGrpSvc().getgRep().multiPersist(gc, gs);

		return "OK";

	}

	@GET
	@Path("/mandaosprints")
	@PermitAll
	public String mandaOsPrints() {
		logger.debug("Carregando os prints");
		Set<String> itms = rep.getPrintJson();
		System.out.print("Enviando ps prints");
		for (String s : itms) {
			logger.debug(s);
			JsonReader jr = Json.createReader(new StringReader(s));
			JsonObject job = jr.readObject();
			IntegrationReturn res = rest.sendSync(job, WS_METHOD);
			if(res.isResult()) {
				System.out.println(" - SUCESSO");
			} else {
				logger.debug(" - FALHOU: " + res.getMessage());
			}
		}

		return "OK";
	}

}
