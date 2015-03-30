package com.tecurti.model.entidades;

import java.io.Serializable;

import com.tecurti.model.service.idioma.i18nUsandoApiPadrao;

public enum UnidadeTempo implements Serializable {

    SEGUNDOS {
	@Override
	public String getDescricao(long value, i18nUsandoApiPadrao idioma) {
	    
	    if (value == 1)  {
		return idioma.geti18nTexto("dataCerca1SegundoAtras");
	    } else {
		return idioma.geti18nTexto("dataCercaXSegundosAtras").replace("{0}", value+"");	
	    }
	}
    },
    MINUTOS {
	@Override
	public String getDescricao(long value, i18nUsandoApiPadrao idioma) {

	    if (value == 1)  {
		return idioma.geti18nTexto("dataCerca1MinutoAtras");
	    } else {
		return idioma.geti18nTexto("dataCercaXMinutosAtras").replace("{0}", value+"");	
	    }
	}
    },
    HORAS {
	@Override
	public String getDescricao(long value, i18nUsandoApiPadrao idioma) {

	    if (value == 1)  {
		return idioma.geti18nTexto("dataCerca1HoraAtras");
	    } else {
		return idioma.geti18nTexto("dataCercaXHorasAtras").replace("{0}", value+"");	
	    }
	}
    },
    DIAS {
	@Override
	public String getDescricao(long value, i18nUsandoApiPadrao idioma) {

	    if (value == 1)  {
		return idioma.geti18nTexto("dataCerca1DiaAtras");
	    } else {
		return idioma.geti18nTexto("dataCercaXDiasAtras").replace("{0}", value+"");	
	    }
	}
    },
    SEMANAS {
	@Override
	public String getDescricao(long value, i18nUsandoApiPadrao idioma) {
	    if (value == 1)  {
		return idioma.geti18nTexto("dataCerca1SemanaAtras");
	    } else {
		return idioma.geti18nTexto("dataCercaXSemanasAtras").replace("{0}", value+"");	
	    }
	}
    },
    MESES {
	@Override
	public String getDescricao(long value, i18nUsandoApiPadrao idioma) {
	    if (value == 1)  {
		return idioma.geti18nTexto("dataCerca1MesAtras");
	    } else {
		return idioma.geti18nTexto("dataCercaXMesesAtras").replace("{0}", value+"");	
	    }
	}
    },
    ANOS {
	@Override
	public String getDescricao(long value, i18nUsandoApiPadrao idioma) {
	    if (value == 1)  {
		return idioma.geti18nTexto("dataCerca1AnoAtras");
	    } else {
		return idioma.geti18nTexto("dataCercaXAnosAtras").replace("{0}", value+"");	
	    }
	}
    };
    
    public abstract String getDescricao(long value, i18nUsandoApiPadrao idioma);

}