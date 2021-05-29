package com.gtp.hunter.core.util;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;

@Stateless
public class MailUtil {

	@Inject
	private Logger	logger;

	@Resource(name = "java:jboss/mail/hunter2")
	private Session	ss;

	@Asynchronous
	public void sendmail(String[] address, String[] cc, String[] bcc, String title, String body) {
		Message msg = new MimeMessage(ss);
		try {
			logger.info("TO: " + (address != null ? address.length : "No Mail Recipients"));
			logger.info("CC: " + (cc != null ? cc.length : "No Mail Recipients"));
			logger.info("BCC: " + (bcc != null ? bcc.length : "No Mail Recipients"));
			for (String s : address) {
				if (s.trim().length() > 1)
					msg.addRecipient(RecipientType.TO, new InternetAddress(s));
			}
			if (cc != null) {
				for (String s : cc) {
					if (s.trim().length() > 1)
						msg.addRecipient(RecipientType.CC, new InternetAddress(s));
				}
			}
			if (bcc != null) {
				for (String s : bcc) {
					if (s.trim().length() > 1)
						msg.addRecipient(RecipientType.BCC, new InternetAddress(s));
				}
			}
			logger.info("Title: " + title);
			msg.setSubject(title);
			logger.info("Body: " + body);
			msg.setText(body);

			Transport.send(msg);
		} catch (AddressException e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
		} catch (MessagingException e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
		}
	}

}
