package top.xym.community.app.module.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.xym.community.app.module.community.mapper.ArticleLikeMapper;
import top.xym.community.app.module.community.mapper.ArticleMapper;
import top.xym.community.app.module.community.model.dto.ArticleLikeResponse;
import top.xym.community.app.module.community.model.entity.Article;
import top.xym.community.app.module.community.model.entity.ArticleLike;
import top.xym.community.app.module.community.service.ArticleLikeService;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleLikeServiceImpl implements ArticleLikeService {

    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleMapper articleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleLikeResponse likeOrCancel(Integer articleId, Long userId) {
        // 检验文章是否存在
        Article article = articleMapper.selectOne(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getArticleId, articleId)
                        .eq(Article::getDeleted, 0)
                        .eq(Article::getStatus, 1)
        );
        if (article == null) {
            throw new RuntimeException("文章不存在或已删除");
        }

        Integer uid = Math.toIntExact(userId);
        // 查询用户点赞记录
        LambdaQueryWrapper<ArticleLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(ArticleLike::getArticleId, articleId)
                   .eq(ArticleLike::getUserId, uid);
        ArticleLike likeRecord = articleLikeMapper.selectOne(likeWrapper);

        boolean isLiked;
        if (likeRecord == null) {
            // 情况1：无记录 → 首次点赞
            ArticleLike newLike = new ArticleLike();
            newLike.setArticleId(articleId);
            newLike.setUserId(uid);
            newLike.setCreateTime(LocalDateTime.now());
            newLike.setUpdateTime(LocalDateTime.now());
            newLike.setDeleted(0);
            articleLikeMapper.insert(newLike);

            // 文章点赞数+1
            article.setLikeCount(article.getLikeCount() + 1);
            article.setUpdateTime(LocalDateTime.now());
            articleMapper.updateById(article);
            isLiked = true;
        } else {
            if (likeRecord.getDeleted() == 0) {
                // 情况2：已点赞 → 取消点赞
                likeRecord.setDeleted(1);
                likeRecord.setUpdateTime(LocalDateTime.now());
                articleLikeMapper.updateById(likeRecord);

                // 文章点赞数-1
                article.setLikeCount(article.getLikeCount() - 1);
                article.setUpdateTime(LocalDateTime.now());
                articleMapper.updateById(article);
                isLiked = false;
            } else {
                // 情况3：已取消 → 重新点赞
                likeRecord.setDeleted(0);
                likeRecord.setUpdateTime(LocalDateTime.now());
                articleLikeMapper.updateById(likeRecord);

                // 文章点赞数+1
                article.setLikeCount(article.getLikeCount() + 1);
                article.setUpdateTime(LocalDateTime.now());
                articleMapper.updateById(article);
                isLiked = true;
            }
        }
        // 3. 返回最新状态
        return new ArticleLikeResponse(isLiked, article.getLikeCount());
    }
}
