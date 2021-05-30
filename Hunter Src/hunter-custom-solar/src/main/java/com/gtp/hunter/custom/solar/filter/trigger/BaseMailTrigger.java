package com.gtp.hunter.custom.solar.filter.trigger;

import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.wf.filter.trigger.BaseTrigger;

public abstract class BaseMailTrigger extends BaseTrigger {

	protected IntegrationService is;

	public BaseMailTrigger(SAPSolar solar, SAPService svc, IntegrationService is) {
		super(new FilterTrigger());
		this.is = is;
	}

	public BaseMailTrigger(FilterTrigger model) {
		super(model);
	}

	protected void sendMail(String groupMeta, String tkNum, String load, String transp, String truck, String date, String mailProp, String subjectProp, String bodyProp) {
		//		List<User> usList = getISvc().getRegSvc().getUsrSvc().listByGroup(groupMeta);
		//		List<String> mailList = usList.parallelStream().map(us -> us.getProperties().get("mail")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
		//		String[] to = new String[mailList.size()];
		//		to = mailList.toArray(to);
		String[] to = ConfigUtil.get("hunter-custom-solar", mailProp, "").split(",");
		String[] cc = new String[] {};
		String[] bcc = new String[] {};
		String subject = ConfigUtil.get("hunter-custom-solar", subjectProp, "");
		String body = ConfigUtil.get("hunter-custom-solar", bodyProp, "");

		body = body.replace("${loadId}", load)
						.replace("${transpCode}", tkNum)
						.replace("${transpId}", transp)
						.replace("${truckId}", truck)
						.replace("${transpDt}", date);
		is.getMail().sendmail(to, cc, bcc, subject, body);
	}
}
