package diplom.controller;

import diplom.service.file.FileService;
import diplom.model.UploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/drive")
@Tag(name = "Drive", description = "Операції з файлами")
public class DriveController {
    private final FileService service;

    @Autowired
    public DriveController(FileService service) {
        this.service = service;
    }


    @Operation(
            summary = "Отримання списку файлів",
            description = "Повертає масив імен файлів, збережених у файловому сервісі."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Files not found."),
    })
    @GetMapping
    public ResponseEntity<List<String>> getAllFileNames() {
        List<String> list = service.listAllFiles();
        return ResponseEntity.ok(list);
    }

    @Operation(
            summary = "Збереження файлу",
            description = "Зберігає файл на сервері."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "File is empty."),
            @ApiResponse(responseCode = "500", description = "Failed to save file")
    })
    @PutMapping("/upload")
    public ResponseEntity<UploadResponse> uploadFile(@RequestParam("image") MultipartFile file) {
        if (file.isEmpty()) {
            UploadResponse response = new UploadResponse();
            response.setStatus(400);
            response.setMessage("File is empty.");
            return ResponseEntity.badRequest().body(response);
        }

        UploadResponse response;
        try {
            response = service.saveFile(file, null);
        } catch (IOException e) {
            response = new UploadResponse();
            response.setStatus(500);
            response.setMessage("Failed to save file: " + e.getMessage());
        }

        if (response.getStatus() == 200) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getStatus()).body(response);
        }
    }

    @Operation(
            summary = "Завантаження файлу",
            description = "Завантажує файл за ім'ям файлу."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "File not found.")
    })
    @GetMapping("/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) {
        try {
            byte[] fileBytes = service.getFile(fileName);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(fileBytes);
        } catch (IOException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    @Operation(
            summary = "Видалення файлу",
            description = "Видаляє файл за ім'ям файлу."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "File not found.")
    })
    @DeleteMapping("/{fileName}")
    public ResponseEntity<UploadResponse> deleteFile(@PathVariable String fileName) {
        UploadResponse response = service.deleteFile(fileName);
        if (response.getStatus() == 200) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getStatus()).body(response);
        }
    }
}