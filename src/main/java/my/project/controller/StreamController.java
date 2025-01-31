package my.project.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.StreamingOutput;
import my.project.entity.FileEntity;

@Path("/stream")
public class StreamController {

    @GET
    @Path("/{id}")
    @Produces("video/mp4")
    public Response downloadFileInChunks(@PathParam("id") int fileId, @HeaderParam("Range") String range) {
    	System.out.println(range);
        // 根據文件ID獲取文件的詳細資訊（此處可以從資料庫或其他地方讀取）
    	FileEntity fileEntity = getFileById(fileId);
    	System.out.println(String.format("file : %s", (fileEntity == null)));
        if (fileEntity == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("File not found").build();
        }
        
        String fileName = fileEntity.name;
        File file = new File(fileEntity.path);
        System.out.println(String.format("file exist: %s", (file.exists())));
        if (!file.exists()) {
            return Response.status(Response.Status.NOT_FOUND).entity("File not found").build();
        }
        
        try {
            // 解析 Range 標頭以獲取起始和結束位置
            long fileLength = file.length();
            long[] rangeValues = new long[2]; // 用於存儲 start 和 end
            rangeValues[0] = 0; // start
            rangeValues[1] = fileLength - 1; // end

            if (range != null && range.startsWith("bytes=")) {
                String[] rangeParts = range.substring(6).split("-");
                rangeValues[0] = Long.parseLong(rangeParts[0]);
                if (rangeParts.length > 1) {
                    rangeValues[1] = Long.parseLong(rangeParts[1]);
                }
            }

            long start = rangeValues[0];
            long end = rangeValues[1];
            long contentLength = end - start + 1;

            // StreamingOutput 用來處理大文件的分塊下載
            StreamingOutput fileStream = output -> {
                try (InputStream in = new FileInputStream(file)) {
                    in.skip(start); // 跳過文件的部分，根據 Range 進行下載
                    byte[] buffer = new byte[128];
                    int bytesRead;
                    long bytesToRead = contentLength;
                    while ((bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, bytesToRead))) != -1) {
                        output.write(buffer, 0, bytesRead);
                        bytesToRead -= bytesRead;
                        if (bytesToRead <= 0) {
                            break;
                        }
                    }
                    System.out.println(String.format("file ava: %s", (in.available())));
                } catch (IOException e) {
                    throw new WebApplicationException("File chunk download error", e);
                }
            };
            // 返回部分內容並且設置 Content-Range 標頭
            ResponseBuilder response = Response.status(Response.Status.PARTIAL_CONTENT)
                    .entity(fileStream)
                    .header(HttpHeaders.CONTENT_LENGTH, contentLength)
                    .header("Accept-Ranges", "bytes")
                    .header("Content-Length", end - start + 1)
                    .header("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);

            // 設置下載文件的名稱
            response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            return response.build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error downloading file").build();
        }
    }

    // 模擬從資料庫或文件系統獲取文件名稱
    private FileEntity getFileById(int fileId) {
        return FileEntity.findById(fileId);
    }
}