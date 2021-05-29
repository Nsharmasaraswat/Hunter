package com.gtp.hunter.process.wf.taskdecorator;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.service.RegisterService;

public class InventoryDecorator extends BaseTaskDecorator {

	public InventoryDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
	}

	@Override
	public String decorateName(Document d) {
		return d.getName();
	}

	@Override
	public String decorateContent(Document d) {
		String content = "";

		switch (d.getModel().getMetaname()) {
			case "APOCONTINV":
				Optional<DocumentField> optDf = d.getFields().stream().filter(df -> df.getField().getMetaname().equals("WAREHOUSE")).findAny();

				content = optDf.isPresent() ? optDf.get().getValue() : d.getSiblings().stream()
								.sorted((Document ds1, Document ds2) -> {
									if (ds1 == null && ds2 == null) return 0;
									if (ds1 == null) return -1;
									if (ds2 == null) return 1;
									DocumentField df1 = ds1.getFields().stream().filter(df -> df.getField().getMetaname().equals("INVADDRESSORDER")).findAny().orElse(null);
									DocumentField df2 = ds2.getFields().stream().filter(df -> df.getField().getMetaname().equals("INVADDRESSORDER")).findAny().orElse(null);

									if ((df1 == null || df1.getValue().isEmpty()) && (df2 == null || df2.getValue().isEmpty())) return ds1.getMetaname().compareTo(ds2.getMetaname());
									if (df1 == null || df1.getValue().isEmpty()) return -1;
									if (df2 == null || df2.getValue().isEmpty()) return 1;
									int v1 = Integer.parseInt(df1.getValue());
									int v2 = Integer.parseInt(df2.getValue());

									return v1 - v2;
								}).filter(ds -> ds.getModel().getMetaname().equals("APORUAINV"))
								.flatMap(ds -> ds.getFields().stream())
								.filter(df -> df.getField().getMetaname().equals("INVADDRESS"))
								.map(df -> getRegSvc().getAddSvc().findById(UUID.fromString(df.getValue())).getName())
								.collect(Collectors.joining(", "));
				break;
			case "INVENTORY":
				content = "Contagens: " + d.getSiblings().size();
				content += "\r\nUsuário: " + d.getUser().getName();
				break;
			default:
				content = d.getName();
		}

		return content;
	}
}