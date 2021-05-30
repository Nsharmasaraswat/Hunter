package com.gtp.hunter.process.wf.taskdecorator;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.service.RegisterService;

public class DocProductDecorator extends BaseTaskDecorator {

	public DocProductDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
	}

	@Override
	public String decorateName(Document d) {
		StringBuilder sb = new StringBuilder(d.getName());

		return sb.toString();
	}

	@Override
	public String decorateContent(Document d) {
		StringBuilder sb = new StringBuilder("");
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.forLanguageTag("pt-BR"));
		DecimalFormat df = new DecimalFormat("0.0000", symbols);
		Map<Product, Prod> prdMap = new HashMap<>();

		df.setRoundingMode(RoundingMode.FLOOR);
		for (DocumentItem di : d.getItems()) {
			Prod p = null;

			if (prdMap.containsKey(di.getProduct())) {
				p = prdMap.get(di.getProduct());
				p.qty += di.getQty();
			} else {
				p = new Prod();
				p.qty = di.getQty();
				p.um = di.getMeasureUnit() == null || di.getMeasureUnit().isEmpty() ? "" : " " + di.getMeasureUnit();
			}
			prdMap.put(di.getProduct(), p);
		}

		for (Product p : prdMap.keySet()) {
			Prod prd = prdMap.get(p);

			sb.append(p.getSku());
			sb.append(" - ");
			sb.append(p.getName());
			sb.append(" (");
			sb.append(df.format(prd.qty));
			sb.append(prd.um);
			sb.append(") / ");
		}
		if (sb.length() > 3)
			sb.delete(sb.length() - 3, sb.length());
		return sb.toString();
	}

	private class Prod {
		double	qty;
		String	um;
	}

}
