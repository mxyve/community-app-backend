package top.xym.community.app.module.community.controller;



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

    /**
     * 文章详情
     */
    @GetMapping("/{articleId}")
    @Operation(summary = "获取文章详情")
    public Result<Article> getArticleDetail(@PathVariable Integer articleId) {
        Article article = articleService.getArticleDetail(articleId);
        return Result.success("查询成功", article);
    }

    /**
     * 获取当前用户点赞的文章列表
     */
    @PostMapping("/myLike/pages")
    @Operation(summary = "获取当前用户点赞的文章列表（分页）")
    public Result<PageResponse<Article>> getMyLikeArticlePage(@RequestBody ArticlePageRequest request) {
        PageResponse<Article> pageResponse = articleService.getMyLikeArticlePage(request);
        return Result.success("查询成功", pageResponse);
    }

    /**
     * 获取当前用户发布的文章列表（分页）
     */
    @PostMapping("/my/post/pages")
    @Operation(summary = "获取当前用户发布的文章列表（分页）")
    public Result<PageResponse<Article>> getMyArticlePage(@RequestBody ArticlePageRequest request) {
        PageResponse<Article> pageResponse = articleService.getMyArticlePage(request);
        return Result.success("查询成功", pageResponse);
    }

    /**
     * 统计：当前用户点赞的文章总数
     */
    @GetMapping("/count/my-like")
    @Operation(summary = "统计用户点赞的文章数量")
    public Result<Long> countMyLikeArticles() {
        Long count = articleService.countMyLikeArticles();
        return Result.success("查询成功", count);
    }

    /**
     * 统计：当前用户发布的文章总数
     */
    @GetMapping("/count/my-post")
    @Operation(summary = "统计用户发布的文章数量")
    public Result<Long> countMyPostArticles() {
        Long count = articleService.countMyPostArticles();
        return Result.success("查询成功", count);
    }

    /**
     * 统计：今日发布的帖子总数
     */
    @GetMapping("/count/today")
    @Operation(summary = "统计今日发布的帖子数量")
    public Result<Long> countTodayPosts() {
        Long count = articleService.countTodayPosts();
        return Result.success("查询成功", count);
    }

}
