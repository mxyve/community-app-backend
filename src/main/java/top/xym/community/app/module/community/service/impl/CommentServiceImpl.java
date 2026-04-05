package top.xym.community.app.module.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import top.xym.community.app.model.dto.PageResponse;
import top.xym.community.app.module.community.mapper.CommentMapper;
import top.xym.community.app.module.community.model.dto.CommentCreateRequest;
import top.xym.community.app.module.community.model.dto.CommentPageRequest;
import top.xym.community.app.module.community.model.dto.UserCommentPageRequest;
import top.xym.community.app.module.community.model.entity.Comment;
import top.xym.community.app.module.community.model.vo.CommentVO;
import top.xym.community.app.module.community.model.vo.UserCommentVO;
import top.xym.community.app.module.community.service.CommentService;
import top.xym.community.app.module.myProfile.mapper.MyProfileMapper;
import top.xym.community.app.module.myProfile.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
            implements CommentService {

    private final CommentMapper commentMapper;

    private final MyProfileMapper myProfileMapper;

    /**
     * 发布评论
     */
    @Override
    public void createComment(CommentCreateRequest request, Long userId) {
        Comment comment = new Comment();

        // 1. 基本信息
        comment.setArticleId(request.getArticleId());
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setImg(request.getImg());

        // 2. 层级关系
        comment.setParentCommentId(request.getParentCommentId() == null ? 0 : request.getParentCommentId());
        comment.setToUserId(request.getToUserId() == null ? 0 : request.getToUserId());

        // 3. 初始值
        comment.setLikeCount(0L);
        comment.setReplyCount(0L);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        comment.setDeleted(0);

        // 4. 保存
        this.save(comment);

        // 文章评论数 +1
        commentMapper.incrArticleCommentCount(request.getArticleId());

        // 5. 如果是回复评论 → 父评论回复数 +1
        if (request.getParentCommentId() != null && request.getParentCommentId() > 0) {
            lambdaUpdate()
                    .setSql("reply_count = reply_count + 1")
                    .eq(Comment::getCommentId, request.getParentCommentId())
                    .update();
        }
    }

    public PageResponse<CommentVO> getCommentTreePage(CommentPageRequest request) {
        Long articleId = request.getArticleId();
        long current = request.getCurrent();
        long size = request.getSize();

        // 1. 分页查询顶级评论 parentCommentId=0
        LambdaQueryWrapper<Comment> pWrapper = new LambdaQueryWrapper<>();
        pWrapper.eq(Comment::getArticleId, articleId)
                .eq(Comment::getParentCommentId, 0)
                .eq(Comment::getDeleted, 0)
                .orderByDesc(Comment::getCreateTime);

        Page<Comment> pPage = new Page<>(current, size);
        Page<Comment> parentPage = commentMapper.selectPage(pPage, pWrapper);
        List<Comment> parentList = parentPage.getRecords();

        if (parentList.isEmpty()) {
            return new PageResponse<>(current, size, parentPage.getTotal(),
                    parentPage.getPages(), List.of());
        }

        // 2. 查询这篇文章 **所有非顶级评论**（无限层级）
        LambdaQueryWrapper<Comment> allChildWrapper = new LambdaQueryWrapper<>();
        allChildWrapper.eq(Comment::getArticleId, articleId)
                .ne(Comment::getParentCommentId, 0)
                .eq(Comment::getDeleted, 0)
                .orderByAsc(Comment::getCreateTime);
        List<Comment> allCommentList = commentMapper.selectList(allChildWrapper);

        // 3. 组装树形结构
        List<CommentVO> result = new ArrayList<>();
        for (Comment parent : parentList) {
            CommentVO vo = convert(parent);
            // 递归获取所有子孙评论
            List<CommentVO> children = buildChildrenRecursive(parent.getCommentId(), allCommentList);
            vo.setChildList(children);
            result.add(vo);
        }

        return new PageResponse<>(
                parentPage.getCurrent(),
                parentPage.getSize(),
                parentPage.getTotal(),
                parentPage.getPages(),
                result
        );
    }

    /**
     * 递归：获取一个评论下的所有子孙评论（无限层级）
     */
    private List<CommentVO> buildChildrenRecursive(Long parentId, List<Comment> allCommentList) {
        List<CommentVO> result = new ArrayList<>();
        for (Comment comment : allCommentList) {
            if (parentId.equals(comment.getParentCommentId())) {
                CommentVO vo = convert(comment);
                result.add(vo);
                result.addAll(buildChildrenRecursive(comment.getCommentId(), allCommentList));
            }
        }
        return result;
    }

    /**
     * Comment 转 CommentVO
     */
    private CommentVO convert(Comment comment) {
        CommentVO vo = new CommentVO();
        vo.setCommentId(comment.getCommentId());
        vo.setArticleId(comment.getArticleId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setImg(comment.getImg());
        vo.setLikeCount(comment.getLikeCount());
        vo.setReplyCount(comment.getReplyCount());
        vo.setCreateTime(comment.getCreateTime());
        vo.setToUserId(comment.getToUserId());

        User user = myProfileMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setNickName(user.getNickName());
            vo.setAvatar(user.getAvatar());
        }
        // 被回复的用户
        if (comment.getToUserId() != null && comment.getToUserId() > 0) {
            User toUser = myProfileMapper.selectById(comment.getToUserId());
            vo.setToUserName(toUser != null ? toUser.getNickName() : "");
        }

        return vo;
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        // 查询要删除的评论
        Comment comment = getById(commentId);
        if (comment == null || comment.getDeleted() == 1) {
            throw new RuntimeException("评论不存在");
        }
        // 只能删除自己的评论
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("智能删除自己的评论");
        }

        // 先拿到文章 ID（一定要在删除前拿）
        Long articleId = comment.getArticleId();
        // 如果是回复，父评论回复数 -1
        Long parentId = comment.getParentCommentId();

        // 查询要删除的评论总数（自己 + 所有子孙）
        int totalDeleteCount = getTotalCommentCount(commentId);

        // 递归删除该评论及其所有子孙回复
        removeCommentAndChildren(commentId);

        // 文章评论数 -1
        commentMapper.decrArticleCommentCountBatch(articleId, totalDeleteCount);


        if (parentId != null && parentId != 0) {
            lambdaUpdate()
                    .setSql("reply_count = reply_count - 1")
                    .eq(Comment::getCommentId, parentId)
                    .update();
        }
    }

    /**
     * 递归统计：当前评论 + 所有子孙评论的数量
     */
    private int getTotalCommentCount(Long commentId) {
        int count = 1; // 自己

        List<Comment> children = lambdaQuery()
                .eq(Comment::getParentCommentId, commentId)
                .eq(Comment::getDeleted, 0)
                .list();

        for (Comment child : children) {
            count += getTotalCommentCount(child.getCommentId());
        }

        return count;
    }

    /**
     * 递归删除当前评论及其所有子孙评论
     */
    private void removeCommentAndChildren(Long commentId) {
        // 先删除所有子评论
        List<Comment> children = lambdaQuery()
                .eq(Comment::getParentCommentId, commentId)
                .eq(Comment::getDeleted, 0)
                .list();

        for (Comment child : children) {
            removeCommentAndChildren(child.getCommentId());
        }

        // 再删自己
        lambdaUpdate()
                .eq(Comment::getCommentId, commentId)
                .set(Comment::getDeleted, 1)
                .update();
    }

    @Override
    public int countMyComments(Long userId) {
        return commentMapper.countUserComments(userId);
    }

    @Override
    public PageResponse<UserCommentVO> getMyCommentPage(UserCommentPageRequest request, Long userId) {
        long current = request.getCurrent();
        long size = request.getSize();
        int offset = (int) ((current - 1) * size);

        // 分页数据
        List<UserCommentVO> records = commentMapper.selectUserCommentPage(userId, offset, (int) size);

        // 总数
        int total = countMyComments(userId);

        return new PageResponse<>(current, size, (long) total,
                (total + size - 1) / size, records);
    }

}
