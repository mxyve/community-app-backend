package top.xym.community.app.module.message.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.xym.community.app.module.message.mapper.AliOssFileMapper;
import top.xym.community.app.module.message.model.entity.AliOssFile;
import top.xym.community.app.module.message.service.AliOssFileService;

import java.util.List;

@Service
public class AliOssFileServiceImpl extends ServiceImpl<AliOssFileMapper, AliOssFile>
        implements AliOssFileService {

    @Override
    public List<String> listEnabledVectorIds() {
        // 查询所有启用状态(0)的知识库文件
        List<AliOssFile> files = this.lambdaQuery()
                .eq(AliOssFile::getStatus, 0)
                .list();

        // 把所有启用的 vectorId 展开成一个大列表
        return files.stream()
                .flatMap(file -> JSON.parseArray(file.getVectorId(), String.class).stream())
                .toList();
    }
}