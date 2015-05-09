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
    
    public int getTotalNiveis() {
	return listZoom.size();
    }
    
    public List<PontoNoMapa> getPontosNoMapaParaOsNiveisMaioresQue(int nivel) {
	List<PontoNoMapa> list = new ArrayList<>();
	
	for (int nivelAtual = nivel + 1; nivelAtual <= getTotalNiveis(); nivelAtual++) {
	    List<PontoNoMapa> listNiveis = getPontosNoMapaDoNivel(nivelAtual);
	    if (listNiveis != null) {
		list.addAll(listNiveis);
	    }
	}
	
	return list;
    }
    public List<PontoNoMapa> getPontosNoMapaParaOsNiveisMenoresQue(int nivel) {
	List<PontoNoMapa> list = new ArrayList<>();
	
	for (int nivelAtual = 1; nivelAtual < nivel; nivelAtual++) {
	    List<PontoNoMapa> listNiveis = getPontosNoMapaDoNivel(nivelAtual);
	    if (listNiveis != null) {
		list.addAll(listNiveis);
	    }
	}
	
	return list;
    }
    public List<PontoNoMapa> getPontosNoMapaDoNivel(int nivel) {
	if (nivel < 0 || nivel > listZoom.size()) {
	    return null;
	}
	
	return listZoom.get(nivel-1);
    }
}










