package com.tecurti.view.util;

import com.tecurti.model.entidades.MimeType;

public class UploadedFile implements Cloneable {

    public String nomeParametro;
    public String nomeArquivo;
    public MimeType mimeType;
    public byte[] bytes;
    
    public UploadedFile() {
	super();
    }
    public UploadedFile(String nomeArquivo, byte[] bytes, MimeType mimeType) {
	this.nomeArquivo = nomeArquivo;
	this.bytes = bytes;
	this.mimeType = mimeType;
    }
    public UploadedFile(byte[] bytes, MimeType mimeType) {
	super();
	this.bytes = bytes;
	this.mimeType = mimeType;
    }

    public boolean isGifAnimado() {
	return mimeType.in(MimeType.GIF);
    }
    
    @Override
    public UploadedFile clone() throws CloneNotSupportedException {
	return (UploadedFile) super.clone();
    }
    
}
