package com.tecurti.model.service.email;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
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

    public void enviarEmail(DadosParaEnviarEmail dadosParaEnviarEmail) throws Exception {
	
	Session session = Session.getDefaultInstance(new Properties(), null);
	MimeMessage msg = new MimeMessage(session);
	msg.setFrom(new InternetAddress(dadosParaEnviarEmail.getEmailRemetente(), dadosParaEnviarEmail.getNomeRemetente()));

	if (dadosParaEnviarEmail.getListEmailDestinatarios().size() > 0) {
	    RecipientType recipientType = dadosParaEnviarEmail.isUsuarioQueReceberaEmailPodeVerOsOutrosEmCopia() ? Message.RecipientType.TO : Message.RecipientType.BCC;
	    String listEmailsSeparadosPorVirgulaAsString = toStringListDeEmailsSeparandoPorVirgula(dadosParaEnviarEmail.getListEmailDestinatarios());
	    
	    msg.addRecipients(recipientType, InternetAddress.parse(listEmailsSeparadosPorVirgulaAsString));
	} else {
	    if (ModelUtils.isEmailValido(dadosParaEnviarEmail.getEmailDestinatario()) == false) {
		return;
	    }
	    msg.addRecipient(Message.RecipientType.TO, new InternetAddress(dadosParaEnviarEmail.getEmailDestinatario(), dadosParaEnviarEmail.getNomeDestinatario()));
	}

	msg.setSubject(dadosParaEnviarEmail.getAssunto(), "UTF-8");

	boolean isEnviarMensagemFormatoHtml = dadosParaEnviarEmail.getCorpoMensagemHtml() != null && !dadosParaEnviarEmail.getCorpoMensagemHtml().equals("");
	if (isEnviarMensagemFormatoHtml) {
	    msg.setContent(criarMultipart(dadosParaEnviarEmail.getCorpoMensagemHtml()));
	} else {
	    msg.setText(dadosParaEnviarEmail.getCorpoMensagemTexto());
	}

	Transport.send(msg);
    }
    
    private String toStringListDeEmailsSeparandoPorVirgula(List<String> listEmails) {
	StringBuilder builder = new StringBuilder("");
	for (String email : listEmails) {
	    if (ModelUtils.isEmailValido(email) == false) {
		continue;
	    }
	    if (builder.length() > 0) {
		builder.append(",");
	    }
	    builder.append(email);
	}
	return builder.toString();
    }
    
    public static void main(String[] args) {
	List<String> ll = new ArrayList<>();
	ll.add("Felipe@gmail.com");
	ll.add("Felipe1gmail.com");
	ll.add("Felipe2@gmail.com");
//	ll.add("Felipe1");
//	ll.add("Felipe2");
	System.err.println(new EmailServiceGAE().toStringListDeEmailsSeparandoPorVirgula(ll));
    }

    private Multipart criarMultipart(String texto) throws MessagingException {
	
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(texto, "text/html");
        
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(htmlPart);
        return mp;
    }    
}











