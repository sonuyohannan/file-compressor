package com.example.compressor.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

@Service
public class CompressionService {

    public record CompressionResult(byte[] data, String filename, String contentType) {}

    public CompressionResult compress(MultipartFile file, double quality) throws IOException {
        String originalName = Objects.requireNonNullElse(file.getOriginalFilename(), "file");
        String extension = extensionOf(originalName);

        if (isImage(extension)) {
            return compressImage(file, originalName, quality);
        }

        return compressWithGzip(file, originalName);
    }

    private CompressionResult compressImage(MultipartFile file, String originalName, double quality) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            return compressWithGzip(file, originalName);
        }

        String extension = extensionOf(originalName);
        String formatName = extension.equals("jpg") ? "jpeg" : extension;
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);
        if (!writers.hasNext()) {
            return compressWithGzip(file, originalName);
        }

        ImageWriter writer = writers.next();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
        writer.setOutput(imageOutputStream);

        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality((float) Math.max(0.1, Math.min(1.0, quality)));
        }

        writer.write(null, new IIOImage(image, null, null), param);
        writer.dispose();
        imageOutputStream.close();

        return new CompressionResult(outputStream.toByteArray(), originalName, file.getContentType());
    }

    private CompressionResult compressWithGzip(MultipartFile file, String originalName) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
            gzipOutputStream.write(file.getBytes());
        }

        return new CompressionResult(
                outputStream.toByteArray(),
                originalName + ".gz",
                "application/gzip"
        );
    }

    private boolean isImage(String extension) {
        return switch (extension.toLowerCase(Locale.ROOT)) {
            case "jpg", "jpeg", "png", "webp", "bmp" -> true;
            default -> false;
        };
    }

    private String extensionOf(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }
}
