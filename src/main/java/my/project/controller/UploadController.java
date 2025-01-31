package my.project.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import my.project.dto.ChunkedUploadBody;
import my.project.dto.VttChunkedUploadBody;
import my.project.entity.FileEntity;
import my.project.entity.VttEntity;

@Path("/upload")
public class UploadController {
    
	@ConfigProperty(name = "resource.path")
    String uploadDir;

    @POST
    @Path("/resource")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String handleChunkUpload(@MultipartForm ChunkedUploadBody data) {
        // 根據塊的索引和總塊數合併檔案的邏輯
    	System.out.println(String.format("%s: %d", data.fileName, data.chunkIndex));
        String filePath = uploadDir + "/" + data.fileName; // 使用唯一名稱或生成的名稱
        try (FileOutputStream fos = new FileOutputStream(filePath + "_" + data.chunkIndex, true)) {
            fos.write(data.fileChunk); // 寫入檔案塊
        } catch (IOException e) {
            return "Failed to save chunk";
        }
        if (data.chunkIndex == data.totalChunks - 1) {
            // 當所有塊都上傳完成後，進行檔案合併
            try {
                // 合併所有塊
                File finalFile = new File(filePath);
                try (FileOutputStream fos = new FileOutputStream(finalFile, true)) {
                    for (int i = 0; i < data.totalChunks; i++) {
                        File chunkFile = new File(filePath + "_" + i);
                        try (FileInputStream fis = new FileInputStream(chunkFile)) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = fis.read(buffer)) != -1) {
                                fos.write(buffer, 0, bytesRead);
                            }
                        }
                        // 刪除已經合併的塊檔案
                        chunkFile.delete();
                    }
                }
                FileEntity fileRow = new FileEntity();
                fileRow.name = data.fileName;
                fileRow.path = filePath;
                if(!saveFileEntity(fileRow)) {
                	return "Failed to save file";
                }
            } catch (IOException e) {
                return "Failed to merge file: " + e.getMessage();
            }
            return "File uploaded and merged successfully.";
        }
        return "Chunk " + data.chunkIndex + " uploaded successfully.";
    }
    
    @POST
    @Path("/vtt")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String vttChunkUpload(@MultipartForm VttChunkedUploadBody data) {
        // 根據塊的索引和總塊數合併檔案的邏輯
        String filePath = uploadDir + "/" + data.fileName; // 使用唯一名稱或生成的名稱
        try (FileOutputStream fos = new FileOutputStream(filePath + "_" + data.chunkIndex, true)) {
            fos.write(data.fileChunk); // 寫入檔案塊
        } catch (IOException e) {
            return "Failed to save chunk";
        }
        if (data.chunkIndex == data.totalChunks - 1) {
            // 當所有塊都上傳完成後，進行檔案合併
            try {
                // 合併所有塊
                File finalFile = new File(filePath);
                try (FileOutputStream fos = new FileOutputStream(finalFile, true)) {
                    for (int i = 0; i < data.totalChunks; i++) {
                        File chunkFile = new File(filePath + "_" + i);
                        try (FileInputStream fis = new FileInputStream(chunkFile)) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = fis.read(buffer)) != -1) {
                                fos.write(buffer, 0, bytesRead);
                            }
                        }
                        // 刪除已經合併的塊檔案
                        chunkFile.delete();
                    }
                }
                VttEntity vttRow = new VttEntity();
                vttRow.fileId = data.fileId;
                vttRow.local = data.local;
                vttRow.path = filePath;
                if(!saveVttEntity(vttRow)) {
                	return "Failed to save file";
                }
            } catch (IOException e) {
                return "Failed to merge file: " + e.getMessage();
            }
            return "File uploaded and merged successfully.";
        }
        return "Chunk " + data.chunkIndex + " uploaded successfully.";
    }
    
    @Transactional
    public boolean saveVttEntity(VttEntity row) {
    	row.persist();
    	return true;
    }
    
    @Transactional
    public boolean saveFileEntity(FileEntity row) {
    	row.persist();
    	return true;
    }
    
}