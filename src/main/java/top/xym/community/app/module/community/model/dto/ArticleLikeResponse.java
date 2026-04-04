package top.xym.community.app.module.community.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleLikeResponse {
    /**
     * 当前是否已点赞
     */
    private Boolean isLiked;
    /**
     * 文章最新点赞数
     */
    private Long likeCount;
}