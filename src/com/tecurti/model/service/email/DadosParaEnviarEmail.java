package com.tecurti.model.service.email;

import com.tecurti.model.service.idioma.i18nUsandoApiPadrao;

public class DadosParaEnviarEmail {

    String nomeRemetente;
    String emailRemetente;
    String nomeDestinatario;
    String emailDestinatario;
    String emailsDestinatarios;
    String emailDestinatarioComCopia;
    String emailDestinatarioComCopiaOculta;
    String assunto;
    String corpoMensagemHtml;
    String corpoMensagemTexto;
//    i18nUsandoApiPadrao idioma;
    
    public String getNomeRemetente() {
        return nomeRemetente;
    }
    public void setNomeRemetente(String nomeRemetente) {
        this.nomeRemetente = nomeRemetente;
    }
    public String getEmailRemetente() {
        return emailRemetente;
    }
    public void setEmailRemetente(String emailRemetente) {
        this.emailRemetente = emailRemetente;
    }
    public String getNomeDestinatario() {
        return nomeDestinatario;
    }
    public void setNomeDestinatario(String nomeDestinatario) {
        this.nomeDestinatario = nomeDestinatario;
    }    
    public String getEmailDestinatario() {
        return emailDestinatario;
    }
    public void setEmailDestinatario(String emailDestinatario) {
        this.emailDestinatario = emailDestinatario;
    }
    public String getEmailDestinatarioComCopia() {
        return emailDestinatarioComCopia;
    }
    public void setEmailDestinatarioComCopia(String emailDestinatarioComCopia) {
        this.emailDestinatarioComCopia = emailDestinatarioComCopia;
    }
    public String getEmailDestinatarioComCopiaOculta() {
        return emailDestinatarioComCopiaOculta;
    }
    public void setEmailDestinatarioComCopiaOculta(String emailDestinatarioComCopiaOculta) {
        this.emailDestinatarioComCopiaOculta = emailDestinatarioComCopiaOculta;
    }
    public String getAssunto() {
        return assunto;
    }
    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }
    public String getCorpoMensagemHtml() {
        return corpoMensagemHtml;
    }
    public void setCorpoMensagemHtml(String corpoMensagemHtml) {
        this.corpoMensagemHtml = corpoMensagemHtml;
    }
    public String getCorpoMensagemTexto() {
        return corpoMensagemTexto;
    }
    public void setCorpoMensagemTexto(String corpoMensagemTexto) {
        this.corpoMensagemTexto = corpoMensagemTexto;
    }
    
    public String getEmailsDestinatarios() {
        return emailsDestinatarios;
    }
    public void setEmailsDestinatarios(String emailsDestinatarios) {
        this.emailsDestinatarios = emailsDestinatarios;
    }
//    public i18nUsandoApiPadrao getIdioma() {
//        return idioma;
//    }
//    public void setIdioma(i18nUsandoApiPadrao idioma) {
//        this.idioma = idioma;
//    }

}