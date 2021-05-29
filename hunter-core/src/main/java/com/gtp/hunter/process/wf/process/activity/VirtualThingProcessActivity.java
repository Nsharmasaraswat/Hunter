package com.gtp.hunter.process.wf.process.activity;

import java.util.UUID;

import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.process.model.ProcessActivity;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.process.BaseProcess;

public class VirtualThingProcessActivity extends BaseProcessActivity<ComplexData, Thing> {

	private String[] params;
	
	public VirtualThingProcessActivity(ProcessActivity model, BaseOrigin origin) {
		super(model, origin);
		this.params = model.getParam().split(",");
	}

	@Override
	public ProcessActivityExecuteReturn executePostConstruct() {
		return null;
	}

	@Override
	public ProcessActivityExecuteReturn executePreTransform(ComplexData arg) {
		return null;
	}

	@Override
	public ProcessActivityExecuteReturn execute(Thing arg) {
		return null;
	}

	@Override
	public ProcessActivityExecuteReturn execute(BaseProcess p) {
		return null;
	}

	@Override
	public Thing executeUnknown(ComplexData arg) {
		Thing t = new Thing("",new Product("ITEM DESCONHECIDO",null,"","CANCELADO"),null,this.params[0]);
		try {
			StringBuilder sb = new StringBuilder(arg.getTagId());
			sb.insert(8, "-");
			sb.insert(12, "-");
			sb.insert(16, "-");
			sb.insert(20, "-");
			t.setId(UUID.fromString(sb.toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		t.getUnitModel().add(new Unit(arg.getTagId(),null)); 
		return t;
	}

	@Override
	public ProcessActivityExecuteReturn execute() {
		// TODO Auto-generated method stub
		return null;
	}

}
