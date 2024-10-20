package my.project.dto;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;

public class ChunkedUploadBody {
    
	@FormParam("fileChunk")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] fileChunk; // 檔案塊
    @FormParam("fileName")
    @PartType(MediaType.TEXT_PLAIN)
    public String fileName;
    @FormParam("chunkIndex")
    @PartType(MediaType.TEXT_PLAIN)
    public int chunkIndex; // 當前塊的索引
    @FormParam("totalChunks")
    @PartType(MediaType.TEXT_PLAIN)
    public int totalChunks; // 總塊數
}

