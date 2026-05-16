package top.xym.community.app.module.message.tool;

import com.alibaba.fastjson2.JSON;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import top.xym.community.app.model.dto.PageResponse;
import top.xym.community.app.module.community.model.entity.Article;
import top.xym.community.app.module.community.service.ArticleService;
import top.xym.community.app.utils.SecurityUtils;

@Component
public class CommunityTool {

    private final ArticleService articleService;

    public CommunityTool(ArticleService articleService) {
        this.articleService = articleService;
    }

    // 搜索邻里圈帖子
    @Tool(description = "搜索社区帖子：失物招领、邻里互助、二手闲置、房屋租赁、生活分享、美食宠物等")
    public String searchCommunityPosts(
            Long userId,
            @ToolParam(description = "搜索关键词") String keyword,
            @ToolParam(description = "标签ID：1=失物招领 2=二手交易 3=邻里互助") Integer tagId
    ) {
        userId = SecurityUtils.getCurrentUserId();
        PageResponse<Article> result = articleService.searchPosts(keyword, tagId);
        return JSON.toJSONString(result);
    }
}