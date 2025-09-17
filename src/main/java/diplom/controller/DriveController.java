package diplom.controller;

import diplom.service.google.DriveService;
import diplom.service.google.web.UploadResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/drive")
public class DriveController {
    private final DriveService service;

    @Autowired
    public DriveController(DriveService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<String>> getAllFileNames() {
        var list = service.listAllFilesInFolder();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/upload")
    public ResponseEntity<UploadResponse> uploadFile(@RequestParam("image") MultipartFile file) {
        if (file.isEmpty()) {
            UploadResponse response = new UploadResponse();
            response.setStatus(400);  // 400 - Bad Request
            response.setMessage("File is empty.");
            return ResponseEntity.badRequest().body(response);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            UploadResponse response = new UploadResponse();
            response.setStatus(400);  // 400 - Bad Request
            response.setMessage("File has no name.");
            return ResponseEntity.badRequest().body(response);
        }

        String nameWithoutExtension = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        String tempFileName = nameWithoutExtension + ".tmp";
        File tempFile = new File(System.getProperty("java.io.tmpdir"), tempFileName);
        try {
            file.transferTo(tempFile);
        } catch (IOException e) {
            var tmp = new UploadResponse();
            tmp.setMessage(Arrays.toString(e.getStackTrace()));
            tmp.setStatus(400);
            return ResponseEntity.badRequest().body(tmp);
        }

        UploadResponse response = service.uploadImageToDrive(tempFile);
        tempFile.delete();  // Удаляем временный файл

        if (response.getStatus() == 200) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getStatus()).body(response);
        }
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) {
        try {
            byte[] fileBytes = service.getImageFromDrive(fileName);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(fileBytes);
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(404).body(null);  // File not found
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);  // Internal server error (for I/O issues)
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);  // Generic error
        }
    }


    @DeleteMapping("/{fileName}")
    public ResponseEntity<UploadResponse> deleteFile(@PathVariable String fileName) {
        UploadResponse response = service.deleteFileFromDrive(fileName);

        if (response.getStatus() == 200) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getStatus()).body(response);
        }
    }

    @GetMapping("/exists/{fileName}")
    public ResponseEntity<Boolean> checkIfExist(@PathVariable String fileName) {
        var check = service.isFileExists(fileName);

        return ResponseEntity.ok(check);
    }
}
