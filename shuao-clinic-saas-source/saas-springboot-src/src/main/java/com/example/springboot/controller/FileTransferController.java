package com.example.springboot.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/file-transfer")
public class FileTransferController {

    private static final Path DOWNLOAD_DIR = Paths.get(System.getProperty("user.home"), "shared-downloads");
    private static final Path UPLOAD_DIR = Paths.get(System.getProperty("user.home"), "shared-uploads");
    private static final String LATEST_PACKAGE = "project-source-latest.zip";
    private static final String LATEST_DAILY_BACKUP = "daily-backup-latest.zip";
    private static final DateTimeFormatter BATCH_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
            .withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String index(@RequestParam(required = false) String uploadStatus,
                        @RequestParam(required = false) String uploadMessage) throws IOException {
        Files.createDirectories(DOWNLOAD_DIR);
        Files.createDirectories(UPLOAD_DIR);

        List<TransferFileView> files = new ArrayList<>();
        try (var stream = Files.list(DOWNLOAD_DIR)) {
            stream.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(this::lastModifiedSafe).reversed())
                    .forEach(path -> files.add(new TransferFileView(
                            path.getFileName().toString(),
                            formatSize(sizeSafe(path)),
                            TIME_FORMATTER.format(lastModifiedSafe(path))
                    )));
        }

        String latestSourceLink = "/file-transfer/files/" + LATEST_PACKAGE;
        String latestDailyBackupLink = "/file-transfer/files/" + LATEST_DAILY_BACKUP;
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html lang=\"zh-CN\"><head><meta charset=\"utf-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">")
                .append("<title>固定文件传输页</title>")
                .append("<style>")
                .append("body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;background:#f4f7fb;color:#1f2937;margin:0;padding:24px;}")
                .append(".wrap{max-width:960px;margin:0 auto;}")
                .append(".card{background:#fff;border-radius:18px;padding:20px 22px;box-shadow:0 12px 30px rgba(15,23,42,.08);margin-bottom:16px;}")
                .append("h1{margin:0 0 8px;font-size:28px;}p{margin:6px 0;line-height:1.7;color:#4b5563;}")
                .append(".btn{display:inline-block;margin-top:10px;padding:10px 14px;border-radius:10px;background:#2563eb;color:#fff;text-decoration:none;font-weight:600;}")
                .append(".alert{padding:12px 14px;border-radius:12px;margin-top:14px;font-size:14px;line-height:1.6;}")
                .append(".alert-success{background:#ecfdf5;color:#166534;border:1px solid #bbf7d0;}")
                .append(".alert-error{background:#fef2f2;color:#b91c1c;border:1px solid #fecaca;}")
                .append(".form-row{display:grid;gap:12px;margin-top:12px;}")
                .append("input[type=file],textarea{width:100%;box-sizing:border-box;border:1px solid #d1d5db;border-radius:12px;padding:10px 12px;font:inherit;background:#fff;}")
                .append("textarea{min-height:92px;resize:vertical;}")
                .append("button{border:0;border-radius:10px;background:#0f766e;color:#fff;padding:10px 14px;font-size:14px;font-weight:600;cursor:pointer;}")
                .append("button:hover{background:#115e59;}")
                .append("table{width:100%;border-collapse:collapse;margin-top:10px;}th,td{text-align:left;padding:12px 10px;border-bottom:1px solid #e5e7eb;font-size:14px;}")
                .append("th{color:#6b7280;font-weight:600;}code{background:#eef2ff;padding:2px 6px;border-radius:6px;}")
                .append("a{color:#2563eb;text-decoration:none;}a:hover{text-decoration:underline;}")
                .append("</style></head><body><div class=\"wrap\">")
                .append("<div class=\"card\">")
                .append("<h1>固定文件传输页</h1>")
                .append("<p>这个页面以后可以作为固定传输入口使用。</p>")
                .append("<p>固定最新源码包链接：<code>")
                .append(escapeHtml("https://saas.shuao.cc" + latestSourceLink))
                .append("</code></p>")
                .append("<a class=\"btn\" href=\"").append(latestSourceLink).append("\">下载最新项目源码 ZIP</a>")
                .append("<p style=\"margin-top:16px\">固定最新每日全量备份链接：<code>")
                .append(escapeHtml("https://saas.shuao.cc" + latestDailyBackupLink))
                .append("</code></p>")
                .append("<a class=\"btn\" href=\"").append(latestDailyBackupLink).append("\">下载最新每日全量备份 ZIP</a>")
                .append("</div>");

        html.append("<div class=\"card\"><h1 style=\"font-size:22px\">上传资料</h1>")
                .append("<p>以后你可以直接在这里把资料上传给我。文件会保存到服务器独立目录，不会出现在公开下载列表中。</p>")
                .append("<p>建议：单次上传尽量控制在 500MB 以内，可一次选多个文件。</p>");
        if ("success".equalsIgnoreCase(uploadStatus) && uploadMessage != null && !uploadMessage.isBlank()) {
            html.append("<div class=\"alert alert-success\">").append(escapeHtml(uploadMessage)).append("</div>");
        } else if ("error".equalsIgnoreCase(uploadStatus) && uploadMessage != null && !uploadMessage.isBlank()) {
            html.append("<div class=\"alert alert-error\">").append(escapeHtml(uploadMessage)).append("</div>");
        }
        html.append("<form method=\"post\" action=\"/file-transfer/upload\" enctype=\"multipart/form-data\">")
                .append("<div class=\"form-row\">")
                .append("<input type=\"file\" name=\"files\" multiple required>")
                .append("<textarea name=\"note\" placeholder=\"可选备注：这批资料是什么、希望我怎么处理\"></textarea>")
                .append("<div><button type=\"submit\">上传资料</button></div>")
                .append("</div>")
                .append("</form>")
                .append("</div>");

        html.append("<div class=\"card\"><h1 style=\"font-size:22px\">当前可下载文件</h1>");
        if (files.isEmpty()) {
            html.append("<p>当前还没有可下载文件。</p>");
        } else {
            html.append("<table><thead><tr><th>文件名</th><th>大小</th><th>更新时间</th></tr></thead><tbody>");
            for (TransferFileView file : files) {
                String href = "/file-transfer/files/" + encodePath(file.filename);
                html.append("<tr>")
                        .append("<td><a href=\"").append(href).append("\">")
                        .append(escapeHtml(file.filename))
                        .append("</a></td>")
                        .append("<td>").append(file.sizeText).append("</td>")
                        .append("<td>").append(file.updatedAtText).append("</td>")
                        .append("</tr>");
            }
            html.append("</tbody></table>");
        }
        html.append("</div></div></body></html>");
        return html.toString();
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(@RequestParam("files") MultipartFile[] files,
                         @RequestParam(required = false) String note) {
        try {
            Files.createDirectories(UPLOAD_DIR);
            if (files == null || files.length == 0) {
                return redirectWithMessage("error", "未选择任何文件");
            }

            String batchId = BATCH_TIME_FORMATTER.format(java.time.Instant.now()) + "-" + System.nanoTime();
            List<String> storedFiles = new ArrayList<>();
            List<String> originalFiles = new ArrayList<>();

            int index = 0;
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                index++;
                String originalName = normalizeText(file.getOriginalFilename());
                String safeName = sanitizeUploadedFilename(originalName);
                String finalName = batchId + "-" + index + "-" + safeName;
                Path target = UPLOAD_DIR.resolve(finalName).normalize();
                file.transferTo(target);
                storedFiles.add(finalName);
                originalFiles.add(originalName.isEmpty() ? safeName : originalName);
            }

            if (storedFiles.isEmpty()) {
                return redirectWithMessage("error", "上传失败：文件为空");
            }

            writeUploadMeta(batchId, originalFiles, storedFiles, note);
            return redirectWithMessage("success", "上传成功，已接收 " + storedFiles.size() + " 个文件。");
        } catch (Exception exception) {
            return redirectWithMessage("error", "上传失败：" + exception.getMessage());
        }
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> download(@PathVariable String filename) throws IOException {
        if (!isSafeFilename(filename)) {
            return ResponseEntity.badRequest().build();
        }
        Path file = DOWNLOAD_DIR.resolve(filename).normalize();
        if (!file.startsWith(DOWNLOAD_DIR) || !Files.exists(file) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        String contentType = Files.probeContentType(file);
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    private void writeUploadMeta(String batchId,
                                 List<String> originalFiles,
                                 List<String> storedFiles,
                                 String note) throws IOException {
        Path metaFile = UPLOAD_DIR.resolve(batchId + ".upload-meta.txt");
        StringBuilder content = new StringBuilder();
        content.append("上传时间: ").append(TIME_FORMATTER.format(java.time.Instant.now())).append('\n');
        content.append("批次号: ").append(batchId).append('\n');
        content.append("备注: ").append(normalizeText(note).isEmpty() ? "(无)" : normalizeText(note)).append('\n');
        content.append("文件数: ").append(storedFiles.size()).append('\n');
        content.append('\n');
        for (int i = 0; i < storedFiles.size(); i++) {
            content.append("原始文件名: ").append(originalFiles.get(i)).append('\n');
            content.append("保存文件名: ").append(storedFiles.get(i)).append('\n');
            content.append('\n');
        }
        Files.writeString(metaFile, content.toString(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    private String redirectWithMessage(String status, String message) {
        return "redirect:/file-transfer?uploadStatus=" + encodePath(status) + "&uploadMessage=" + encodePath(message);
    }

    private boolean isSafeFilename(String filename) {
        return filename != null
                && !filename.isBlank()
                && filename.matches("[A-Za-z0-9._-]+");
    }

    private java.time.Instant lastModifiedSafe(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant();
        } catch (IOException ignored) {
            return java.time.Instant.EPOCH;
        }
    }

    private long sizeSafe(Path path) {
        try {
            return Files.size(path);
        } catch (IOException ignored) {
            return 0L;
        }
    }

    private String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        }
        double value = size;
        String[] units = {"KB", "MB", "GB"};
        int index = -1;
        while (value >= 1024 && index + 1 < units.length) {
            value /= 1024D;
            index++;
        }
        return String.format(Locale.ROOT, "%.2f %s", value, units[Math.max(index, 0)]);
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String encodePath(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    private String normalizeText(String text) {
        return text == null ? "" : text.trim();
    }

    private String sanitizeUploadedFilename(String originalName) {
        String normalized = normalizeText(originalName);
        if (normalized.isEmpty()) {
            return "file.bin";
        }
        String safe = normalized.replaceAll("[^A-Za-z0-9._-]", "_");
        if (safe.isEmpty() || ".".equals(safe) || "..".equals(safe)) {
            return "file.bin";
        }
        return safe;
    }

    private static class TransferFileView {
        private final String filename;
        private final String sizeText;
        private final String updatedAtText;

        private TransferFileView(String filename, String sizeText, String updatedAtText) {
            this.filename = filename;
            this.sizeText = sizeText;
            this.updatedAtText = updatedAtText;
        }
    }
}
