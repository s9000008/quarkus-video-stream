package my.project.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import my.project.entity.FileEntity;

@Path("/list")
public class ListController {

	@GET
	public java.util.List<FileEntity> getAllFileInfo() {
		return FileEntity.findAll().list();
	}
	
}
