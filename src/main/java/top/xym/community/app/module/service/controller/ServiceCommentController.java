package top.xym.community.app.module.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.xym.community.app.common.result.Result;
import top.xym.community.app.model.dto.PageResponse;
import top.xym.community.app.module.oss.service.OssService;
import top.xym.community.app.module.service.model.dto.ServiceCommentCreateRequest;
import top.xym.community.app.module.service.model.dto.ServiceCommentPageRequest;
import top.xym.community.app.module.service.model.vo.ServiceCommentVO;
import top.xym.community.app.module.service.service.ServiceCommentService;
import top.xym.community.app.utils.SecurityUtils;

@RestController
@RequestMapping("/api/v1/service/comment")
@Tag(name = "服务评论接口")
@RequiredArgsConstructor
public class ServiceCommentController {

    private final ServiceCommentService serviceCommentService;
    private final OssService ossService;

    /**
     * 发布评论/评价/回复（共用接口）
     */
    @PostMapping
    @Operation(summary = "发布服务评论/评价/回复")
    public Result<Void> createComment(@Valid @RequestBody ServiceCommentCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        serviceCommentService.createComment(request, userId);
        return Result.success("发布成功", null);
    }

    /**
     * 树形评论列表
     */
    @PostMapping("/pages")
    @Operation(summary = "获取服务评论树形列表")
    public Result<PageResponse<ServiceCommentVO>> getCommentPage(
            @RequestBody ServiceCommentPageRequest request) {
        PageResponse<ServiceCommentVO> page = serviceCommentService.getCommentTreePage(request);
        return Result.success(page);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{commentId}")
    @Operation(summary = "删除评论")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        Long userId = SecurityUtils.getCurrentUserId();
        serviceCommentService.deleteComment(commentId, userId);
        return Result.success("删除成功", null);
    }

    /**
     * 根据订单ID查询当前用户的评价
     */
    @GetMapping("/order/{orderId}")
    @Operation(summary = "根据订单ID查询评价")
    public Result<ServiceCommentVO> getCommentByOrderId(@PathVariable Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        ServiceCommentVO comment = serviceCommentService.getByOrderId(orderId, userId);
        return Result.success(comment);
    }

    /**
     * 获取我的评价列表
     */
    @GetMapping("/my/list")
    @Operation(summary = "我的评价列表")
    public Result<PageResponse<ServiceCommentVO>> getMyCommentList(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Long userId = SecurityUtils.getCurrentUserId();
        PageResponse<ServiceCommentVO> page = serviceCommentService.getMyCommentList(userId, current, size);
        return Result.success(page);
    }

    /**
     * 评论/评价图片上传
     */
    @PostMapping("/upload/picture")
    @Operation(summary = "上传评论/评价图片")
    public Result<String> uploadCommentImage(@RequestParam("file") MultipartFile file) {
        String url = ossService.uploadCommentImage(file);
        return Result.success(url);
    }
}