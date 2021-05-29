package com.gtp.hunter.custom.solar.service;

import java.util.HashMap;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.NotImplementedException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.dtos.ReadFieldsSap;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReadControleDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReadStartDTO;
import com.gtp.hunter.custom.solar.sap.worker.BaseWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWControleQualidadeWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWInformacaoChegadaWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWInformacaoInventarioWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWInformacaoVeiculoWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWOrdemProducaoWorker;
import com.gtp.hunter.custom.solar.util.Constants;
import com.gtp.hunter.custom.solar.util.ToJsonSAP;

@Stateless
public class SAPService {

	@Inject
	private SAPSolar			solar;

	@Inject
	private IntegrationService	iSvc;

	private boolean				immediateLog	= false;

	public boolean call(int code, String controle) {
		try {

			Profiler p = new Profiler("SAPCALL - CODE: " + code + " CONTROLE: " + controle, immediateLog);

			/**
			 * Verificando se o CODE for 8 o fluxo será modificado
			 */
			p.step("VERIFICANDO CODE PARA DEFINIÇÂO FLUXO", immediateLog);
			p.step(String.format("INICIANDO FLUXO BASICO CODE => %s", code), immediateLog);

			final Gson gson = new GsonBuilder().create();

			p.step("INICIANDO GETFUNC - READ_START", immediateLog);
			ToJsonSAP jcoSonReadStart = new ToJsonSAP(solar.getFunc(Constants.RFC_START));
			p.step("FINALIZANDO GETFUNC - PARAMETRIZANDO", immediateLog);
			jcoSonReadStart.setParameters(new HashMap<String, Object>() {
				{
					put(Constants.I_CODE, String.valueOf(code));
					put(Constants.I_CONTROLE, controle);
				}
			});
			p.step("EXECUTANDO READ_START", immediateLog);
			String resultJsonReadStart = jcoSonReadStart.execute(solar.getDestination());

			p.step("EXECUTADO READ_START - LENDO RETORNO", immediateLog);
			ReadFieldsSap readFieldsSap = gson.fromJson(resultJsonReadStart, ReadFieldsSap.class);

			p.step("LISTANDO PARAMS - " + readFieldsSap.getReadStartDTOs().size(), immediateLog);
			for (SAPReadStartDTO start : readFieldsSap.getReadStartDTOs()) {

				if (!start.getStatus().equalsIgnoreCase("L")) {
					return false;
				}

				p.step("INICIANDO READ_CONTROLE", immediateLog);
				ToJsonSAP jcoSonReadControle = new ToJsonSAP(solar.getFunc(Constants.RFC_CONTROLE));
				p.step("PARAMETRIZANDO READ_CONTROLE", immediateLog);
				jcoSonReadControle.setParameters(new HashMap<String, Object>() {
					private static final long serialVersionUID = 6367724332112855379L;

					{
						put(Constants.I_CODE, String.valueOf(code));
					}
				});
				p.step("EXECUTANDO READ_CONTROLE", immediateLog);
				String resultJsonReadControle = jcoSonReadControle.execute(solar.getDestination());
				p.step("LISTANDO PARAMS READ_CONTROLE", immediateLog);
				readFieldsSap = gson.fromJson(resultJsonReadControle, ReadFieldsSap.class);

				for (SAPReadControleDTO control : readFieldsSap.getReadControleDTOs()) {

					if (!control.getInterCode().equalsIgnoreCase("1")) {
						continue;
					}

					p.step("PARAMETRIZANDO " + control.getNomeRfc(), immediateLog);
					jcoSonReadControle.setParameters(new HashMap<String, Object>() {
						private static final long serialVersionUID = 3115140042772026957L;

						{
							put(Constants.I_CONTROLE, controle);
						}
					});
					// p.step("EXECUTADO " + control.getNomeRfc(), false);
					// String resultJsonReadTeste = jcoSonReadTeste.execute(solar.getDestination());
					// //System.out.println("Results Teste: " + resultJsonReadTeste);
					//
					// readFieldsSap = gson.fromJson(resultJsonReadTeste, ReadFieldsSap.class);

					// TODO: Colocar a lista de Controle para carregar o worker dinamicamente
					BaseWorker worker = null;
					switch (code) {
						case Constants.CODE_NF:
							worker = new ZHWInformacaoChegadaWorker(this, solar, iSvc);
							break;
						case Constants.CODE_INVENTARIO:
							worker = new ZHWInformacaoInventarioWorker(this, solar, iSvc);
							break;
						case Constants.CODE_PLANPROD:
							worker = new ZHWOrdemProducaoWorker(this, solar, iSvc);
							break;
						case Constants.CODE_VEICULO:
							worker = new ZHWInformacaoVeiculoWorker(this, solar, iSvc);
							break;
						case Constants.CODE_TRANSFERENCIA:
						case Constants.CODE_SOLICRESERVA:
							//SOLICITACAO_RESERVA_MATERIAL
							break;
						case Constants.CODE_CONTROLEQUALIDADE:
							worker = new ZHWControleQualidadeWorker(this, solar, iSvc);
							break;
						default:
							throw new NotImplementedException("Code not Available");
					}
					// ZHWInformacaoChegadaWorker worker = new ZHWInformacaoChegadaWorker(this,
					// solar, integrationService);
					p.step("EXECUTANDO " + control.getNomeRfc(), immediateLog);
					if (worker != null) {
						worker.work(start);
					}
				}
			}
			p.done("EXECUÇÃO FINALIZADA", immediateLog, true);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public void external(Object obj) {
	}

	//	private void callForCode8(int code, String controle, Profiler p) throws Exception {
	//		final Gson gson = new GsonBuilder().create();
	//
	//		p.step("INICIANDO GETFUNC - READ_START", false);
	//		ToJsonSAP jcoSonReadStart = new ToJsonSAP(solar.getFunc(Constants.Z_HW_READ_START));
	//		p.step("FINALIZANDO GETFUNC - PARAMETRIZANDO", false);
	//		jcoSonReadStart.setParameters(new HashMap<String, Object>() {
	//			{
	//				put(Constants.I_CODE, String.valueOf(code));
	//
	//				if (controle != null && !controle.isEmpty()) {
	//					put(Constants.I_CONTROLE, controle);
	//				}
	//			}
	//		});
	//
	//		p.step("EXECUTANDO READ_START", false);
	//		String resultJsonReadStart = jcoSonReadStart.execute(solar.getDestination());
	//
	//		p.step("EXECUTADO READ_START - LENDO RETORNO", false);
	//		ReadFieldsSap readFieldsSap = gson.fromJson(resultJsonReadStart, ReadFieldsSap.class);
	//
	//		readFieldsSap.getReadStartDTOs().stream().filter(start -> start.getStatus().equalsIgnoreCase("L"))
	//				.forEach(start -> {
	//					try {
	//
	//						p.step("INICIANDO READ_CONTROLE", false);
	//						ToJsonSAP jcoSonReadControle = new ToJsonSAP(solar.getFunc(Constants.Z_HW_READ_CONTROLE));
	//						p.step("PARAMETRIZANDO READ_CONTROLE", false);
	//						jcoSonReadControle.setParameters(new HashMap<String, Object>() {
	//							{
	//								put(Constants.I_CODE, String.valueOf(code));
	//							}
	//						});
	//						p.step("EXECUTANDO READ_CONTROLE", false);
	//						String resultJsonReadControle;
	//						resultJsonReadControle = jcoSonReadControle.execute(solar.getDestination());
	//						p.step("LISTANDO PARAMS READ_CONTROLE", false);
	//						ReadFieldsSap readFieldsSapControle = gson.fromJson(resultJsonReadControle,
	//								ReadFieldsSap.class);
	//
	//						readFieldsSapControle.getReadControleDTOs().stream()
	//								.filter(readControle -> readControle.getInterCode().equalsIgnoreCase("1"))
	//								.forEach(readControle -> {
	//
	//									p.step("INICIALIZANDO " + readControle.getNomeRfc(), false);
	//									ToJsonSAP jcoSonReadTeste = new ToJsonSAP(solar.getFunc(readControle.getNomeRfc()));
	//									p.step("PARAMETRIZANDO " + readControle.getNomeRfc(), false);
	//									jcoSonReadControle.setParameters(new HashMap<String, Object>() {
	//										{
	//											put(Constants.I_CONTROLE, controle);
	//										}
	//									});
	//									// TODO: Colocar a lista de Controle para carregar o worker dinamicamente
	//									ZHWOrdemProducaoWorker worker = new ZHWOrdemProducaoWorker(this, solar,
	//											integrationService);
	//									p.step("EXECUTANDO (???????) " + readControle.getNomeRfc(), false);
	//									worker.work(start);
	//
	//								});
	//					} catch (JCoException e) {
	//					}
	//				});
	//
	//	}

}
