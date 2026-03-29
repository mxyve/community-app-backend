package top.xym.community.app.module.myProfile.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.xym.community.app.module.myProfile.mapper.MyProfileMapper;
import top.xym.community.app.module.myProfile.model.User;
import top.xym.community.app.module.myProfile.service.MyProfileService;

@Service
public class MyProfileServiceImpl extends ServiceImpl<MyProfileMapper, User> implements MyProfileService {

}