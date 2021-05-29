package com.gtp.hunter.custom.solar.sap.worker;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.common.enums.FieldType;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.dtos.ReadFieldsSap;
import com.gtp.hunter.custom.solar.sap.dtos.SAPCustomerDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPDocumentDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPDocumentItemDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPProductDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPProductPropertyDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReadStartDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPSupplierDTO;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.custom.solar.util.Constants;
import com.gtp.hunter.custom.solar.util.ToJsonSAP;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.Person;
import com.gtp.hunter.process.model.PersonField;
import com.gtp.hunter.process.model.PersonModel;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.ProductField;
import com.gtp.hunter.process.model.ProductModel;
import com.gtp.hunter.process.model.ProductModelField;
import com.sap.conn.jco.JCoException;

public abstract class BaseWorker {

	protected static final int			TKNUM_LENGTH			= 10;

	private static Map<String, String>	methodConf				= new HashMap<String, String>() {
																	private static final long serialVersionUID = 3339161720798934284L;

																	{
																		put("BRAND", "getMvGr4");
																		put("CATEGORY", "getMvGr1");
																		put("COMPLEMENT_FLAVOR", "getMvGr5");
																		put("DUN", "getDun");
																		put("EAN", "getEan");
																		put("FLAVOR", "getMvGr5");
																		put("GROSS_WEIGHT", "getBrGew");
																		put("GROUP_UM", "getBsTme");
																		put("MATERIAL_GROUP", "getMvGr2");
																		put("NET_WEIGHT", "getNtGew");
																		put("PACKING_TYPE", "getMeins");
																		put("SIZE", "getMvGr3");
																		put("KIT", "getKit");
																		put("KIT_QUANTITY", "getKitQuantity");
																	}
																};

	private static Map<String, String>	personFieldmethodConf	= new HashMap<String, String>() {
																	private static final long serialVersionUID = 3339161720798934284L;

																	{
																		put("CNPJ", "getStcd1");
																		put("CPF", "getStcd2");
																		put("REGION", "getRegio");
																		put("LOCATION", "getOrt01");
																		put("CITY", "getCity1");
																		put("ACCOUNT_GROUP", "getKtokd");
																		put("CLIENT_CLASS", "getKukla");
																		put("KEY_ACCOUNT", "getBran1");
																		put("ACTIVITY", "getKatr1");
																		put("SOLAR_MARKET", "getKatr6");
																		put("SUB_CHANNEL", "getKatr7");
																		put("SALES_ROUTE", "getRpmkr");
																		put("CLUSTER", "getKatr10");
																		put("SECTOR_TYPE", "CLIENTE");
																		put("INDUSTRIAL_SECTOR", "getKtokk");
																		put("ACCOUNT_NUMBER", "getLifnr");
																		put("CCRM", "");
																		put("LOT_QUANTITY", "");
																		put("BATCH_VALIDITY", "");
																		put("DELIVERY_PRIORITY", "");
																		put("SHIPPING_CONDITION", "");
																	}
																};

	private SAPSolar					solar;
	private IntegrationService			iSvc;
	private Gson						gson					= new GsonBuilder().create();

	public BaseWorker(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		//		this.svc = svc;
		this.solar = solar;
		this.iSvc = integrationService;
	}

	public abstract boolean work(SAPReadStartDTO rstart);

	public abstract boolean external(Object obj);

	public SAPSolar getSolar() {
		return solar;
	}

	public IntegrationService getISvc() {
		return iSvc;
	}

	public Gson getGson() {
		return gson;
	}

	protected void logJcoError(ReadFieldsSap readFieldsSap, String code, String rfc) {
		if (readFieldsSap.getJcoError() != null && !readFieldsSap.getJcoError().isEmpty()) {
			StringBuilder jcoError = new StringBuilder();

			for (Entry<String, Object> en : readFieldsSap.getJcoError().entrySet()) {
				jcoError.append(en.getKey());
				jcoError.append(" - ");
				jcoError.append(en.getValue());
			}
			getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, code, rfc, jcoError.toString()));
		}
	}

	protected Person registerSupplier(SAPSupplierDTO supplier) {
		PersonModel perm = getISvc().getRegSvc().getPsmSvc().findByMetaname("SUPPLIER");
		// Carrega Fornecedor
		Person ps = getISvc().getRegSvc().getPsSvc().findByCode(supplier.getStcd1());
		final Person person = ps == null ? new Person(supplier.getName1(), perm, supplier.getStcd1(), "ATIVO") : ps;

		getISvc().getRegSvc().getPsSvc().persist(person);
		perm.getFields().forEach(prmf -> {
			String value = "";
			String prmfMN = prmf.getMetaname();
			String methodNameSAP = personFieldmethodConf.get(prmfMN);
			Optional<PersonField> optPF = person.getFields().stream().filter(psf -> psf.getField().getId().equals(prmf.getId())).findFirst();

			if (methodNameSAP.startsWith("get")) {
				Optional<Method> optMethod = Stream.of(supplier.getClass().getDeclaredMethods()).filter(prm -> prm.getName().equals(methodNameSAP)).findFirst();

				try {
					if (optMethod.isPresent()) {
						value = (String) optMethod.get().invoke(supplier);
						if (prmf.getType().equals(FieldType.BOOLEAN))
							value = Boolean.toString(value.equalsIgnoreCase("X"));
						else if (prmf.getType().equals(FieldType.NUMBER)) value = value.replace(",", ".");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else
				value = methodNameSAP;//campos que nao vem do SAP tem valor fixo definido no mapa

			PersonField psf = new PersonField(person, prmf, value);
			if (optPF.isPresent()) {
				psf = optPF.get();

				if ((psf.getValue() == null || psf.getValue().isEmpty()) && value != null && !value.isEmpty() && value != methodNameSAP) psf.setValue(value);
			} else
				psf.setStatus("NOVO");
			getISvc().getRegSvc().getPsfSvc().persist(psf);
			person.getFields().add(psf);
		});

		getISvc().getRegSvc().getPsSvc().persist(person);
		getISvc().getRegSvc().getAglSvc().sendSupplierToWMS(person, ps == null ? "POST" : "PUT");
		return person;
	}

	protected Person registerCustomer(SAPCustomerDTO customer) {
		// Carrega Clientes
		PersonModel perm = getISvc().getRegSvc().getPsmSvc().findByMetaname("CUSTOMER");
		Person ps = getISvc().getRegSvc().getPsSvc().findByCode(customer.getKunnr());
		final Person person = ps == null ? new Person(customer.getName1(), perm, customer.getKunnr(), "ATIVO") : ps;

		getISvc().getRegSvc().getPsSvc().persist(person);
		perm.getFields().forEach(prmf -> {
			String value = "";
			String prmfMN = prmf.getMetaname();
			String methodNameSAP = personFieldmethodConf.get(prmfMN);
			Optional<PersonField> optPF = person.getFields().stream().filter(psf -> psf.getField().getId().equals(prmf.getId())).findFirst();

			if (methodNameSAP.startsWith("get")) {
				Optional<Method> optMethod = Stream.of(customer.getClass().getDeclaredMethods()).filter(prm -> prm.getName().equals(methodNameSAP)).findFirst();

				try {
					if (optMethod.isPresent()) {
						value = (String) optMethod.get().invoke(customer);
						if (prmf.getType().equals(FieldType.BOOLEAN))
							value = Boolean.toString(value.equalsIgnoreCase("X"));
						else if (prmf.getType().equals(FieldType.NUMBER)) value = value.replace(",", ".");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else
				value = methodNameSAP;//campos que nao vem do SAP tem valor fixo definido no mapa

			PersonField psf = new PersonField(person, prmf, value);
			if (optPF.isPresent()) {
				psf = optPF.get();

				if ((psf.getValue() == null || psf.getValue().isEmpty()) && value != null && !value.isEmpty() && value != methodNameSAP) psf.setValue(value);
			} else
				psf.setStatus("NOVO");
			getISvc().getRegSvc().getPsfSvc().persist(psf);
			person.getFields().add(psf);
		});

		getISvc().getRegSvc().getPsSvc().persist(person);
		getISvc().getRegSvc().getAglSvc().sendCustomerToWMS(person, ps == null ? "POST" : "PUT");
		return person;
	}

	protected void registerProducts(List<SAPProductDTO> produtos) {
		List<ProductModel> modelBase = getISvc().getRegSvc().getPmSvc().listAll();
		HashMap<String, ProductModel> chaves = new HashMap<>();

		for (ProductModel modelsNaBase : modelBase) {
			chaves.put(modelsNaBase.getMetaname(), modelsNaBase);
		}

		for (SAPProductDTO prod : produtos) {
			if (prod.getMvGr1() != null && !prod.getMvGr1().isEmpty()) {
				String mvGr1 = prod.getMvGr1();
				ProductModel productModel = null;

				if (!chaves.containsKey(mvGr1)) {
					//TODO ira realizar o cadastro do productModel
					productModel = new ProductModel(mvGr1, mvGr1, null, "INTEGRADO");
					productModel.getFields().add(new ProductModelField("DUN", "DUN", productModel, FieldType.TEXT, "ATIVO", 0));
					productModel.getFields().add(new ProductModelField("Quantidade para Ressuprimento", "RESUPPLY_QUANTITY", productModel, FieldType.TEXT, "ATIVO", 1));
					productModel.getFields().add(new ProductModelField("Grupo de Material", "MATERIAL_GROUP", productModel, FieldType.TEXT, "ATIVO", 2));
					productModel.getFields().add(new ProductModelField("EAN", "EAN", productModel, FieldType.TEXT, "ATIVO", 3));
					productModel.getFields().add(new ProductModelField("Categoria", "CATEGORY", productModel, FieldType.TEXT, "ATIVO", 4));
					productModel.getFields().add(new ProductModelField("Caixas/Palete", "PALLET_BOX", productModel, FieldType.NUMBER, "ATIVO", 5));
					productModel.getFields().add(new ProductModelField("Transferencia MP", "TRANSFER_MP", productModel, FieldType.BOOLEAN, "ATIVO", 6));
					productModel.getFields().add(new ProductModelField("Unidade de Medida", "GROUP_UM", productModel, FieldType.TEXT, "ATIVO", 7));
					productModel.getFields().add(new ProductModelField("Sabor", "FLAVOR", productModel, FieldType.TEXT, "ATIVO", 8));
					productModel.getFields().add(new ProductModelField("Marca", "BRAND", productModel, FieldType.TEXT, "ATIVO", 9));
					productModel.getFields().add(new ProductModelField("Camadas/Palete", "PALLET_LAYER", productModel, FieldType.NUMBER, "ATIVO", 10));
					productModel.getFields().add(new ProductModelField("Curva ABC", "CURVE_ABC", productModel, FieldType.TEXT, "ATIVO", 11));
					productModel.getFields().add(new ProductModelField("Variação do Peso", "VAR_WEIGHT", productModel, FieldType.NUMBER, "ATIVO", 12));
					productModel.getFields().add(new ProductModelField("Peso Bruto", "GROSS_WEIGHT", productModel, FieldType.NUMBER, "ATIVO", 13));
					productModel.getFields().add(new ProductModelField("Tamanho", "SIZE", productModel, FieldType.TEXT, "ATIVO", 14));
					productModel.getFields().add(new ProductModelField("Peso Líquido", "NET_WEIGHT", productModel, FieldType.NUMBER, "ATIVO", 15));
					productModel.getFields().add(new ProductModelField("Alergenico", "ALLERGENIC", productModel, FieldType.TEXT, "ATIVO", 16));
					productModel.getFields().add(new ProductModelField("Caixas/Camada", "BOX_LAYER", productModel, FieldType.NUMBER, "ATIVO", 17));
					productModel.getFields().add(new ProductModelField("Tipo de Embalagem", "PACKING_TYPE", productModel, FieldType.TEXT, "ATIVO", 18));
					productModel.getFields().add(new ProductModelField("Sabor Complementar", "COMPLEMENT_FLAVOR", productModel, FieldType.TEXT, "ATIVO", 19));
					productModel.getFields().add(new ProductModelField("Kit", "KIT", productModel, FieldType.BOOLEAN, "ATIVO", 20));
					productModel.getFields().add(new ProductModelField("Quantidade no Kit", "KIT_QUANTITY", productModel, FieldType.NUMBER, "ATIVO", 21));
					productModel.getFields().add(new ProductModelField("Unidades/Caixa", "UNIT_BOX", productModel, FieldType.NUMBER, "ATIVO", 22));
					productModel.getFields().add(new ProductModelField("Vida útil", "SHELFLIFE", productModel, FieldType.NUMBER, "ATIVO", 23));
					productModel.getFields().add(new ProductModelField("Altura do Palete", "PALLET_HEIGHT", productModel, FieldType.NUMBER, "ATIVO", 24));
					productModel.getFields().add(new ProductModelField("ID da Embalagem", "PACKAGE_ID", productModel, FieldType.TEXT, "ATIVO", 25));
					productModel.getFields().add(new ProductModelField("ID do Vasilhame", "CASK_ID", productModel, FieldType.TEXT, "ATIVO", 26));

					getISvc().getRegSvc().getPmSvc().persist(productModel);
					chaves.put(mvGr1, productModel);
				} else {
					productModel = chaves.get(mvGr1);
				}
				final String sku = String.valueOf(Integer.parseInt(prod.getMaterial()));
				Product tmp = getISvc().getRegSvc().getPrdSvc().findBySKU(sku);
				final Product p = tmp == null ? new Product(prod.getMakTx(), productModel, sku, "INTEGRADO") : tmp;

				if (!p.getModel().getId().equals(productModel.getId()))
					p.setModel(productModel);
				productModel.getFields().forEach(pmf -> {
					String value = "";
					String methodNameSAP = methodConf.get(pmf.getMetaname());
					Optional<ProductField> optPF = p.getFields().parallelStream().filter(pf -> pf.getModel().getMetaname().equals(pmf.getMetaname())).findAny();
					Optional<Method> optMethod = Stream.of(prod.getClass().getDeclaredMethods()).filter(dm -> dm.getName().equals(methodNameSAP)).findAny();

					try {
						if (optMethod.isPresent()) {
							value = (String) optMethod.get().invoke(prod);
							if (pmf.getType().equals(FieldType.BOOLEAN))
								value = Boolean.toString(value.equalsIgnoreCase("X"));
							else if (pmf.getType().equals(FieldType.NUMBER)) value = value.replace(",", ".");
						}
					} catch (Exception e) {
						switch (pmf.getType()) {
							case BOOLEAN:
								value = Boolean.toString(false);
								break;
							case CHAR:
							case TEXT:
							case DATE:
							case COMBO:
								value = "";
								break;
							case NUMBER:
								value = null;
								break;
							default:
								break;
						}
					}
					if (optPF.isPresent()) {
						ProductField pf = optPF.get();

						if (!pf.getModel().getId().equals(pmf.getId()))
							pf.setModel(pmf);

						if (value != null && !value.isEmpty() && !pf.getValue().equals(value)) {
							pf.setValue(value);
							getISvc().getRegSvc().getPfSvc().persist(pf);
						}
					} else {
						p.getFields().add(new ProductField(p, pmf, "NOVO", value == null ? "" : value));
					}
				});
				if (ConfigUtil.get("hunter-custom-solar", "product-kit-enabled", "FALSE").equalsIgnoreCase("TRUE") && prod.getParent_sku() != null && !prod.getParent_sku().isEmpty()) {
					String parentSku = String.valueOf(Integer.parseInt(prod.getParent_sku()));

					if (!parentSku.equals(sku)) {
						Product parent = getISvc().getRegSvc().getPrdSvc().findBySKU(parentSku);

						if (parent != null)
							p.setParent(parent);
					}
				}
				asyncUpdateWMS(getISvc().getRegSvc().getPrdSvc().persist(p));
			}
		}
	}

	protected Set<DocumentItem> getDocumentItems(List<SAPDocumentItemDTO> sapDocItemList, List<SAPProductPropertyDTO> sapPrdPropList, SAPDocumentDTO doc, int code, String controle) {
		Set<DocumentItem> ret = new HashSet<>();

		for (SAPDocumentItemDTO item : sapDocItemList) {
			if (item.getMaterial() == null || item.getMaterial().isEmpty()) {
				this.callResult(Constants.RFC_INFORMACAO, code, controle, "ICW_0003", "NFLIN com material vazio");
				//				getIntegrationService().getMail().sendmail(new String[] { "mateus@gtpautomation.com" }, new String[] {}, new String[] {}, "NFLIN Com Material Vazio", String.format("Número NF: %s - Item NF: %s", item.getNumeroNf(), item.getItemNf()));
				continue;
			}

			Product prod = null;
			try {
				prod = getISvc().getRegSvc().getPrdSvc().findBySKU(String.valueOf(Integer.parseInt(item.getMaterial())));
			} catch (NumberFormatException e) {
				this.callResult(Constants.RFC_INFORMACAO, code, controle, "ICW_0004", "PRODUTO COM MATERIAL INVÁLIDO " + item.getMaterial());
				continue;
			}

			if (prod == null) {
				this.callResult(Constants.RFC_INFORMACAO, code, controle, "ICW_0005", "NF COM MATERIAL NÃO DESCRITO " + item.getMaterial());
				continue;
			}

			DocumentItem documentItem = new DocumentItem();
			documentItem.setQty(Double.parseDouble(item.getQuantidade()));
			documentItem.setName(prod.getSku());
			documentItem.setStatus("NOVO");
			documentItem.setProduct(prod);
			documentItem.setMeasureUnit(item.getUnidMed());
			documentItem.getProperties().put("DOC_COMPRAS", item.getDocCompras());
			documentItem.getProperties().put("CFOP", item.getCfop());

			//			if ((doc.getDirecaoNf() == null || !doc.getDirecaoNf().equals("2")) && doc.getVsTel() != null && !doc.getVsTel().isEmpty() && !doc.getVsTel().equalsIgnoreCase(item.getCentro())) {
			//				RealpickingProductDTO rpProd = getISvc().getrpSvc().findByPlantAndSku(doc.getVsTel(), prod.getSku());
			//
			//				if (rpProd != null)
			//					documentItem.getProperties().put("PALLET_BOX", String.valueOf(rpProd.getQuantity_standard()));
			//			}

			Optional<SAPProductPropertyDTO> optPrdProp = sapPrdPropList.stream().filter(pp -> pp.getMatNr().equals(item.getMaterial()) && pp.getControle().equals(item.getControle())).findAny();
			if (optPrdProp.isPresent()) {
				SAPProductPropertyDTO prop = optPrdProp.get();
				String mult = prop.getUmRez();
				String div = prop.getUmRen();

				documentItem.getProperties().put("DENOMINADOR", div.isEmpty() || div.equals("0") ? "1" : div);
				documentItem.getProperties().put("FATOR_MULTIPLICATIVO", mult.isEmpty() || mult.equals("0") ? "1" : mult);
			}
			ret.add(documentItem);
		}
		return ret;
	}

	protected boolean callResult(String rfc, Integer code, String controle, String code_msg, String msg) {
		if (ConfigUtil.get("hunter-custom-solar", "exec_prf_interface", "false").equalsIgnoreCase("true") && !code_msg.equals("ICW_0099")) {
			ToJsonSAP jcoSonStart = new ToJsonSAP(getSolar().getFunc("Z_HW_PRF_INTERFACE"));
			jcoSonStart.setParameters(new HashMap<String, Object>() {
				private static final long serialVersionUID = 7885960940490561385L;

				{
					put(Constants.CODE, code);
					put(Constants.CONTROLE, controle);
					put(Constants.CODE_MSG, code_msg);
					put(Constants.MSG, msg);
					put(Constants.ORIGEM, "H");
				}
			});

			try {
				jcoSonStart.execute(getSolar().getDestination());
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			} catch (JCoException e) {
				e.printStackTrace();
			}
		}
		iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, code_msg.equals("ICW_0000") ? AlertSeverity.INFO : AlertSeverity.ERROR, controle, rfc, msg));
		return true;
	}

	private void asyncUpdateWMS(Product p) {
		Executors.newSingleThreadScheduledExecutor().schedule(() -> {
			Product updated = getISvc().getRpSvc().update(p);

			if (updated != null) {
				try {
					IntegrationReturn iRet = getISvc().getRegSvc().getAglSvc().sendProductToWMS(updated, "POST").get();

					if (!iRet.isResult() && iRet.getMessage().contains("existe o registro")) {
						getISvc().getRegSvc().getAglSvc().sendProductToWMS(updated, "PUT");
					}
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		}, 10, TimeUnit.SECONDS);
	}
}
