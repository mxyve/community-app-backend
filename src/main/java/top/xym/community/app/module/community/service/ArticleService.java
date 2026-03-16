package top.xym.community.app.module.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.xym.community.app.mapper.UserMapper;
import top.xym.community.app.model.dto.PageResponse;
import top.xym.community.app.model.entity.User;
import top.xym.community.app.module.community.mapper.ArticleMapper;
import top.xym.community.app.module.community.mapper.TagMapper;
import top.xym.community.app.module.community.model.dto.ArticleCreateRequest;
import top.xym.community.app.module.community.model.dto.ArticlePageRequest;
import top.xym.community.app.module.community.model.entity.Article;
import top.xym.community.app.module.community.model.entity.Tag;
import top.xym.community.app.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TagMapper tagMapper;

    /**
     * 创建会话
     */
    public void createArticle(ArticleCreateRequest request, Long userId) {
        Article article = new Article();
        article.setUserId(Math.toIntExact(userId));
        article.setTagId(request.getTagId());
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setImg(request.getImg());
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
        User user = userMapper.selectById(userId);
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

}
