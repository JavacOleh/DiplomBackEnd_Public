package diplom.controller;

import diplom.entity.good.Good;
import diplom.model.GoodAnalyticsItemDto;
import diplom.model.PageResponse;
import diplom.security.AccountAuthRoles;
import diplom.service.GoodService;
import diplom.service.GoodsAnalyticsService;
import diplom.service.OrderService;
import diplom.service.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static diplom.service.file.FileService.DRIVE_PREFIX;

@RestController
@RequestMapping("/api/goods")
@Tag(name = "Goods", description = "Операції з стравами")
public class GoodController {
    private final GoodService goodService;
    private final FileService fileService;
    private final OrderService orderService;
    private final GoodsAnalyticsService goodsAnalyticsService;

    @Autowired
    public GoodController(GoodService goodService,
                          FileService fileService,
                          OrderService orderService,
                          GoodsAnalyticsService goodsAnalyticsService) {
        this.goodService = goodService;
        this.fileService = fileService;
        this.orderService = orderService;
        this.goodsAnalyticsService = goodsAnalyticsService;
    }

    @Operation(
            summary = "Отримати список страв",
            description = "Повертає всі доступні страви або страви за переданими ідентифікаторами"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Good.class))
                    )
            )
    })
    @GetMapping
    public List<Good> getGoods(@RequestParam(required = false) List<Long> ids) {
        var check = ids == null || ids.isEmpty();
        var res = check
                ? goodService.goodRepository.findAll()
                : goodService.goodRepository.findAllById(ids);

        return check
                ? res.stream().filter(t -> t.getInStock() > 0).toList()
                : res;
    }
    @Operation(
            summary = "Отримати аналітику страв",
            description = "Повертає сторінку з аналітичними даними щодо страв"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageResponse.class)
                    )
            )
    })
    @GetMapping("/analytics")
    public PageResponse<GoodAnalyticsItemDto> getGoodsAnalytics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return goodsAnalyticsService.getGoodsAnalytics(page, size);
    }

    @Operation(
            summary = "Оновити страву",
            description = "Оновлює дані страви та, за наявності, замінює її зображення"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(
                                    type = "string",
                                    example = "Food updated successfully"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or good not found",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "The authenticated account does not have the required role",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(
                                    type = "string",
                                    example = "Wrong role"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "An internal error occurred while updating the good",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string")
                    )
            )
    })
    @PutMapping(value = "/update", consumes = {"multipart/form-data"})
    public ResponseEntity<String> updateFood(
            @RequestPart("good") @Valid Good good,
            BindingResult result,
            @RequestPart(value = "file", required = false) MultipartFile file, HttpServletRequest request) {
        var roleFromToken = AccountAuthRoles.getByStr((String) request.getAttribute("role"));

        if (roleFromToken != null && roleFromToken == AccountAuthRoles.MANAGER) {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .filter(Objects::nonNull)
                        .filter(errorMessage -> !errorMessage.contains("imageFileName is required"))
                        .toList();
                if (!errorMessages.isEmpty())
                    return ResponseEntity.badRequest().body(errorMessages.toString());

            }

            var existingFood = goodService.getGood(good.getId());

            if (existingFood == null) {
                return ResponseEntity.badRequest().body("Food not found");
            }

            try {
                String oldFileName = existingFood.getImageFileName();

                // 1. обновляем файл (если пришёл новый)
                if (file != null && !file.isEmpty()) {
                    var fileName = file.getOriginalFilename();
                    var isFileExists = fileService.isFileExists(fileName);

                    var uploadResponse = fileService.saveFile(
                            file,
                            isFileExists ? System.currentTimeMillis() + "_" + fileName : null
                    );

                    existingFood.setImageFileName(uploadResponse.getUrl());

                    // старый файл удаляем ПОСЛЕ успешной загрузки нового
                    if (oldFileName != null && !oldFileName.isEmpty()) {
                        String cleanName = oldFileName.substring(
                                oldFileName.indexOf(FileService.DRIVE_PREFIX)
                                        + FileService.DRIVE_PREFIX.length()
                        );
                        fileService.deleteFile(cleanName);
                    }
                }

                // 2. обновляем поля
                existingFood.setCaption(good.getCaption());
                existingFood.setPrice(good.getPrice());
                existingFood.setDescription(good.getDescription());
                existingFood.setInStock(good.getInStock());

                // 3. сохраняем
                goodService.addFood(existingFood);

                return ResponseEntity.ok("Food updated successfully");

            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body("Update failed: " + e.getMessage());
            }
        }

        return ResponseEntity.status(403).body("Wrong role");
    }


    @Operation(
            summary = "Додати нову страву",
            description = "Створює нову страву та завантажує файл її зображення"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(
                                    type = "string",
                                    example = "Food added successfully."
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data, empty file, or the good could not be added",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "The authenticated account does not have the required role",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(
                                    type = "string",
                                    example = "Wrong role"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "An internal error occurred while saving the file",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string")
                    )
            )
    })
    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    public ResponseEntity<String> addGood(
            @RequestPart("good") @Valid Good good,
            BindingResult result,
            @RequestPart(value = "file") MultipartFile file, HttpServletRequest request) {
        var roleFromToken = AccountAuthRoles.getByStr((String) request.getAttribute("role"));

        if (roleFromToken != null && roleFromToken == AccountAuthRoles.MANAGER) {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages.toString());
            }

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty.");
            }

            try {
                // Сохраняем файл через твой FileService
                var fileName = file.getOriginalFilename();
                var isFileExists = fileService.isFileExists(fileName);
                var uploadResponse = fileService.saveFile(file,
                        isFileExists
                                ? System.currentTimeMillis() + "_" + fileName
                                : null);
                good.setImageFileName(uploadResponse.getUrl()); // DRIVE_PREFIX + имя файла

                boolean added = goodService.addFood(good);
                return added ? ResponseEntity.ok("Food added successfully.") :
                        ResponseEntity.badRequest().body("Failed to add food.");
            } catch (IOException e) {
                return ResponseEntity.internalServerError()
                        .body("Failed to save file: " + e.getMessage());
            }
        }

        return ResponseEntity.status(403).body("Wrong role");
    }

    @Operation(
            summary = "Видалити страву",
            description = "Видаляє страву та пов’язаний із нею файл зображення"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The good was not found, could not be deleted, or the account does not have permission"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(@PathVariable long id, HttpServletRequest request) {
        var roleFromToken = AccountAuthRoles.getByStr((String) request.getAttribute("role"));
        boolean success = false;
        if (roleFromToken != null && roleFromToken == AccountAuthRoles.MANAGER) {
            var food = goodService.getGood(id);
            success = food != null;
            if (success) {
                var fileName = food.getImageFileName();

                if (fileName != null && !fileName.isEmpty()) {
                    if (fileName.contains(DRIVE_PREFIX))
                        fileName = fileName.substring(fileName.indexOf(DRIVE_PREFIX) + DRIVE_PREFIX.length());

                    fileService.deleteFile(fileName);
                }
                success = orderService.deleteFood(food.getId());
            }
        }
        return success
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }
}
