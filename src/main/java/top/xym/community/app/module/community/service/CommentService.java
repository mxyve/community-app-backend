package top.xym.community.app.module.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.xym.community.app.model.dto.PageResponse;
import top.xym.community.app.module.community.model.dto.CommentCreateRequest;
import top.xym.community.app.module.community.model.dto.CommentPageRequest;
import top.xym.community.app.module.community.model.dto.UserCommentPageRequest;
import top.xym.community.app.module.community.model.entity.Comment;
import top.xym.community.app.module.community.model.vo.CommentVO;
import top.xym.community.app.module.community.model.vo.UserCommentVO;

public interface CommentService extends IService<Comment> {

    void createComment(CommentCreateRequest request, Long userId);

    PageResponse<CommentVO> getCommentTreePage(CommentPageRequest request);

    void deleteComment(Long commentId, Long userId);

    // 统计我的评论数
    int countMyComments(Long userId);

    // 我的评论分页列表
    PageResponse<UserCommentVO> getMyCommentPage(UserCommentPageRequest request, Long userId);

}
