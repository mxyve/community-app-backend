package top.xym.community.app.module.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.xym.community.app.model.dto.PageResponse;
import top.xym.community.app.module.community.mapper.ArticleLikeMapper;
import top.xym.community.app.module.community.model.entity.ArticleLike;
import top.xym.community.app.module.myProfile.model.User;
import top.xym.community.app.module.community.mapper.ArticleMapper;
import top.xym.community.app.module.community.mapper.TagMapper;
import top.xym.community.app.module.community.model.dto.ArticleCreateRequest;
import top.xym.community.app.module.community.model.dto.ArticlePageRequest;
import top.xym.community.app.module.community.model.entity.Article;
import top.xym.community.app.module.community.model.entity.Tag;
import top.xym.community.app.module.myProfile.mapper.MyProfileMapper;
import top.xym.community.app.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private MyProfileMapper myProfileMapper;

    @Autowired
    private ArticleLikeMapper articleLikeMapper;

    /**
     * 创建文章
     */
    public void createArticle(ArticleCreateRequest request, Long userId) {
        Article article = new Article();
        article.setUserId(Math.toIntExact(userId));
        article.setTagId(request.getTagId());
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setImg(request.getImg());
        article.setStatus(2);
        article.setProvince(request.getProvince());
        article.setCity(request.getCity());
        article.setArea(request.getArea());
        article.setViewCount(0L);
        article.setLikeCount(0L);
        article.setCommentCount(0L);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        article.setDeleted(0);

        articleMapper.insert(article);

    }

    /**
     * 删除文章
     */
    public void deleteArticle(Long articleId, Long userId) {
        Article article =articleMapper.selectOne(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getArticleId, articleId)
                        .eq(Article::getUserId, userId)
                        .eq(Article::getDeleted, 0)
        );

        if (article == null) {
            throw new RuntimeException("文章不存在");
        }

        // 逻辑删除
        article.setDeleted(1);
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(article);

    }

    public PageResponse<Article> getArticlePage(ArticlePageRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = myProfileMapper.selectById(userId);
        String userProvince = user.getProvince();
        String userCity = user.getCity();
        String userDistrict = user.getDistrict();

        // 构建查询条件
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();

        if (userProvince != null && !userProvince.isEmpty()) {
            wrapper.eq(Article::getProvince, userProvince);
        }

        if (userCity != null && !userCity.isEmpty()) {
            wrapper.eq(Article::getCity, userCity);
        }

        if (userDistrict != null && !userDistrict.isEmpty()) {
            wrapper.eq(Article::getArea, userDistrict);
        }

        if (request.getTagId() != null) {
            wrapper.eq(Article::getTagId, request.getTagId());
        }

        wrapper.eq(Article::getDeleted, 0);
        wrapper.eq(Article::getStatus, 1);
        wrapper.orderByDesc(Article::getCreateTime);

        Page<Article> page = new Page<>(request.getCurrent(), request.getSize());
        Page<Article> resultPage = articleMapper.selectPage(page, wrapper);

        // 封装返回
        List<Article> records = resultPage.getRecords();

        for (Article article : records) {
            Tag tag = tagMapper.selectById(article.getTagId());
            if (tag != null) {
                article.setTagName(tag.getName());
                article.setTagColor(tag.getColor());
                article.setIcon(tag.getIcon());
            }
            user = myProfileMapper.selectById(article.getUserId());
            if (user != null) {
                article.setNickName(user.getNickName());
                article.setAvatar(user.getAvatar());
                article.setUsername(user.getUsername());
            }

            LambdaQueryWrapper<ArticleLike> likeQuery = new LambdaQueryWrapper<>();
            likeQuery.eq(ArticleLike::getArticleId, article.getArticleId())
                     .eq(ArticleLike::getUserId, Math.toIntExact(userId));
            ArticleLike like = articleLikeMapper.selectOne(likeQuery);

            // 赋值 isLiked
            if (like == null) {
                article.setIsLiked(false);
            } else {
                article.setIsLiked(like.getDeleted() == 0);
            }
        }

        return new PageResponse<>(
                resultPage.getCurrent(),
                resultPage.getSize(),
                resultPage.getTotal(),
                resultPage.getPages(),
                records
        );
    }

    /**
     * 获取文章详情
     */
    public Article getArticleDetail(Integer articleId) {
        Long userId = SecurityUtils.getCurrentUserId();

        Article article = articleMapper.selectOne(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getArticleId, articleId)
                        .eq(Article::getDeleted, 0)
                        .eq(Article::getStatus, 1)
        );

        if (article == null) {
            throw new RuntimeException("文章不存在或已被删除");
        }

        Tag tag = tagMapper.selectById(article.getTagId());
        if (tag != null) {
            article.setTagName(tag.getName());
            article.setTagColor(tag.getColor());
            article.setIcon(tag.getIcon());
        }

        User user = myProfileMapper.selectById(article.getUserId());
        if (user != null) {
            article.setNickName(user.getNickName());
            article.setAvatar(user.getAvatar());
            article.setUsername(user.getUsername());
        }

        // 查询是否点赞
        LambdaQueryWrapper<ArticleLike> likeQuery = new LambdaQueryWrapper<>();
        likeQuery.eq(ArticleLike::getArticleId, articleId)
                .eq(ArticleLike::getUserId, Math.toIntExact(userId));
        ArticleLike like = articleLikeMapper.selectOne(likeQuery);

        if (like == null) {
            article.setIsLiked(false);
        } else {
            article.setIsLiked(like.getDeleted() == 0);
        }

        return article;
    }


    /**
     * 获取当前用户点赞的文章列表（分页）
     */
    public PageResponse<Article> getMyLikeArticlePage(ArticlePageRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 1. 查询当前用户所有点赞记录（deleted=0 才是有效点赞）
        LambdaQueryWrapper<ArticleLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(ArticleLike::getUserId, Math.toIntExact(userId))
                .eq(ArticleLike::getDeleted, 0)
                .orderByDesc(ArticleLike::getCreateTime);

        Page<ArticleLike> likePage = new Page<>(request.getCurrent(), request.getSize());
        Page<ArticleLike> likeResult = articleLikeMapper.selectPage(likePage, likeWrapper);
        List<ArticleLike> likeList = likeResult.getRecords();

        if (likeList.isEmpty()) {
            return new PageResponse<>(
                    likeResult.getCurrent(),
                    likeResult.getSize(),
                    likeResult.getTotal(),
                    likeResult.getPages(),
                    List.of()
            );
        }

        // 2. 批量获取文章ID
        List<Integer> articleIds = likeList.stream()
                .map(ArticleLike::getArticleId)
                .toList();

        // 3. 查询文章详情
        LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
        articleWrapper.in(Article::getArticleId, articleIds)
                .eq(Article::getDeleted, 0)
                .eq(Article::getStatus, 1);

        List<Article> records = articleMapper.selectList(articleWrapper);

        // 4. 封装标签、用户信息、isLiked=true
        for (Article article : records) {
            Tag tag = tagMapper.selectById(article.getTagId());
            if (tag != null) {
                article.setTagName(tag.getName());
                article.setTagColor(tag.getColor());
                article.setIcon(tag.getIcon());
            }
            // 已点赞
            article.setIsLiked(true);
        }

        return new PageResponse<>(
                likeResult.getCurrent(),
                likeResult.getSize(),
                likeResult.getTotal(),
                likeResult.getPages(),
                records
        );
    }

    /**
     * 获取当前用户发布的文章列表（分页）
     */
    public PageResponse<Article> getMyArticlePage(ArticlePageRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 只查当前用户自己发布的文章
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getUserId, Math.toIntExact(userId))
                .eq(Article::getDeleted, 0)
                .eq(Article::getStatus, 1)
                .orderByDesc(Article::getCreateTime);

        Page<Article> page = new Page<>(request.getCurrent(), request.getSize());
        Page<Article> resultPage = articleMapper.selectPage(page, wrapper);
        List<Article> records = resultPage.getRecords();

        // 封装标签
        for (Article article : records) {
            Tag tag = tagMapper.selectById(article.getTagId());
            if (tag != null) {
                article.setTagName(tag.getName());
                article.setTagColor(tag.getColor());
                article.setIcon(tag.getIcon());
            }

            // 查询是否点赞
            LambdaQueryWrapper<ArticleLike> likeQuery = new LambdaQueryWrapper<>();
            likeQuery.eq(ArticleLike::getArticleId, article.getArticleId())
                    .eq(ArticleLike::getUserId, Math.toIntExact(userId));
            ArticleLike like = articleLikeMapper.selectOne(likeQuery);
            article.setIsLiked(like != null && like.getDeleted() == 0);
        }

        return new PageResponse<>(
                resultPage.getCurrent(),
                resultPage.getSize(),
                resultPage.getTotal(),
                resultPage.getPages(),
                records
        );
    }

    /**
     * 统计：当前用户点赞的文章数量
     */
    public Long countMyLikeArticles() {
        Long userId = SecurityUtils.getCurrentUserId();

        // 先查用户点赞记录（有效点赞）
        LambdaQueryWrapper<ArticleLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(ArticleLike::getUserId, Math.toIntExact(userId))
                .eq(ArticleLike::getDeleted, 0);
        List<ArticleLike> likeList = articleLikeMapper.selectList(likeWrapper);

        if (likeList.isEmpty()) return 0L;

        // 只统计 article 中 deleted=0 且 status=1 的
        List<Integer> articleIds = likeList.stream().map(ArticleLike::getArticleId).toList();
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Article::getArticleId, articleIds)
                .eq(Article::getDeleted, 0)
                .eq(Article::getStatus, 1);

        return articleMapper.selectCount(wrapper);
    }

    /**
     * 统计：当前用户发布的文章数量
     */
    public Long countMyPostArticles() {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getUserId, Math.toIntExact(userId))
                .eq(Article::getDeleted, 0)
                .eq(Article::getStatus, 1);
        return articleMapper.selectCount(wrapper);
    }

    /**
     * 统计：今日发布的帖子数量（有效文章）
     */
    public Long countTodayPosts() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0)
                .eq(Article::getStatus, 1)
                // ≥
                .ge(Article::getCreateTime, todayStart)
                // <
                .lt(Article::getCreateTime, tomorrowStart);

        return articleMapper.selectCount(wrapper);
    }

}
