package top.xym.community.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"top.xym.community.app.mapper", "top.xym.community.app.module.session.mapper",
        "top.xym.community.app.module.message.mapper", "top.xym.community.app.module.community.mapper",
        "top.xym.community.app.module.service.mapper", "top.xym.community.app.module.myProfile.mapper"})
public class CommunityAppBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityAppBackendApplication.class, args);
    }

}
