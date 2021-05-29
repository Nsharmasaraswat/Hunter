/**
 * 
 */
package com.gtp.hunter.process.wf.process.solar;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.Product;

/**
 * @author t_mtormin
 *
 */
public interface ProductionLineInterface {

	boolean resetCounters();

	boolean startProduction(Document ordProd);

	boolean stopProduction();

	Document getProductionOrder();

	Document recordProduction(Product prd, double quantity, boolean modifyLast);

}
