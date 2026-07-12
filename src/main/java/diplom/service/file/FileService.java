package diplom.service.file;

import diplom.model.UploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/images";
    public static final String DRIVE_PREFIX = "drive/";

    public UploadResponse saveFile(MultipartFile file, String fileName) throws IOException {
        UploadResponse response = new UploadResponse();

        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        String originalFilename = fileName == null || fileName.isEmpty()
                ? file.getOriginalFilename()
                : fileName;

        Path filePath = Paths.get(UPLOAD_DIR, originalFilename);
        file.transferTo(filePath.toFile());

        response.setStatus(200);
        response.setMessage("File saved successfully.");
        response.setUrl(DRIVE_PREFIX + originalFilename);
        response.setOriginalFilename(originalFilename);

        return response;
    }

    public byte[] getFile(String fileName) throws IOException {
        Path filePath = Paths.get(UPLOAD_DIR, fileName);
        if (!Files.exists(filePath)) throw new IOException("File not found");
        return Files.readAllBytes(filePath);
    }

    public UploadResponse deleteFile(String fileName) {
        UploadResponse response = new UploadResponse();
        Path filePath = Paths.get(UPLOAD_DIR, fileName);
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                response.setStatus(200);
                response.setMessage("File deleted successfully.");
            } else {
                response.setStatus(404);
                response.setMessage("File not found.");
            }
        } catch (IOException e) {
            response.setStatus(500);
            response.setMessage("Failed to delete file: " + e.getMessage());
        }
        return response;
    }

    public boolean isFileExists(String fileName) {
        Path filePath = Paths.get(UPLOAD_DIR, fileName);
        return Files.exists(filePath);
    }

    public List<String> listAllFiles() {
        List<String> files = new ArrayList<>();
        File folder = new File(UPLOAD_DIR);
        if (folder.exists() && folder.isDirectory()) {
            for (File f : folder.listFiles()) {
                if (f.isFile()) files.add(f.getName());
            }
        }
        return files;
    }
}