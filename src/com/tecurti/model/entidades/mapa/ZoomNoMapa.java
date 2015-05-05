package com.tecurti.model.entidades.mapa;

import java.util.ArrayList;
import java.util.List;

import com.tecurti.model.service.MapaService;

public class ZoomNoMapa {
    
    List<List<PontoNoMapa>> listZoom = new ArrayList<>();	
    
    public void addPontoNoMapa(int nivelZoom, PontoNoMapa pontoNoMapa) {
	aumentarListaDeZoomSeNecessario(nivelZoom);
	List<PontoNoMapa> zoom = listZoom.get(nivelZoom-1);
	zoom.add(pontoNoMapa);
    }

    private void aumentarListaDeZoomSeNecessario(int tamanhoNecessario) {
	for (int i = 0; i < tamanhoNecessario; i++) {
	    if (listZoom.size() <= i) {
		listZoom.add(new ArrayList<PontoNoMapa>());
	    }
	}
    }
    
    public static void main(String[] args) {
	
	ZoomNoMapa zoom = new ZoomNoMapa();
	zoom.addPontoNoMapa(1, MapaService.findEstado(1));
	zoom.addPontoNoMapa(1, MapaService.findEstado(2));
	zoom.addPontoNoMapa(1, MapaService.findEstado(3));
	zoom.addPontoNoMapa(2, MapaService.findEstado(20));
	zoom.addPontoNoMapa(2, MapaService.findEstado(21));
	zoom.addPontoNoMapa(1, MapaService.BRASIL);
	zoom.addPontoNoMapa(4, MapaService.findEstado(13));
	
	zoom.toString();
    }

    @Override
    public String toString() {
	String toString = "";
	int nivel = 0;
	for (List<PontoNoMapa> list : listZoom) {
	    nivel++;
	    toString += "\nNivel "+nivel;
	    for (PontoNoMapa pontoNoMapa : list) {
		toString += "\n"+pontoNoMapa;
	    }
	}
	return toString;
    }
}





