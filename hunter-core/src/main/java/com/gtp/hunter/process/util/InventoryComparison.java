package com.gtp.hunter.process.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.Product;

public class InventoryComparison {

	private Document		doc1;
	private Document		doc2;
	private List<UUID>		addressDivIds;
	private List<Document>	sibOk;
	private boolean			success;

	public InventoryComparison(Document doc1, Document doc2) {
		addressDivIds = new ArrayList<>();
		sibOk = new ArrayList<>();
		if (doc2.getCode().compareTo(doc1.getCode()) >= 0) {
			this.doc1 = doc1;
			this.doc2 = doc2;
		} else {
			this.doc1 = doc2;
			this.doc2 = doc1;
		}
	}

	public List<Document> getSibsOk() {
		return sibOk;
	}

	public InventoryComparison compareAdd() {
		final List<Document> apoRuas1 = doc1.getSiblings().stream().filter(ds -> ds.getModel().getMetaname().equals("APORUAINV")).collect(Collectors.toList());
		final List<Document> apoRuas2 = doc2.getSiblings().stream().filter(ds -> ds.getModel().getMetaname().equals("APORUAINV")).collect(Collectors.toList());
		final Set<UUID> ruasId = Stream.concat(apoRuas1.parallelStream(), apoRuas2.parallelStream())
						.flatMap(d -> d.getFields().stream())
						.filter(df -> df.getField().getMetaname().equals("INVADDRESS") && df.getValue() != null && !df.getValue().isEmpty())
						.map(df -> UUID.fromString(df.getValue()))
						.collect(Collectors.toSet());

		for (UUID ruaId : ruasId) {
			final Document ar1 = apoRuas1.parallelStream().flatMap(d -> d.getFields().stream())
							.filter(df -> df.getField().getMetaname().equals("INVADDRESS") && df.getValue().equals(ruaId.toString()))
							.map(df -> df.getDocument())
							.findAny()
							.orElse(null);
			final Document ar2 = apoRuas2.parallelStream().flatMap(d -> d.getFields().stream())
							.filter(df -> df.getField().getMetaname().equals("INVADDRESS") && df.getValue().equals(ruaId.toString()))
							.map(df -> df.getDocument())
							.findAny()
							.orElse(null);
			if (ar1 == null || ar2 == null) {//Um dos dois nao existe significa que mais pra frente tem dois documentos compativeis
				addressDivIds.add(ruaId);
			} else if (ar1.getItems().isEmpty() && ar2.getItems().isEmpty()) {//os dois existem e nao tem itens, significa rua vazia sibok
				sibOk.add(ar1);
				addressDivIds.remove(ruaId);
			} else if ((ar1.getItems().isEmpty() || ar2.getItems().isEmpty()) && !sibOk.parallelStream().anyMatch(ds -> ds.getId().equals(ar1.getId()))) {
				addressDivIds.add(ruaId);
			} else {
				Map<Product, List<DocumentItem>> di1Map = ar1.getItems().parallelStream().collect(Collectors.groupingBy(DocumentItem::getProduct));
				Map<Product, List<DocumentItem>> di2Map = ar2.getItems().parallelStream().collect(Collectors.groupingBy(DocumentItem::getProduct));

				if (di1Map.keySet().containsAll(di2Map.keySet()) && di2Map.keySet().containsAll(di1Map.keySet())) {//Todos os produtos estão contidos nos dois documentos
					double di1Qty = di1Map.values().parallelStream().flatMap(diL -> diL.parallelStream()).mapToDouble(di -> di.getQty()).sum();
					double di2Qty = di2Map.values().parallelStream().flatMap(diL -> diL.parallelStream()).mapToDouble(di -> di.getQty()).sum();

					if (di1Qty == di2Qty) {
						sibOk.add(ar1);
						addressDivIds.remove(ruaId);
					} else if (!sibOk.parallelStream().anyMatch(ds -> ds.getId().equals(ar1.getId()))) {
						addressDivIds.add(ruaId);
					}
				} else if (!sibOk.parallelStream().anyMatch(ds -> ds.getId().equals(ar1.getId()))) {
					addressDivIds.add(ruaId);
				}
			}
		}
		success = addressDivIds.isEmpty();
		return this;
	}

	public Document getDoc1() {
		return this.doc1;
	}

	public Document getDoc2() {
		return this.doc2;
	}

	public List<UUID> getAddressDivergence() {
		return this.addressDivIds;
	}

	public int diffSize() {
		return this.addressDivIds.size();
	}

	public boolean isSuccess() {
		return success;
	}
}
