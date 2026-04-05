package top.xym.community.app.module.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.xym.community.app.common.result.Result;
import top.xym.community.app.model.dto.PageResponse;
import top.xym.community.app.module.community.model.dto.CommentCreateRequest;
import top.xym.community.app.module.community.model.dto.CommentPageRequest;
import top.xym.community.app.module.community.model.dto.UserCommentPageRequest;
import top.xym.community.app.module.community.model.vo.CommentVO;
import top.xym.community.app.module.community.model.vo.UserCommentVO;
import top.xym.community.app.module.community.service.CommentService;
import top.xym.community.app.utils.SecurityUtils;

@RestController
@RequestMapping("/api/v1/community/comment")
@Tag(name = "评论接口")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "发布评论/回复")
    public Result<Void> createComment(@Valid @RequestBody CommentCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        commentService.createComment(request, userId);
        return Result.success("评论发布成功", null);
    }

    @PostMapping("/pages")
    @Operation(summary = "获取文章评论列表（两级树形结构）")
    public Result<PageResponse<CommentVO>> getCommentPage(
            @RequestBody CommentPageRequest request) {
        PageResponse<CommentVO> page = commentService.getCommentTreePage(request);
        return Result.success("查询成功", page);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "删除评论")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        Long UserId = SecurityUtils.getCurrentUserId();
        commentService.deleteComment(commentId, UserId);
        return Result.success("删除成功", null);
    }

    @GetMapping("/my/count")
    @Operation(summary = "我的评论总数")
    public Result<Integer> countMyComments() {
        Long UserId = SecurityUtils.getCurrentUserId();
        return Result.success(commentService.countMyComments(UserId));
    }

    @GetMapping("/my/page")
    @Operation(summary = "我的评论分页列表")
    public Result<PageResponse<UserCommentVO>> myCommentPage(UserCommentPageRequest request) {
        Long UserId = SecurityUtils.getCurrentUserId();
        return Result.success(commentService.getMyCommentPage(request, UserId));
    }
}