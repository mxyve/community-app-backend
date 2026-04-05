package top.xym.community.app.module.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.xym.community.app.module.community.model.entity.Comment;
import top.xym.community.app.module.community.model.vo.UserCommentVO;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Update("UPDATE t_article SET comment_count = comment_count + 1 WHERE article_id = #{articleId}")
    void incrArticleCommentCount(@Param("articleId") Long articleId);

    @Update("UPDATE t_article SET comment_count = comment_count - #{count} WHERE article_id = #{articleId} AND comment_count >= #{count}")
    void decrArticleCommentCountBatch(@Param("articleId") Long articleId, @Param("count") int count);

    // 查询我的评论总数
    @Select("SELECT COUNT(*) FROM t_comment WHERE user_id = #{userId} AND deleted = 0")
    int countUserComments(@Param("userId") Long userId);

    // 分页查询我的评论（带文章标题）
    @Select("SELECT " +
            "c.comment_id, c.article_id, c.content, c.create_time, " +
            "a.title AS article_title " +
            "FROM t_comment c " +
            "LEFT JOIN t_article a ON c.article_id = a.article_id " +
            "WHERE c.user_id = #{userId} AND c.deleted = 0 " +
            "ORDER BY c.create_time DESC " +
            "LIMIT #{offset}, #{size}")
    List<UserCommentVO> selectUserCommentPage(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("size") int size
    );
}
