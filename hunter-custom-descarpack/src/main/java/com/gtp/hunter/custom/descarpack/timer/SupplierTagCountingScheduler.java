package com.gtp.hunter.custom.descarpack.timer;

import java.util.Map;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.custom.descarpack.repository.CustomRepository;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;

@Stateless
public class SupplierTagCountingScheduler {

	@Inject
	private MailSender			mail;

	@Inject
	private CustomRepository	cRep;

	@Schedule(hour = "8", minute = "0", second = "0")
	public void sendMail() {
		String mailcomex = ConfigUtil.get("hunter-custom-descarpack", "mailcomex", "fernando.projetos@gmail.com");
		String mailcc = ConfigUtil.get("hunter-custom-descarpack", "mailcc", null);
		String mailbcc = ConfigUtil.get("hunter-custom-descarpack", "mailbcc", null);
		String subject = "[HUNTER] Quantidade de Tags impressas por fornecedor - ";
		String[] to = mailcomex == null ? new String[] {} : mailcomex.split(",");
		String[] cc = mailcc == null ? new String[] {} : mailcc.split(",");
		String[] bcc = mailbcc == null ? new String[] {} : mailbcc.split(",");

		if (to.length > 0)
			mail.sendmail(to, cc, bcc, subject, gerabody());
	}

	private String gerabody() {
		Map<String, Integer> lst = cRep.getSupplierDailyTags();
		StringBuilder sb = new StringBuilder("Quantidade de Tags impressas: \r\n");
		
		for (String s : lst.keySet()) {
			sb.append(s + " - " + lst.get(s) + "\r\n");
		}
		return sb.toString();
	}

}
