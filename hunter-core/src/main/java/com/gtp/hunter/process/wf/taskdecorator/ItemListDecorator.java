package com.gtp.hunter.process.wf.taskdecorator;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.service.RegisterService;

public class ItemListDecorator extends BaseTaskDecorator {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ItemListDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
	}

	@Override
	public String decorateName(Document d) {
		StringBuilder sb = new StringBuilder(d.getCode() + " - ");

		//Motorista
		if (d.getPerson() != null) {
			sb.append(d.getPerson().getName());
		}

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
								.sorted()
								.collect(Collectors.joining(", ")));
				sb.append("\r\n");
				sb.append(truck.getName());
				sb.append("\r\n");
				sb.append(truck.getProperties().stream()
								.filter(pr -> pr.getField().getMetaname().equals("CARRIER"))
								.map(pr -> pr.getValue())
								.findFirst()
								.orElse(""));
			} else
				logger.warn("optTruck notPresent: " + d.getCode() + " DTSize: " + d.getThings().size());
		} else
			logger.warn("things null - " + d.getCode());

		sb.append("\n");
		return sb.toString();
	}

	@Override
	public String decorateContent(Document d) {
		StringBuilder content = new StringBuilder();
		Supplier<Stream<Document>> supDsib = () -> d.getSiblings().stream();
		String prdIn = supDsib.get().filter(ds -> ds.getModel().getMetaname().equals("NFENTRADA")).flatMap(s -> s.getItems().stream()).map(f -> f.getProduct().getName()).distinct().collect(Collectors.joining(" ; "));
		String prdOut = supDsib.get().filter(ds -> ds.getModel().getMetaname().equals("NFSAIDA")).flatMap(s -> s.getItems().stream()).map(f -> f.getProduct().getName()).distinct().collect(Collectors.joining(" ; "));

		if (!prdIn.isEmpty()) {
			content.append("*Descarga*: ");
			content.append(prdIn);
		}
		if (!prdOut.isEmpty()) {
			if (!prdIn.isEmpty())
				content.append("\r\n");
			content.append("*Carregamento*: ");
			content.append(prdOut);
		}
		return content.length() == 0 ? "NÃO CONTÉM NOTAS FISCAIS ANEXADAS" : content.toString();
	}
}
