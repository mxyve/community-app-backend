package top.xym.community.app.module.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.xym.community.app.module.community.mapper.TagMapper;
import top.xym.community.app.module.community.model.entity.Tag;

import java.util.List;

@Slf4j
@Service
public class TagService {

    @Autowired
    private TagMapper tagMapper;

    public List<Tag> listAllTags() {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getDeleted, 0);
        return tagMapper.selectList(wrapper);
    }
}
