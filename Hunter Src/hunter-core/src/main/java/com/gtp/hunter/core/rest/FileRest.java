package com.gtp.hunter.core.rest;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.annotation.MultipartConfig;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.common.model.RawData.RawDataType;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.service.RawDataService;

@RequestScoped
@Path("/file")
@MultipartConfig(location = "configuration/upload")
public class FileRest {

	private final String			UPLOADED_FILE_PATH	= System.getProperty("jboss.server.config.dir") + "/upload/";

	@Resource
	private ManagedExecutorService	mes;

	@Inject
	private Logger					logger;

	@Inject
	private RawDataService			rdSvc;

	@POST
	@Path("/upload")
	@PermitAll
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(MultipartFormDataInput input) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String dataDir = UPLOADED_FILE_PATH + "/data/" + sdf.format(new Date()) + "/";
		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		List<InputPart> inputParts = uploadForm.get("uploadedFile");
		String fileName = "";

		try {
			java.nio.file.Path p = Paths.get(dataDir);
			if (!false)
				Files.createDirectories(p);
			for (InputPart inputPart : inputParts) {
				MultivaluedMap<String, String> header = inputPart.getHeaders();
				fileName = getFileName(header);
				//convert the uploaded file to inputstream
				InputStream inputStream = inputPart.getBody(InputStream.class, null);
				byte[] bytes = IOUtils.toByteArray(inputStream);
				//constructs upload file path
				fileName = dataDir + fileName;
				writeFile(bytes, fileName);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		//TODO: PIX4D Process - remove this and put a process
		runPix4dProcess(dataDir);
		return Response.status(200)
						.entity("uploadFile is called, Uploaded file name : " + fileName).build();
	}

	@POST
	@Path("/cambuhy")
	@PermitAll
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadLocFile(MultipartFormDataInput input) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String dataDir = UPLOADED_FILE_PATH + "/data/" + sdf.format(new Date()) + "/";
		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		List<InputPart> inputParts = uploadForm.get("uploadedFile");
		String fileName = "";

		try {
			java.nio.file.Path p = Paths.get(dataDir);
			if (!false)
				Files.createDirectories(p);
			logger.info("Parts: " + inputParts.size());
			for (InputPart inputPart : inputParts) {
				MultivaluedMap<String, String> header = inputPart.getHeaders();

				fileName = getFileName(header);
				//convert the uploaded file to inputstream
				InputStream inputStream = inputPart.getBody(InputStream.class, null);
				byte[] bytes = IOUtils.toByteArray(inputStream);
				//constructs upload file path
				fileName = dataDir + fileName;
				writeFile(bytes, fileName);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileInputStream inputStream = null;
		Scanner sc = null;
		int linha = 1;
		try {
			inputStream = new FileInputStream(fileName);
			sc = new Scanner(inputStream, "UTF-8");
			logger.info("HEADER: " + sc.nextLine());

			while (sc.hasNextLine()) {
				String line = sc.nextLine();

				logger.info("Line " + ++linha);
				processLocLine(line.replace("\r", "").replace("\n", ""));
			}

			// note that Scanner suppresses exceptions
			if (sc.ioException() != null) {
				throw sc.ioException();
			}
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (FileNotFoundException fnfe) {
			logger.error(fnfe.getLocalizedMessage());
		} catch (IOException ioe) {
			logger.error(ioe.getLocalizedMessage());
		} finally {
			if (sc != null) {
				sc.close();
			}
		}
		return Response.status(200)
						.entity("uploadFile is called, Uploaded file name : " + fileName).build();
	}

	//TODO: REMOVE
	private void runPix4dProcess(final String dataDir) {
		mes.execute(() -> {
			try {
				StringBuilder sb = new StringBuilder();
				Socket s = new Socket("localhost", 5000);
				DataOutputStream dout = new DataOutputStream(s.getOutputStream());
				DataInputStream din = new DataInputStream(s.getInputStream());
				String message = new File(dataDir).getAbsolutePath();

				dout.writeUTF(message);
				dout.flush();
				while (din.available() <= 0)
					Thread.sleep(5000);
				while (din.available() > 0) {
					byte[] buff = new byte[din.available()];

					din.readFully(buff);
					sb.append(new String(buff));
				}
				System.out.print(sb.toString());
				dout.close();
				din.close();
				s.close();
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	/**
	 * header sample
	 * {
	 * 	Content-Type=[image/png], 
	 * 	Content-Disposition=[form-data; name="file"; filename="filename.extension"]
	 * }
	 **/
	//get uploaded filename, is there a easy way in RESTEasy?
	private String getFileName(MultivaluedMap<String, String> header) {
		for (Entry<String, List<String>> es : header.entrySet())
			logger.info("Header " + es.getKey() + ": " + es.getValue().stream().collect(Collectors.joining(" ; ")));
		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

		for (String filename : contentDisposition) {
			if ((filename.trim().startsWith("filename"))) {
				String[] name = filename.split("=");
				String finalFileName = name[1].trim().replaceAll("\"", "");

				return finalFileName;
			}
		}
		return "unknown";
	}

	//save to somewhere
	private void writeFile(byte[] content, String filename) throws IOException {
		File file = new File(filename);

		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream fop = new FileOutputStream(file);

		fop.write(content);
		fop.flush();
		fop.close();
	}

	/**
	 * @author t_mtormin
	 *
	 */
	private class CambuhyPayload {
		@Expose
		private String	tagId;

		@Expose
		private String	tipoEq;

		@Expose
		private String	desc;

		@Expose
		private Date	eventTime;

		@Expose
		private int		codAtiv;

		@Expose
		private String	descAtiv;

		@Expose
		private int		tempoTrabalho;

		@Expose
		@SerializedName("latitude")
		private double	lat;

		@Expose
		@SerializedName("longitude")
		private double	lng;

		@Expose
		private int		codColab;

		@Expose
		private String	descColab;

		@Expose
		private int		cdFrente;

		@Expose
		private String	descFrente;

		@Expose
		private int		rpm;

		@Expose
		private double	speed;

		public CambuhyPayload(String[] fields) throws ParseException {
			tagId = fields[0];
			tipoEq = fields[1];
			desc = fields[2];
			eventTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(fields[3]);
			codAtiv = new Integer(fields[4]);
			descAtiv = fields[5];
			tempoTrabalho = new Integer(fields[6]);
			lat = new Double(fields[7]);
			lng = new Double(fields[8]);
			codColab = fields[9].isEmpty() ? 0 : new Integer(fields[9]);
			descColab = fields[10];
			cdFrente = fields[11].isEmpty() ? 0 : new Integer(fields[11]);
			descFrente = fields[12];
			rpm = new Integer(fields[13]);
			speed = new Double(fields[14]);
		}

		/**
		 * @return the tagId
		 */
		public String getTagId() {
			return tagId;
		}

		/**
		 * @param tagId the tagId to set
		 */
		public void setTagId(String tagId) {
			this.tagId = tagId;
		}

		/**
		 * @return the tipoEq
		 */
		public String getTipoEq() {
			return tipoEq;
		}

		/**
		 * @param tipoEq the tipoEq to set
		 */
		public void setTipoEq(String tipoEq) {
			this.tipoEq = tipoEq;
		}

		/**
		 * @return the desc
		 */
		public String getDesc() {
			return desc;
		}

		/**
		 * @param desc the desc to set
		 */
		public void setDesc(String desc) {
			this.desc = desc;
		}

		/**
		 * @return the eventTime
		 */
		public Date getEventTime() {
			return eventTime;
		}

		/**
		 * @param eventTime the eventTime to set
		 */
		public void setEventTime(Date eventTime) {
			this.eventTime = eventTime;
		}

		/**
		 * @return the codAtiv
		 */
		public int getCodAtiv() {
			return codAtiv;
		}

		/**
		 * @param codAtiv the codAtiv to set
		 */
		public void setCodAtiv(int codAtiv) {
			this.codAtiv = codAtiv;
		}

		/**
		 * @return the descAtiv
		 */
		public String getDescAtiv() {
			return descAtiv;
		}

		/**
		 * @param descAtiv the descAtiv to set
		 */
		public void setDescAtiv(String descAtiv) {
			this.descAtiv = descAtiv;
		}

		/**
		 * @return the tempoTrabalho
		 */
		public int getTempoTrabalho() {
			return tempoTrabalho;
		}

		/**
		 * @param tempoTrabalho the tempoTrabalho to set
		 */
		public void setTempoTrabalho(int tempoTrabalho) {
			this.tempoTrabalho = tempoTrabalho;
		}

		/**
		 * @return the lat
		 */
		public double getLat() {
			return lat;
		}

		/**
		 * @param lat the lat to set
		 */
		public void setLat(double lat) {
			this.lat = lat;
		}

		/**
		 * @return the lng
		 */
		public double getLng() {
			return lng;
		}

		/**
		 * @param lng the lng to set
		 */
		public void setLng(double lng) {
			this.lng = lng;
		}

		/**
		 * @return the codColab
		 */
		public int getCodColab() {
			return codColab;
		}

		/**
		 * @param codColab the codColab to set
		 */
		public void setCodColab(int codColab) {
			this.codColab = codColab;
		}

		/**
		 * @return the descColab
		 */
		public String getDescColab() {
			return descColab;
		}

		/**
		 * @param descColab the descColab to set
		 */
		public void setDescColab(String descColab) {
			this.descColab = descColab;
		}

		/**
		 * @return the cdFrente
		 */
		public int getCdFrente() {
			return cdFrente;
		}

		/**
		 * @param cdFrente the cdFrente to set
		 */
		public void setCdFrente(int cdFrente) {
			this.cdFrente = cdFrente;
		}

		/**
		 * @return the descFrente
		 */
		public String getDescFrente() {
			return descFrente;
		}

		/**
		 * @param descFrente the descFrente to set
		 */
		public void setDescFrente(String descFrente) {
			this.descFrente = descFrente;
		}

		/**
		 * @return the rpm
		 */
		public int getRpm() {
			return rpm;
		}

		/**
		 * @param rpm the rpm to set
		 */
		public void setRpm(int rpm) {
			this.rpm = rpm;
		}

		/**
		 * @return the speed
		 */
		public double getSpeed() {
			return speed;
		}

		/**
		 * @param speed the speed to set
		 */
		public void setSpeed(double speed) {
			this.speed = speed;
		}

		@Override
		public String toString() {
			return new GsonBuilder().create().toJson(this);
		}
	}

	private void processLocLine(String line) {
		final String sourceId = "add37962-fcec-4eea-ade0-64c818212321";
		final String deviceId = "41dc6385-381f-4662-b40d-8da84467069a";
		//CD_EQUIPTO,TP_EQUIPTO,NM_EQUIPTO,DH_EVENTO,CD_ATIVIDADE,NM_ATIVIDADE,VA_TEMPO_TRABALHO_SS,VA_LATITUDE,VA_LONGITUDE,
		try {
			CambuhyPayload cambuhyPayload = new CambuhyPayload(line.split(","));
			ComplexData cd = new ComplexData();

			cd.setSource(UUID.fromString(sourceId));
			cd.setDevice(UUID.fromString(deviceId));
			cd.setPort(1);
			cd.setTagId(cambuhyPayload.getTagId());
			cd.setTs(cambuhyPayload.getEventTime().getTime());
			cd.setPayload(cambuhyPayload.toString());
			cd.setType(RawDataType.LOCATION);
			rdSvc.processRawData(cd);
		} catch (ParseException pe) {
			logger.error("Wrong Date/Time");
		}
	}
}
