package sn.lhacksrt.firdawsitech_server.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sn.lhacksrt.firdawsitech_server.service.ImageStorageService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/uploads", produces = "application/json")
public class UploadController {

    private final ImageStorageService storage;

    @PostMapping(value = "/images", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestPart("file") @NotNull MultipartFile file) {
        var stored = storage.store(file);
        return ResponseEntity.ok(Map.of(
                "url", stored.url(),
                "filename", stored.filename(),
                "contentType", stored.contentType(),
                "size", stored.size()
        ));
    }
}
