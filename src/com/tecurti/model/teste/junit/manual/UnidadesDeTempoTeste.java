package com.tecurti.model.teste.junit.manual;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.tecurti.model.entidades.Tempo;
import com.tecurti.model.entidades.UnidadeTempo;
import com.tecurti.model.utils.ModelUtils;

public class UnidadesDeTempoTeste {
    
    static DateFormat formatDataHora;
    static Calendar dataBase;
    
    @BeforeClass
    public static void metodoExecutadoAntes() throws Exception {
	formatDataHora = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	dataBase = Calendar.getInstance();
	dataBase.setTime(formatDataHora.parse( "21/09/2013 20:06:09" ));
    }
    
    @Test
    public void testeUmSegundo() throws Exception {
	fazerAssert("21/09/2013 20:06:10", 1, UnidadeTempo.SEGUNDOS);
    }
    @Test
    public void testeUmMinuto() throws Exception {
	fazerAssert("21/09/2013 20:07:09", 1, UnidadeTempo.MINUTOS);
    }
    @Test
    public void testeUmaHora() throws Exception {
	fazerAssert("21/09/2013 21:06:09", 1, UnidadeTempo.HORAS);
    }
    @Test
    public void testeUmDia() throws Exception {
	fazerAssert("22/09/2013 20:06:09", 1, UnidadeTempo.DIAS);
    }
    @Test
    public void testeUmaSemana() throws Exception {
	fazerAssert("28/09/2013 20:06:09", 1, UnidadeTempo.SEMANAS);
    }
    @Test
    public void testeUmMes() throws Exception {
	fazerAssert("22/10/2013 20:06:09", 1, UnidadeTempo.MESES);
    }
    @Test
    public void testeUmAno() throws Exception {
	fazerAssert("21/09/2014 20:06:09", 1, UnidadeTempo.ANOS);
    }
    @Test
    public void testeOnzeSegundos() throws Exception {
	fazerAssert("21/09/2013 20:06:20", 11, UnidadeTempo.SEGUNDOS);
    }
    @Test
    public void testeQuatroMinutos() throws Exception {
	fazerAssert("21/09/2013 20:10:29", 4, UnidadeTempo.MINUTOS);
    }
    @Test
    public void testeUmAno_e_CincoDias() throws Exception {
	fazerAssert("26/09/2014 20:06:09", 1, UnidadeTempo.ANOS);
    }
    @Test
    public void testeUmAno_e_NoveMeses() throws Exception {
	fazerAssert("21/06/2015 20:06:09", 1, UnidadeTempo.ANOS);
    }
    @Test
    public void testeTresAnos() throws Exception {
	fazerAssert("21/09/2016 20:06:09", 3, UnidadeTempo.ANOS);
    }
    @Test
    public void testeTresSemanas_e_DoisDias() throws Exception {
	fazerAssert("14/10/2013 20:06:09", 3, UnidadeTempo.SEMANAS);
    }
    @Test
    public void testeUmMinutos_e_VinteSegundos() throws Exception {
	fazerAssert("21/09/2013 20:07:29", 1, UnidadeTempo.MINUTOS);
    }
    @Test
    public void testeDuasHoras_TrintaMinutos_e_DezSegudos() throws Exception {
	fazerAssert("21/09/2013 22:36:19", 2, UnidadeTempo.HORAS);
    }
    @Test
    public void testeSeisDias_QuinzeSegudos() throws Exception {
	fazerAssert("27/09/2013 20:06:24", 6, UnidadeTempo.DIAS);
    }

    private void fazerAssert(String data, long tempoEsperado, UnidadeTempo unidadeTempoEsperada) throws Exception {

	Calendar calendar = Calendar.getInstance();
	calendar.setTime(formatDataHora.parse( data ));

	Tempo pegaData = ModelUtils.calcularTempoEntreDatas(dataBase, calendar);
	Assert.assertEquals(tempoEsperado, pegaData.getTempo());
	Assert.assertEquals(unidadeTempoEsperada, pegaData.getUnidade());
    }
    
}
