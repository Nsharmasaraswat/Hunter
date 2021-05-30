package com.gtp.hunter.core.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.gtp.hunter.common.model.RawData.RawDataType;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.repository.RawDataRepository;

@WebServlet("/teste")
public class TesteServlet extends HttpServlet{
	
	private static final long serialVersionUID = 7054186769244501240L;
	
	@Inject
	private RawDataRepository rawRep;

	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
		ComplexData r = new ComplexData();
		r.setSource(UUID.randomUUID());
		r.setDevice(UUID.randomUUID());
		r.setPort(1);
		r.setTagId("1");
		r.setTs(new Date().getTime());
		r.setPayload("EUSEINADAR");
		r.setType(RawDataType.IDENT);
		Gson g = new Gson();
		
		response.getWriter().print(g.toJson(r));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        response.getWriter().print("my POST");
    }
}
