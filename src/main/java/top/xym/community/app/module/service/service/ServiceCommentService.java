package top.xym.community.app.module.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.xym.community.app.mapper.UserMapper;
import top.xym.community.app.model.dto.PageResponse;
import top.xym.community.app.model.entity.User;
import top.xym.community.app.module.service.mapper.ServiceCommentMapper;
import top.xym.community.app.module.service.model.dto.ServiceCommentCreateRequest;
import top.xym.community.app.module.service.model.dto.ServiceCommentPageRequest;
import top.xym.community.app.module.service.model.entity.ServiceComment;
import top.xym.community.app.module.service.model.vo.ServiceCommentVO;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceCommentService {

    private final ServiceCommentMapper serviceCommentMapper;
    private final UserMapper userMapper;

    /**
     * 发布评论 / 订单评价 / 回复
     */
    public void createComment(ServiceCommentCreateRequest request, Long userId) {
        ServiceComment comment = new ServiceComment();

        comment.setServiceId(request.getServiceId());
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setImg(request.getImg());
        comment.setMerchantId(request.getMerchantId());

        Long parentId = request.getParentCommentId() == null ? 0 : request.getParentCommentId();
        comment.setParentCommentId(parentId);
        comment.setToUserId(request.getToUserId() == null ? 0 : request.getToUserId());

        comment.setLikeCount(0L);
        comment.setReplyCount(0L);
        comment.setDeleted(0);

        // 一级评论 = 订单评价（必须评分 + 订单）
        if (parentId == 0) {
            if (request.getOrderId() == null) {
                throw new RuntimeException("订单ID不能为空");
            }
            if (request.getStar() == null) {
                throw new RuntimeException("评分必须为1-5星");
            }
            comment.setOrderId(request.getOrderId());
            comment.setStar(request.getStar());
        } else {
            // 回复评论 不允许评分
            comment.setOrderId(null);
            comment.setStar(null);
        }

        serviceCommentMapper.insert(comment);

        // 父评论回复数 + 1
        if (parentId > 0) {
            serviceCommentMapper.updateById(
                    new ServiceComment() {{
                        setId(parentId);
                        setReplyCount(serviceCommentMapper.selectById(parentId).getReplyCount() + 1);
                    }}
            );
        }
    }

    /**
     * 评论树形列表
     */
    public PageResponse<ServiceCommentVO> getCommentTreePage(ServiceCommentPageRequest request) {
        Long serviceId = request.getServiceId();
        long current = request.getCurrent();
        long size = request.getSize();

        // 查一级评论
        LambdaQueryWrapper<ServiceComment> pWrapper = new LambdaQueryWrapper<>();
        pWrapper.eq(ServiceComment::getServiceId, serviceId)
                .eq(ServiceComment::getParentCommentId, 0)
                .eq(ServiceComment::getDeleted, 0)
                .orderByDesc(ServiceComment::getCreateTime);

        Page<ServiceComment> pPage = new Page<>(current, size);
        Page<ServiceComment> parentPage = serviceCommentMapper.selectPage(pPage, pWrapper);
        List<ServiceComment> parentList = parentPage.getRecords();

        if (parentList.isEmpty()) {
            return new PageResponse<>(current, size, parentPage.getTotal(), parentPage.getPages(), List.of());
        }

        // 查所有子评论
        LambdaQueryWrapper<ServiceComment> allChildWrapper = new LambdaQueryWrapper<>();
        allChildWrapper.eq(ServiceComment::getServiceId, serviceId)
                .ne(ServiceComment::getParentCommentId, 0)
                .eq(ServiceComment::getDeleted, 0)
                .orderByAsc(ServiceComment::getCreateTime);
        List<ServiceComment> allCommentList = serviceCommentMapper.selectList(allChildWrapper);

        // 组装树形
        List<ServiceCommentVO> result = new ArrayList<>();
        for (ServiceComment parent : parentList) {
            ServiceCommentVO vo = convert(parent);
            List<ServiceCommentVO> children = buildChildrenRecursive(parent.getId(), allCommentList);
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
     * 递归组装子评论
     */
    private List<ServiceCommentVO> buildChildrenRecursive(Long parentId, List<ServiceComment> allCommentList) {
        List<ServiceCommentVO> result = new ArrayList<>();
        for (ServiceComment comment : allCommentList) {
            if (parentId.equals(comment.getParentCommentId())) {
                ServiceCommentVO vo = convert(comment);
                result.add(vo);
                result.addAll(buildChildrenRecursive(comment.getId(), allCommentList));
            }
        }
        return result;
    }

    /**
     * 转为VO
     */
    private ServiceCommentVO convert(ServiceComment comment) {
        ServiceCommentVO vo = new ServiceCommentVO();
        vo.setId(comment.getId());
        vo.setServiceId(comment.getServiceId());
        vo.setUserId(comment.getUserId());
        vo.setStar(comment.getStar());
        vo.setContent(comment.getContent());
        vo.setImg(comment.getImg());
        vo.setParentCommentId(comment.getParentCommentId());
        vo.setToUserId(comment.getToUserId());
        vo.setReplyCount(comment.getReplyCount());
        vo.setLikeCount(comment.getLikeCount());
        vo.setCreateTime(comment.getCreateTime());

        // 评论人信息
        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setNickName(user.getNickName());
            vo.setAvatar(user.getAvatar());
        }

        // 被回复人
        if (comment.getToUserId() != null && comment.getToUserId() > 0) {
            User toUser = userMapper.selectById(comment.getToUserId());
            vo.setToUserName(toUser != null ? toUser.getNickName() : "");
        }

        return vo;
    }

    /**
     * 删除评论（含子孙）
     */
    public void deleteComment(Long commentId, Long userId) {
        ServiceComment comment = getById(commentId);
        if (comment == null || comment.getDeleted() == 1) {
            throw new RuntimeException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己的评论");
        }

        // 递归删除
        removeCommentAndChildren(commentId);

        // 父评论回复数 -1
        if (comment.getParentCommentId() > 0) {
            ServiceComment parent = getById(comment.getParentCommentId());
            if (parent != null) {
                parent.setReplyCount(parent.getReplyCount() - 1);
                serviceCommentMapper.updateById(parent);
            }
        }

    }

    /**
     * 递归删除
     */
    private void removeCommentAndChildren(Long commentId) {
        List<ServiceComment> children = listByParentId(commentId);
        for (ServiceComment child : children) {
            removeCommentAndChildren(child.getId());
        }

        LambdaUpdateWrapper<ServiceComment> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ServiceComment::getId, commentId)
                .set(ServiceComment::getDeleted, 1);
        serviceCommentMapper.update(null, wrapper);
    }

    // ===================== 工具方法 =====================
    public ServiceComment getById(Long id) {
        return serviceCommentMapper.selectById(id);
    }

    public List<ServiceComment> listByParentId(Long parentId) {
        LambdaQueryWrapper<ServiceComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceComment::getParentCommentId, parentId).eq(ServiceComment::getDeleted, 0);
        return serviceCommentMapper.selectList(wrapper);
    }

    /**
     * 根据订单ID + 用户ID 查询评价
     */
    public ServiceCommentVO getByOrderId(Long orderId, Long userId) {
        LambdaQueryWrapper<ServiceComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceComment::getOrderId, orderId)
                .eq(ServiceComment::getUserId, userId)
                // 只查一级评价
                .eq(ServiceComment::getParentCommentId, 0)
                .eq(ServiceComment::getDeleted, 0)
                .last("LIMIT 1");

        ServiceComment comment = serviceCommentMapper.selectOne(wrapper);
        return comment == null ? null : convert(comment);
    }

    // 我的评价列表
    public PageResponse<ServiceCommentVO> getMyCommentList(Long userId, long current, long size) {
        LambdaQueryWrapper<ServiceComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceComment::getUserId, userId)
                .eq(ServiceComment::getParentCommentId, 0) // 只查自己发的一级评价
                .eq(ServiceComment::getDeleted, 0)
                .orderByDesc(ServiceComment::getCreateTime);

        Page<ServiceComment> page = new Page<>(current, size);
        Page<ServiceComment> resultPage = serviceCommentMapper.selectPage(page, wrapper);

        List<ServiceCommentVO> records = resultPage.getRecords().stream()
                .map(this::convert)
                .toList();

        return new PageResponse<>(
                current,
                size,
                resultPage.getTotal(),
                resultPage.getPages(),
                records
        );
    }
}