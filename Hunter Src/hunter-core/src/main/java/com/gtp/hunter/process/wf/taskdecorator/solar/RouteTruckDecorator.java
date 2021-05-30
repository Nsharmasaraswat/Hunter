package com.gtp.hunter.process.wf.taskdecorator.solar;

import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.wf.taskdecorator.BaseTaskDecorator;

public class RouteTruckDecorator extends BaseTaskDecorator {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public RouteTruckDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String decorateName(Document d1) {
		Profiler prof = new Profiler("TaskManager");
		StringBuilder sb = new StringBuilder(d1.getName());
		String obs = d1.getFields().stream().filter(df -> df.getField().getMetaname().equals("OBS")).map(df -> df.getValue()).findAny().orElse("");
		String entrega = d1.getSiblings().stream()
						.filter(ds -> ds.getModel().getMetaname().equals("PICKING"))
						.flatMap(ds -> ds.getFields().stream())
						.filter(df -> df.getField().getMetaname().equals("DELIVERY_DATE"))
						.map(df -> df.getValue())
						.distinct()
						.collect(Collectors.joining(","));

		if (!obs.isEmpty()) {
			sb.append('\r');
			sb.append('\n');
			sb.append(obs);
		}
		if (!entrega.isEmpty()) {
			sb.append("\r\n");
			sb.append(entrega);
		}
		prof.done("Name Decorated", false, false).forEach(logger::debug);
		return sb.toString().trim();
	}

	@Override
	public String decorateContent(Document d1) {
		StringBuilder sb = new StringBuilder();
		Profiler prof = new Profiler("TaskManager");
		Supplier<Stream<Document>> supPick = () -> d1.getSiblings().parallelStream()
						.filter(ds -> ds.getModel().getMetaname().equals("PICKING"));

		long fullCount = supPick.get()
						.filter(d -> Boolean.valueOf(d.getFields().parallelStream()
										.filter(df -> df.getField().getMetaname().equals("FULL"))
										.findAny()
										.get()
										.getValue()))
						.count();
		long mixCount = supPick.get().count() - fullCount;
		String ticketMessage = supPick.get()
						.flatMap(ds -> ds.getFields().parallelStream())
						.filter(df -> df.getField().getMetaname().equals("TICKET_MESSAGE"))
						.map(df -> df.getValue().trim())
						.filter(str -> !str.isEmpty())
						.distinct()
						.collect(Collectors.joining(","));

		sb.append("Paletes Cheios: ");
		sb.append(fullCount);
		sb.append("\r\n");
		sb.append("Paletes Mistos: ");
		sb.append(mixCount);
		sb.append("\r\n");
		sb.append("Observação: ");
		sb.append(ticketMessage);
		prof.done("Content Decorated", false, false).forEach(logger::info);
		return sb.toString().trim();
	}
}
