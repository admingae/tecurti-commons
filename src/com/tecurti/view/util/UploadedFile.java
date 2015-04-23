package com.tecurti.view.util;

public class UploadedFile {

    public String nomeParametro;
    public String nomeArquivo;
    public byte[] bytes;

    public UploadedFile() {
    }

    public UploadedFile(byte[] bytes) {
	super();
	this.bytes = bytes;
    }

}
