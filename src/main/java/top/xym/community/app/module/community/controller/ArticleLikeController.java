package top.xym.community.app.module.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.xym.community.app.common.result.Result;
import top.xym.community.app.module.community.model.dto.ArticleLikeResponse;
import top.xym.community.app.module.community.service.ArticleLikeService;

import static top.xym.community.app.utils.SecurityUtils.getCurrentUserId;

@RestController
@RequestMapping("/api/v1/community/article")
@Tag(name = "社区文章点赞")
@RequiredArgsConstructor
public class ArticleLikeController {

    private final ArticleLikeService articleLikeService;

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/{articleId}/like")
    @Operation(summary = "文章点赞/取消点赞")
    public Result<ArticleLikeResponse> likeOrCancel(@PathVariable Integer articleId) {
        Long userId = getCurrentUserId();
        ArticleLikeResponse response = articleLikeService.likeOrCancel(articleId, userId);
        return Result.success("操作成功", response);
    }
}