package top.xym.community.app.module.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.xym.community.app.module.community.model.entity.Article;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
