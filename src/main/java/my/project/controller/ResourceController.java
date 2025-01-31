package my.project.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import my.project.entity.FileEntity;
import my.project.entity.VttEntity;

@Path("/resource")
public class ResourceController {
	
	@Path("/list")
	@GET
	public java.util.List<FileEntity> getAllFileInfo() {
		return FileEntity.findAll().list();
	}
	
	@Path("/get/{id}")
	@GET
	public FileEntity getFileInfo(@PathParam("id") String id) {
		long fileId = Long.parseLong(id);
		return FileEntity.findById(fileId);
	}
	
	@Path("/delete/{id}")
	@POST
	public String deleteFile(@PathParam("id") String id) throws IOException {
		long fileId = Long.parseLong(id);
		Optional<FileEntity> fileOpt = FileEntity.findByIdOptional(fileId);
		if(fileOpt.isPresent()) {
			FileEntity fileEntity = fileOpt.get();
			java.nio.file.Path path = Paths.get(fileEntity.path);
			if(Files.exists(path)) {
				Files.delete(path);
			}
			fileEntity.delete();
		}
		return "成功";
	}
	
	@Path("/vtt/{fileId}/{local}")
	@GET
	@Produces("text/vtt")
	public Response getVtt(@PathParam("fileId") String fileId, @PathParam("local") String local) throws IOException {
		long fileIdLon = Long.parseLong(fileId);
		VttEntity vttEntity = VttEntity.find("fileId=?1 AND local=?2", fileIdLon, local).firstResult();
		String filePath = vttEntity.path;
		try {
	        InputStream subtitleStream = Files.newInputStream(Paths.get(filePath));
	        byte[] byteArray = subtitleStream.readAllBytes();
	        subtitleStream.close();
            ResponseBuilder response = Response.ok(byteArray).type("text/vtt");
            return response.build();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
	}
}
