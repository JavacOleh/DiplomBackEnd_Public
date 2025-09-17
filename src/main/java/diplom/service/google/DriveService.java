package diplom.service.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import diplom.service.google.web.UploadResponse;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Folder: https://drive.google.com/drive/folders/1gqg2xte3jaiWTishH44KSJyW9a3xAZAF
@Service
public class DriveService {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String SERVICE_ACOUNT_KEY_PATH = getPathToGoodleCredentials();
    public static final String folderId = "1gqg2xte3jaiWTishH44KSJyW9a3xAZAF";
    private Drive drive;

    public DriveService() {
        try {
            drive = createDriveService();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    private static String getPathToGoodleCredentials() {
        String currentDirectory = System.getProperty("user.dir");
        return Paths.get(currentDirectory, "cred.json").toString();
    }

    public UploadResponse uploadImageToDrive(File file) {
        UploadResponse uploadResponse = new UploadResponse();

        try {
            com.google.api.services.drive.model.File fileMetaData = new com.google.api.services.drive.model.File();

            String originalFilename = file.getName();  // Название с .tmp

            // Устанавливаем метаданные файла
            fileMetaData.setName(originalFilename);
            fileMetaData.setParents(Collections.singletonList(folderId));  // Папка на Google Drive

            // Создаем объект для передачи файла на Google Drive
            FileContent mediaContent = new FileContent("image/jpeg", file);

            // Загружаем файл на Google Drive
            com.google.api.services.drive.model.File uploadedFile = drive.files().create(fileMetaData, mediaContent)
                    .setFields("id")
                    .execute();

            String imageUrl = "https://drive.google.com/uc?export=view&id=" + uploadedFile.getId();
            uploadResponse.setStatus(200);
            uploadResponse.setMessage("Image successfully uploaded.");
            uploadResponse.setUrl(imageUrl);
            uploadResponse.setOriginalFilename(originalFilename);
        } catch (IOException e) {
            uploadResponse.setStatus(500);
            uploadResponse.setMessage("IO Error: " + e.getMessage());
        } catch (Exception e) {
            uploadResponse.setStatus(500);
            uploadResponse.setMessage("Unexpected Error: " + e.getMessage());
        }

        return uploadResponse;
    }

    public byte[] getImageFromDrive(String fileName) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            // Ищем файл по имени внутри папки folderId
            String query = String.format("name = '%s' and '%s' in parents", fileName, folderId);
            com.google.api.services.drive.Drive.Files.List request = drive.files().list()
                    .setQ(query)  // Здесь фильтруем по имени файла и ID папки
                    .setFields("files(id)");

            // Получаем список файлов по запросу
            com.google.api.services.drive.model.FileList fileList = request.execute();

            // Если файл найден
            if (fileList.getFiles().isEmpty()) {
                throw new FileNotFoundException("File not found in the specified folder.");
            }

            // Предполагаем, что первый файл из списка это нужный
            String fileId = fileList.getFiles().get(0).getId();

            // Загружаем файл по его ID
            drive.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream);

            return outputStream.toByteArray();
        } catch (FileNotFoundException e) {
            throw new IOException("File not found: " + fileName, e);  // Преобразуем в IO ошибку
        } catch (IOException e) {
            throw new IOException("Error while downloading the file: " + fileName, e);  // Ошибка загрузки файла
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while accessing Google Drive.", e);  // Прочие ошибки
        }
    }


    public UploadResponse deleteFileFromDrive(String filename) {
        UploadResponse uploadResponse = new UploadResponse();

        try {
            // Ищем файл по имени внутри папки folderId
            String query = String.format("name = '%s' and '%s' in parents", filename, folderId);
            com.google.api.services.drive.Drive.Files.List request = drive.files().list()
                    .setQ(query)  // Фильтруем по имени и ID папки
                    .setFields("files(id)");

            // Получаем список файлов по запросу
            com.google.api.services.drive.model.FileList fileList = request.execute();

            // Если файл найден
            if (fileList.getFiles().isEmpty()) {
                uploadResponse.setStatus(404);
                uploadResponse.setMessage("File not found in the specified folder.");
                return uploadResponse;
            }

            // Получаем ID найденного файла
            String fileId = fileList.getFiles().get(0).getId();

            // Удаляем файл по его ID
            drive.files().delete(fileId).execute();

            uploadResponse.setStatus(200);
            uploadResponse.setMessage("File successfully deleted.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            uploadResponse.setStatus(500);
            uploadResponse.setMessage("Error deleting file: " + e.getMessage());
        }

        return uploadResponse;
    }

    public boolean isFileExists(String fileName) {
        try {
            // Ищем файл по имени внутри папки folderId
            String query = String.format("name = '%s' and '%s' in parents", fileName, folderId);
            com.google.api.services.drive.Drive.Files.List request = drive.files().list()
                    .setQ(query)  // Фильтруем по имени файла и ID папки
                    .setFields("files(id)");

            // Получаем список файлов по запросу
            com.google.api.services.drive.model.FileList fileList = request.execute();

            // Проверяем, если файл найден
            return !fileList.getFiles().isEmpty();  // Если файл есть, возвращаем true
        } catch (Exception e) {
            System.out.println("Error checking file existence: " + e.getMessage());
            return false;  // В случае ошибки возвращаем false
        }
    }

    public List<String> listAllFilesInFolder() {
        List<String> fileNames = new ArrayList<>();

        try {
            // Создаём запрос для получения файлов в папке по folderId
            String query = String.format("'%s' in parents", folderId);
            com.google.api.services.drive.Drive.Files.List request = drive.files().list()
                    .setQ(query)
                    .setFields("files(name)");

            // Получаем список файлов по запросу
            com.google.api.services.drive.model.FileList fileList = request.execute();

            // Добавляем названия файлов в список
            for (com.google.api.services.drive.model.File file : fileList.getFiles()) {
                fileNames.add(file.getName());
            }
        } catch (IOException e) {
            return List.of("Something went wrong in: DriveService.java, Line: 190," + e.getMessage());
        }

        return fileNames;
    }

    private Drive createDriveService() throws GeneralSecurityException, IOException {
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(SERVICE_ACOUNT_KEY_PATH))
                .createScoped(Collections.singleton(DriveScopes.DRIVE));

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential)
                .build();
    }
}