package com.tecurti.model.persistencia.dao.gae;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ColunaGAE {
    
    public static enum Tipo {
	TEXTO_MUITOS_CARACTERES
    }
    
    Tipo tipoGae();

}
