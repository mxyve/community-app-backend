package top.xym.community.app.module.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.xym.community.app.common.result.Result;
import top.xym.community.app.module.community.model.entity.Tag;
import top.xym.community.app.module.community.service.TagService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/community/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/list")
    @Operation(summary = "获取所有标签列表")
    public Result<List<Tag>> listAllTags() {
        return Result.success("查询成功", tagService.listAllTags());
    }
}
