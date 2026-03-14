package top.xym.community.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"top.xym.community.app.mapper", "top.xym.community.app.module.session.mapper"})
public class CommunityAppBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityAppBackendApplication.class, args);
    }

}
