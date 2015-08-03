package com.tecurti.model.entidades;

public enum MimeType {
    JPEG("image/jpeg"), GIF("image/gif"), PNG("image/png");

    public final String descricao;

    private MimeType(String mimeType) {
	this.descricao = mimeType;
    }

    public static MimeType findByNomeArquivo(String nomeArquivo) {
	String nomeArquivoLower = nomeArquivo.toLowerCase();
	if (nomeArquivoLower.endsWith(".png")) {
	    return PNG;
	} else if (nomeArquivoLower.endsWith(".gif")) {
	    return GIF;
	} else if (nomeArquivoLower.endsWith(".jpg") || nomeArquivoLower.endsWith(".jpeg")) {
	    return JPEG;
	} else {
	    return null;
	}
    }

    public static MimeType findByDescricaoMimeType(String descricaoMime) {
	for (MimeType mimeType : MimeType.values()) {
	    if (mimeType.descricao.equalsIgnoreCase(descricaoMime)) {
		return mimeType;
	    }
	}
	return null;
    }

    public boolean in(MimeType... arrayMimeType) {
	for (MimeType mimeType : arrayMimeType) {
	    if (mimeType == this) {
		return true;
	    }
	}
	return false;
    }
}