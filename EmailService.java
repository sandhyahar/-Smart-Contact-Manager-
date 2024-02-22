package com.smart;

import java.net.PasswordAuthentication;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	public boolean sendEmail(String subject, String message, String to) {

		boolean f = false;
		String from = "sandhyabhanushalisrk@gmail.com";
		String host = "smtp.gmail.com";

		Properties properties = System.getProperties();
		System.out.println(properties);

		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");

		// Step 1:
		Session session = Session.getInstance(properties, new Authenticator() {
			protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
				return new javax.mail.PasswordAuthentication("sandhyabhanushalisrk@gmail.com", "ozgd jkpo djbz dxca");
			}
		});

		session.setDebug(true);

		// step 2:
		MimeMessage m = new MimeMessage(session);

		try {
			m.setFrom(from);
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			m.setSubject(subject);
			//m.setText(message);
			
			m.setContent(message,"text/html");

			// step 3:
			Transport.send(m);

			System.out.println("sent success....");
			f = true;

		} catch (MessagingException mex) {
			// Handle specific messaging exceptions
			mex.printStackTrace();
		}

		return f;
	}
}
