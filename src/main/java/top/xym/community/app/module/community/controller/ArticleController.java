package top.xym.community.app.module.community.controller;

import cn.hutool.core.lang.func.VoidFunc;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.xym.community.app.common.result.Result;
import top.xym.community.app.model.dto.PageResponse;
import top.xym.community.app.module.community.model.dto.ArticleCreateRequest;
import top.xym.community.app.module.community.model.dto.ArticlePageRequest;
import top.xym.community.app.module.community.model.entity.Article;
import top.xym.community.app.module.community.service.ArticleService;

import static top.xym.community.app.utils.SecurityUtils.getCurrentUserId;

@RestController
@RequestMapping("/api/v1/community/article")
@Tag(name = "社区文章管理")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 创建文章
     */
    @PostMapping
    @Operation(summary = "创建文章")
    public Result<Void> createArticle(@Valid @RequestBody ArticleCreateRequest request) {
        Long userId = getCurrentUserId();
        articleService.createArticle(request, userId);
        return Result.success("发布成功", null);
    }

    /**
     * 删除文
     */
    @DeleteMapping("/{articleId}")
    @Operation(summary = "删除文章")
    public Result<Void> deleteArticle(@PathVariable Long articleId) {
        Long userId = getCurrentUserId();
        articleService.deleteArticle(articleId, userId);
        return Result.success("删除成功", null);
    }

    /**
     * 分页列表接口
     */
    @PostMapping("/pages")
    @Operation(summary = "分页获取文章列表（自动按用户地区筛选）")
    public Result<PageResponse<Article>> getArticlePage(@RequestBody ArticlePageRequest request) {
        PageResponse<Article> pageResponse = articleService.getArticlePage(request);
        return Result.success("查询成功", pageResponse);
    }

}
