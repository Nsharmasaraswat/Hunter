package com.gtp.hunter.custom.descarpack.timer;

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
public class MailSender {

	@Inject
	private static transient Logger	logger;

	@Resource(name = "java:jboss/mail/gmail")
	private Session					ss;

	@Asynchronous
	public void sendmail(String[] address, String[] cc, String[] bcc, String title, String body) {
		Message msg = new MimeMessage(ss);
		try {
			//msg.addRecipient(type, address);
			logger.debug("TO: " + address.length);
			logger.debug("CC: " + cc.length);
			logger.debug("BCC: " + bcc.length);
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
			logger.debug("Titulo: " + title);
			msg.setSubject(title);
			logger.debug("Booty: " + body);
			msg.setText(body);

			Transport.send(msg);
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}

	}

}
