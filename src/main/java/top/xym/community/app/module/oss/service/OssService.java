package top.xym.community.app.module.oss.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.xym.community.app.common.exception.ServerException;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 适配你的OSS配置的图片上传服务
 */
@Service
@Slf4j
public class OssService {

    // 绑定你的yml配置（注意参数名和你的配置一致）
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @Value("${aliyun.oss.temp-dir}")
    private String tempDir;

    @Value("${aliyun.oss.url-expire}")
    private Integer urlExpire;

    @Value("${aliyun.oss.max-file-size}")
    private String maxFileSize;

    @Value("${aliyun.oss.allowed-types}")
    private String allowedTypes;

    /**
     * 核心：上传用户头像到你的OSS（南京节点）
     */
    public String uploadAvatar(MultipartFile file) {
        // 1. 基础校验
        if (file == null || file.isEmpty()) {
            throw new ServerException("头像文件不能为空");
        }

        // 2. 解析并校验文件大小（适配5MB配置）
        long maxSize = parseFileSize(maxFileSize);
        if (file.getSize() > maxSize) {
            throw new ServerException("头像大小不能超过" + maxFileSize);
        }

        // 3. 校验图片类型
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!checkFileSuffix(suffix)) {
            throw new ServerException("仅支持上传" + allowedTypes + "格式的图片");
        }

        // 4. 生成文件名（结合你的temp-dir配置）
        String fileName = tempDir + "/" + UUID.randomUUID() + suffix;

        // 5. 上传到你的OSS（南京节点）
        try (InputStream inputStream = file.getInputStream()) {
            // 创建OSS客户端（适配南京endpoint）
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 设置图片元数据（保证浏览器正常预览）
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(getContentType(suffix));
            metadata.setContentLength(file.getSize());

            // 上传到你的bucket
            ossClient.putObject(bucketName, fileName, inputStream, metadata);

            // 6. 生成带过期时间的访问URL（适配url-expire配置）
            Date expiration = new Date(System.currentTimeMillis() + urlExpire * 1000L);
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, fileName);
            request.setExpiration(expiration);
            URL presignedUrl = ossClient.generatePresignedUrl(request);

            ossClient.shutdown(); // 关闭客户端

            String avatarUrl = presignedUrl.toString();
            log.info("头像上传成功（南京OSS），URL：{}", avatarUrl);
            return avatarUrl;
        } catch (Exception e) {
            log.error("南京OSS头像上传失败", e);
            throw new ServerException("头像上传失败，请重试");
        }
    }

    /**
     * 辅助：解析文件大小配置（如5MB转字节）
     */
    private long parseFileSize(String sizeStr) {
        sizeStr = sizeStr.toUpperCase();
        long size = Long.parseLong(sizeStr.replaceAll("[^0-9]", ""));
        if (sizeStr.contains("KB")) {
            return size * 1024;
        } else if (sizeStr.contains("MB")) {
            return size * 1024 * 1024;
        } else if (sizeStr.contains("GB")) {
            return size * 1024 * 1024 * 1024;
        }
        return size;
    }

    /**
     * 辅助：校验文件后缀是否允许
     */
    private boolean checkFileSuffix(String suffix) {
        String[] allowTypes = allowedTypes.split(",");
        for (String type : allowTypes) {
            if (suffix.equals("." + type.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 辅助：设置图片MIME类型
     */
    private String getContentType(String suffix) {
        return switch (suffix) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }

    /**
     * 可选：生成普通公共读URL（无过期时间，需提前设置Bucket公共读）
     */
    public String getPublicUrl(String fileName) {
        return "https://" + bucketName + "." + endpoint + "/" + fileName;
    }

    /**
     * 单张聊天图片上传（永久公网 URL）
     */
    public String uploadChatImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServerException("图片文件不能为空");
        }
        // 大小、后缀校验同头像，可复用
        long maxSize = parseFileSize(maxFileSize);
        if (file.getSize() > maxSize) {
            throw new ServerException("图片大小不能超过" + maxFileSize);
        }
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!checkFileSuffix(suffix)) {
            throw new ServerException("仅支持" + allowedTypes + "格式");
        }

        // 放在 /chat/ 目录下，方便后续统一清理
        String fileName = "community/chat/" + UUID.randomUUID() + suffix;

        try (InputStream inputStream = file.getInputStream()) {
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(getContentType(suffix));
            metadata.setContentLength(file.getSize());
            // 上传
            ossClient.putObject(bucketName, fileName, inputStream, metadata);
            // 生成永久公网读链接（bucket 已公共读）
            String publicUrl = "https://" + bucketName + "." + endpoint + "/" + fileName;
            ossClient.shutdown();
            log.info("聊天图片上传成功：{}", publicUrl);
            return publicUrl;
        } catch (Exception e) {
            log.error("聊天图片上传失败", e);
            throw new ServerException("图片上传失败，请重试");
        }
    }

    /**
     * 批量上传聊天图片
     */
    public List<String> uploadChatImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        return files.stream()
                .map(this::uploadChatImage)   // 复用你已有的单文件方法
                .toList();
    }

    /**
     * 上传服务评论/评价图片
     */
    public String uploadCommentImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServerException("图片不能为空");
        }

        // 大小校验
        long maxSize = parseFileSize(maxFileSize);
        if (file.getSize() > maxSize) {
            throw new ServerException("图片不能超过" + maxFileSize);
        }

        // 格式校验
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!checkFileSuffix(suffix)) {
            throw new ServerException("仅支持：" + allowedTypes);
        }

        // 存储路径：comment/xxx.jpg
        String fileName = "community/service/comment/" + UUID.randomUUID() + suffix;

        try (InputStream inputStream = file.getInputStream()) {
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(getContentType(suffix));
            metadata.setContentLength(file.getSize());

            ossClient.putObject(bucketName, fileName, inputStream, metadata);
            String publicUrl = "https://" + bucketName + "." + endpoint + "/" + fileName;

            ossClient.shutdown();
            return publicUrl;
        } catch (Exception e) {
            log.error("评论图片上传失败", e);
            throw new ServerException("图片上传失败");
        }
    }
}