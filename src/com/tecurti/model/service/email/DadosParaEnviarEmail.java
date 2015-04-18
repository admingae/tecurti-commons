package com.tecurti.model.service.email;

import java.util.ArrayList;
import java.util.List;

public class DadosParaEnviarEmail {

    String nomeRemetente;
    String emailRemetente;
    String nomeDestinatario;
    String emailDestinatario;
    List<String> listEmailDestinatarios = new ArrayList<>();
    boolean usuarioQueReceberaEmailPodeVerOsOutrosEmCopia = true;
    String emailDestinatarioComCopia;
    String emailDestinatarioComCopiaOculta;
    String assunto;
    String corpoMensagemHtml;
    String corpoMensagemTexto;
    
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
    
    public boolean isUsuarioQueRecebeuEmailPodeVerOsOutrosEmCopia() {
        return isUsuarioQueReceberaEmailPodeVerOsOutrosEmCopia();
    }
    public boolean isUsuarioQueReceberaEmailPodeVerOsOutrosEmCopia() {
        return usuarioQueReceberaEmailPodeVerOsOutrosEmCopia;
    }
    public void setUsuarioQueRecebeuEmailPodeVerOsOutrosEmCopia(boolean usuarioQueRecebeuEmailPodeVerOsOutrosEmCopia) {
        setUsuarioQueReceberaEmailPodeVerOsOutrosEmCopia(usuarioQueRecebeuEmailPodeVerOsOutrosEmCopia);
    }
    public void setUsuarioQueReceberaEmailPodeVerOsOutrosEmCopia(boolean usuarioQueRecebeuEmailPodeVerOsOutrosEmCopia) {
        this.usuarioQueReceberaEmailPodeVerOsOutrosEmCopia = usuarioQueRecebeuEmailPodeVerOsOutrosEmCopia;
    }
    public void addEmailDestinatario(String email) {
	listEmailDestinatarios.add(email);
    }
    public List<String> getListEmailDestinatarios() {
        return listEmailDestinatarios;
    }
    public void setListEmailDestinatarios(List<String> listEmailDestinatarios) {
        this.listEmailDestinatarios = listEmailDestinatarios;
    }

}