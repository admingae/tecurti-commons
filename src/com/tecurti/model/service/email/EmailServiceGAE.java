package com.tecurti.model.service.email;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.tecurti.model.utils.ModelUtils;

public class EmailServiceGAE {

    public static Session abreSessao() throws Exception {
	 
	Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
	
	return session;
    }
    
    public void enviarEmail(DadosParaEnviarEmail dadosParaEnviarEmail) throws Exception {
	
	dadosParaEnviarEmail.setEmailDestinatario(dadosParaEnviarEmail.getEmailDestinatario());
	dadosParaEnviarEmail.setEmailsDestinatarios(dadosParaEnviarEmail.getEmailsDestinatarios());
	
	MimeMessage msg = new MimeMessage((Session)abreSessao());
	msg.setFrom     (new InternetAddress(dadosParaEnviarEmail.getEmailRemetente(), dadosParaEnviarEmail.getNomeRemetente()));
//	msg.setFrom     (new InternetAddress(Config.EMAIL_REMETENTE_EMAIL, Config.EMAIL_REMETENTE_NOME));
	
	if (dadosParaEnviarEmail.getEmailsDestinatarios() != null && !dadosParaEnviarEmail.getEmailsDestinatarios().equals("")) {
	    msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(dadosParaEnviarEmail.getEmailsDestinatarios()));
	} else {
	    if (ModelUtils.isEmpty(dadosParaEnviarEmail.getEmailDestinatario()) || ModelUtils.isEmailValido(dadosParaEnviarEmail.getEmailDestinatario()) == false) {
		return;
	    }
	    msg.addRecipient(Message.RecipientType.TO, new InternetAddress(dadosParaEnviarEmail.getEmailDestinatario(), dadosParaEnviarEmail.getNomeDestinatario()));	    
	}
	
	msg.setSubject  (dadosParaEnviarEmail.getAssunto(), "UTF-8");
	
	boolean isEnviarMensagemFormatoHtml = dadosParaEnviarEmail.getCorpoMensagemHtml() != null && !dadosParaEnviarEmail.getCorpoMensagemHtml().equals("");
	if (isEnviarMensagemFormatoHtml) { 
	    msg.setContent (criarMultipart(dadosParaEnviarEmail.getCorpoMensagemHtml()));
	} else {
	    msg.setText    (dadosParaEnviarEmail.getCorpoMensagemTexto());
	}
	
	Transport.send(msg);
    }
    
    private Multipart criarMultipart(String texto) throws MessagingException {
	
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(texto, "text/html");
        
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(htmlPart);
        return mp;
    }    
}











