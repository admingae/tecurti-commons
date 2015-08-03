package com.tecurti.view.util;

import com.tecurti.model.entidades.MimeType;

public class UploadedFile {

    public String nomeParametro;
    public String nomeArquivo;
    public MimeType mimeType;
    public byte[] bytes;

    public UploadedFile(byte[] bytes, MimeType mimeType) {
	super();
	this.bytes = bytes;
	this.mimeType = mimeType;
    }

    public boolean isGifAnimado() {
	return mimeType.in(MimeType.GIF);
    }

}
