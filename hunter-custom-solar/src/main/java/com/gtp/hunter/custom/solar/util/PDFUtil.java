package com.gtp.hunter.custom.solar.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.util.Documents;

public class PDFUtil {

	public static PDDocumentInformation getInfo() {
		PDDocumentInformation info = new PDDocumentInformation();

		info.setAuthor(ConfigUtil.get("hunter-core", "pdf-author", "hunter® IoT Visibility Manager"));
		info.setCreator(ConfigUtil.get("hunter-core", "pdf-author", "hunter® IoT Visibility Manager"));
		info.setCreationDate(Calendar.getInstance());
		info.setModificationDate(Calendar.getInstance());
		return info;
	}

	public static final File createPickMirror(String fileName, List<Document> transps) {

		String filePath = System.getProperty("jboss.server.config.dir") + "/pdf/" + fileName;

		try (PDDocument document = new PDDocument();) {
			PDDocumentInformation info = getInfo();
			Map<String, List<Document>> picksFull = new LinkedHashMap<>();

			transps.sort((tr1, tr2) -> {
				long picks1 = tr1.getSiblings().parallelStream().filter(ds -> ds.getModel().getMetaname().equals("PICKING")).count();
				long picks2 = tr2.getSiblings().parallelStream().filter(ds -> ds.getModel().getMetaname().equals("PICKING")).count();

				if (picks1 == picks2) return tr1.getCode().compareTo(tr2.getCode());
				return (int) (picks1 - picks2);
			});
			for (Document transp : transps) {
				String load = Documents.getStringField(transp, "OBS");
				String sapTransp = transp.getCode().replace("R", "");
				List<Document> picks = transp.getSiblings()
								.parallelStream()
								.filter(ds -> ds.getModel().getMetaname().equals("PICKING"))
								.filter(pk1 -> pk1.getFields().parallelStream().anyMatch(df -> df.getField().getMetaname().equals("FULL") && df.getValue().equalsIgnoreCase("TRUE")))
								.sorted((pk1, pk2) -> {
									return pk1.getCode().compareTo(pk2.getCode());
								}).collect(Collectors.toList());
				picksFull.put(sapTransp + "." + load, transp.getSiblings()
								.parallelStream()
								.filter(ds -> ds.getModel().getMetaname().equals("PICKING"))
								.filter(pk1 -> pk1.getFields().parallelStream().noneMatch(df -> df.getField().getMetaname().equals("FULL") && df.getValue().equalsIgnoreCase("TRUE")))
								.sorted((pk1, pk2) -> {
									return pk1.getCode().compareTo(pk2.getCode());
								}).collect(Collectors.toList()));

				for (Document pick : picks) {
					createPage(document, pick, sapTransp, load);
				}
			}
			for (Entry<String, List<Document>> en : picksFull.entrySet()) {
				String sapTransp = en.getKey().split("\\.")[0];
				String load = en.getKey().split("\\.")[1];

				for (Document pick : en.getValue())
					createPage(document, pick, sapTransp, load);
			}
			info.setTitle("Espelhos de Identificação");
			document.setDocumentInformation(info);
			document.save(filePath);
			document.close();
			return new File(filePath);
		} catch (IOException | ParseException | WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static void createPage(PDDocument document, Document pick, String sapTransp, String load) throws IOException, ParseException, WriterException {
		SimpleDateFormat sdtf = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss");
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
		SimpleDateFormat sdfbc = new SimpleDateFormat("ddMMyyyy");
		SimpleDateFormat sdfbr = new SimpleDateFormat("dd/MM/yyyy");
		Date delivery = sdfbr.parse(Documents.getStringField(pick, "DELIVERY_DATE"));
		String cases = Documents.getStringField(pick, "CASES_PHYSICAL");
		String bayDesc = Documents.getStringField(pick, "BAY_DESC");
		String barcodeText = ConfigUtil.get("hunter-custom-solar", "sap-plant", "CNAT") + sdfbc.format(delivery) + sapTransp + bayDesc + cases;//plant entrega transporte baia casesphysical
		String message = Documents.getStringField(pick, "TICKET_MESSAGE");
		String contId = Documents.getStringField(pick, "CONTAINER_ID", "");
		int contLv = Documents.getIntegerField(pick, "CONTAINER_LEVELS", 0);
		PDPage mirrorPage = new PDPage(PDRectangle.A4);
		PDRectangle pageSize = mirrorPage.getMediaBox();
		float pageWidth = pageSize.getWidth();
		float startX = 30;
		float startY = 20;
		float midX = 400;
		PDPageContentStream cs = new PDPageContentStream(document, mirrorPage);
		PDImageXObject bcImg = JPEGFactory.createFromImage(document, generateCODE128BarcodeImage(barcodeText));
		PDImageXObject qrCode = JPEGFactory.createFromImage(document, generateQRCodeImage(pick.getId().toString()), 1, 600);

		// add the rotation using the current transformation matrix
		// including a translation of pageWidth to use the lower left corner as 0,0 reference
		cs.transform(new Matrix(0, 1, -1, 0, pageWidth, 0));
		cs.drawImage(bcImg, startX + 40, 440);
		cs.drawImage(qrCode, 650, startY);
		cs.beginText();
		cs.setLeading(10f);
		cs.setFont(PDType1Font.COURIER, 12);
		cs.newLineAtOffset(startX, startY);
		cs.showText("DOCUMENTO DE USO INTERNO");
		cs.newLineAtOffset(0, 100);
		cs.setFont(PDType1Font.COURIER_BOLD, 22);
		cs.showText(message + (!contId.isEmpty() ? contId + ": " + contLv + " Níveis" : ""));
		cs.newLineAtOffset(-startX + 10, 100);
		cs.setFont(PDType1Font.HELVETICA_BOLD, bayDesc.length() > 2 ? 125 : 140);
		cs.showText(load.replace("Carga: ", ""));
		cs.showText(".");
		cs.showText(bayDesc);
		cs.newLineAtOffset(startX + 80, 200);
		cs.setFont(PDType1Font.COURIER, 10);
		cs.showText(barcodeText);
		cs.newLineAtOffset(midX - 80, 30);
		cs.setFont(PDType1Font.COURIER, 30);
		cs.showText("TRANSPORTE " + sapTransp);
		cs.setFont(PDType1Font.COURIER, 16);
		cs.newLineAtOffset(-midX, 60);
		cs.showText("DOCUMENTO DE ACOMPANHAMENTO DE PALETE");
		cs.newLineAtOffset(midX, 0);
		cs.showText(load);
		cs.showText(" ENTREGA DE: ");
		cs.showText(sdf.format(delivery));
		cs.newLineAtOffset(-midX, 20);
		cs.setFont(PDType1Font.COURIER, 16);
		cs.showText("SOLAR");
		cs.newLineAtOffset(midX, 0);
		cs.showText("IMPRESSO: " + sdtf.format(Calendar.getInstance().getTime()));
		cs.endText();
		cs.close();
		mirrorPage.setRotation(90);
		document.addPage(mirrorPage);
	}

	public static BufferedImage generateCODE128BarcodeImage(String barcodeText) throws WriterException {
		Code128Writer barcodeWriter = new Code128Writer();
		BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.CODE_128, 250, 40);

		return MatrixToImageWriter.toBufferedImage(bitMatrix);
	}

	public static BufferedImage generateQRCodeImage(String barcodeText) throws WriterException {
		QRCodeWriter barcodeWriter = new QRCodeWriter();
		BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 120, 120);

		return MatrixToImageWriter.toBufferedImage(bitMatrix);
	}
}
