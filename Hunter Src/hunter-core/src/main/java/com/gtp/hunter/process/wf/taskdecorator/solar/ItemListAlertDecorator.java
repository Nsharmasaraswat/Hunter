package com.gtp.hunter.process.wf.taskdecorator.solar;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.model.util.Things;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.wf.taskdecorator.BaseTaskDecorator;

public class ItemListAlertDecorator extends BaseTaskDecorator {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ItemListAlertDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
	}

	@Override
	public String decorateName(Document d) {
		StringBuilder sb = new StringBuilder();

		//Motorista
		if (d.getPerson() != null)
			sb.append(d.getPerson().getName());

		//Caminhao
		if (d.getThings() != null) {
			Optional<Thing> optTruck = d.getThings().stream().map(dt -> dt.getThing()).filter(t -> t.getProduct().getModel().getMetaname().equals("TRUCK")).findAny();

			if (optTruck.isPresent()) {
				Thing truck = optTruck.get();

				sb.append("\r\nSAP: ");
				sb.append(d.getSiblings().parallelStream()
								.flatMap(ds -> ds.getFields().parallelStream())
								.filter(df -> df.getField().getMetaname().equals("TICKET") || df.getField().getMetaname().equals("TRANSPORTE_SAP"))
								.map(df -> df.getValue())
								.distinct()
								.sorted()
								.collect(Collectors.joining(", ")));
				sb.append("\r\n");
				sb.append(truck.getName());
				sb.append("\r\n");
				sb.append(Things.getStringProperty(truck, "CARRIER"));

			} else
				logger.warn("optTruck notPresent: " + d.getCode() + " DTSize: " + d.getThings().size());
		} else
			logger.warn("things null - " + d.getCode());
		return sb.toString().trim();
	}

	@Override
	public String decorateContent(Document d) {
		StringBuilder content = new StringBuilder();
		Optional<Document> optPrdShortage = d.getSiblings().stream().filter(ds -> ds.getModel().getMetaname().equals("PRDSHORTAGE") && ds.getStatus().equals("NOVO")).findAny();
		Supplier<Stream<Document>> supDsib = () -> d.getSiblings().stream();
		String prdIn = supDsib.get().filter(ds -> ds.getModel().getMetaname().equals("NFENTRADA")).flatMap(s -> s.getItems().stream()).map(f -> f.getProduct().getName()).distinct().collect(Collectors.joining(" ; "));

		if (!prdIn.isEmpty()) {
			content.append("- DESCARGA: ");
			content.append(prdIn);
		}
		if (optPrdShortage.isPresent()) {
			Document prdShrt = optPrdShortage.get();
			String prds = prdShrt.getItems().stream().map(f -> f.getProduct().getName() + " (" + (int) f.getQty() + " " + f.getMeasureUnit() + ")").distinct().collect(Collectors.joining(" ; "));

			if (!prdIn.isEmpty()) content.append("\r\n");
			content.append("***PRODUTOS FALTANTES NO ESTOQUE*** ");
			content.append(prds);
		} else {
			String prdOut = supDsib.get().filter(ds -> ds.getModel().getMetaname().equals("NFSAIDA")).flatMap(s -> s.getItems().stream()).map(f -> f.getProduct().getName()).distinct().collect(Collectors.joining(" ; "));

			if (!prdOut.isEmpty()) {
				if (!prdIn.isEmpty()) content.append("\r\n");
				content.append("- CARGA: ");
				content.append(prdOut);
			}
		}
		if (content.length() > 0) {
			content.append("\r\n");
		}
		content.append(Documents.getStringField(d, "OBS"));

		return content.length() == 0 ? "NÃO CONTÉM NOTAS FISCAIS ANEXADAS" : content.toString().trim();
	}
}
