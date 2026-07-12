package diplom.model;

import lombok.Data;

@Data
public class UploadResponse {
    private int status;
    private String message;
    private String url;
    private String originalFilename;
}