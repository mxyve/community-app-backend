package top.xym.community.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.xym.community.app.common.result.Result;
import top.xym.community.app.service.CommonService;

@Tag(name = "基础服务")
@RestController
@RequestMapping("/api/v1/common")
@AllArgsConstructor
public class CommonController {
    private final CommonService commonService;

    @PostMapping("/sendSms")
    @Operation(summary = "发送短信")
    public Result<Object> sendSms(@RequestParam("phone") String phone) {
        commonService.sendSms(phone);
        return Result.ok();
    }

    @PostMapping(value = "/upload/img")
    @Operation(summary = "图片上传")
    public Result<String> upload(@RequestBody MultipartFile file) {
        return Result.ok(commonService.upload(file));
    }
}
