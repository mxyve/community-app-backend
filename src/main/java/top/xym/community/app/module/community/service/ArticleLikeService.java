package top.xym.community.app.module.community.service;

import top.xym.community.app.module.community.model.dto.ArticleLikeResponse;

public interface ArticleLikeService {
    ArticleLikeResponse likeOrCancel(Integer articleId, Long userId);
}
