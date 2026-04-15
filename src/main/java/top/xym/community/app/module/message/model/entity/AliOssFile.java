package top.xym.community.app.module.message.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ali_oss_file")
public class AliOssFile {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileName;
    private String url;
    private String vectorId;
    private Integer status; // 0启用 1禁用
    private LocalDate createTime;
    private LocalDate updateTime;
}