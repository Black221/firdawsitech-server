package sn.lhacksrt.firdawsitech_server.service;

import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import sn.lhacksrt.firdawsitech_server.config.FileStorageProperties;

import java.io.InputStream;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class ImageStorageService {

    private final FileStorageProperties props;
    private final Tika tika = new Tika();

    public StoredImage store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Aucun fichier reçu.");
        }
        // Détection MIME (Tika) + contrôle image/*
        String mime = detectMime(file);
        if (mime == null || !mime.startsWith("image/")) {
            throw new IllegalArgumentException("Le fichier n'est pas une image valide.");
        }

        // Extension par défaut selon MIME
        String ext = switch (mime) {
            case "image/jpeg" -> ".jpg";
            case "image/png"  -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif"  -> ".gif";
            default -> ""; // inconnu : pas d’extension
        };

        // Hash contenu pour éviter doublons (nommage stable)
        String hash = contentSha256(file);
        String filename = hash + ext;

        Path uploadDir = Path.of(props.uploadDir()).toAbsolutePath().normalize();
        Path target = uploadDir.resolve(filename);

        try {
            Files.createDirectories(uploadDir);
            // n’écrase pas si déjà présent
            if (!Files.exists(target)) {
                try (InputStream in = file.getInputStream()) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Échec de l'écriture du fichier", e);
        }

        String publicUrl = cleanJoin(props.publicBaseUrl(), "/", filename);
        return new StoredImage(filename, mime, file.getSize(), publicUrl, target.toString());
    }

    public void deleteIfLocalUrl(String imageUrl) {
        if (imageUrl == null) return;
        String base = cleanJoin(props.publicBaseUrl(), "/", "");
        if (imageUrl.startsWith(base)) {
            String filename = imageUrl.substring(base.length());
            Path path = Path.of(props.uploadDir(), filename).toAbsolutePath().normalize();
            try { Files.deleteIfExists(path); } catch (Exception ignored) {}
        }
    }

    private String detectMime(MultipartFile f) {
        try (InputStream in = f.getInputStream()) {
            return tika.detect(in, StringUtils.cleanPath(f.getOriginalFilename()));
        } catch (Exception e) {
            return null;
        }
    }

    private String contentSha256(MultipartFile f) {
        try (InputStream in = f.getInputStream()) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) md.update(buf, 0, n);
            return HexFormat.of().formatHex(md.digest());
        } catch (Exception e) {
            throw new RuntimeException("Impossible de calculer le hash du fichier.", e);
        }
    }

    private String cleanJoin(String a, String sep, String b) {
        String left = a.endsWith("/") ? a.substring(0, a.length()-1) : a;
        String right = b.startsWith("/") ? b.substring(1) : b;
        return left + sep + right;
    }

    public record StoredImage(String filename, String contentType, long size, String url, String diskPath) {}
}
