package com.gtp.hunter.core.websocket.source;

import java.util.concurrent.Callable;

import com.gtp.hunter.core.model.Source;
import com.gtp.hunter.core.util.MailUtil;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;

public class SourceDisconnectNotifier implements Callable<Boolean> {

	private MailUtil			mailer;
	private Source				src;
	private static final long	TIMESTAMP	= System.currentTimeMillis();

	public SourceDisconnectNotifier(MailUtil mailer, Source src) {
		this.mailer = mailer;
		this.src = src;
	}

	@Override
	public Boolean call() throws Exception {
		String[] to = ConfigUtil.get("hunter-core", "source-mailer-to", "suporte@gtpautomation.com").split(",");
		String[] cc = ConfigUtil.get("hunter-core", "source-mailer-cc", "suporte@gtpautomation.com").split(",");
		String[] bcc = ConfigUtil.get("hunter-core", "source-mailer-bcc", "suporte@gtpautomation.com").split(",");
		String subject = ConfigUtil.get("hunter-core", "source-mailer-title", "Source Disconnected");
		String body = ConfigUtil.get("hunter-core", "source-mailer-body", "Source ${src_metaname} (${src_id}) was disconnected ${elapsed_time} seconds ago");

		body = body.replace("${src_metaname}", src.getMetaname())
						.replace("${src_id}", src.getId().toString())
						.replace("${elapsed_time}", String.valueOf(System.currentTimeMillis() - TIMESTAMP));
		mailer.sendmail(to, cc, bcc, subject, body);
		return true;
	}

}
