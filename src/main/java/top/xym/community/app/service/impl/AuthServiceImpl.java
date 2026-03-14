package top.xym.community.app.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import top.xym.community.app.common.cache.RedisCache;
import top.xym.community.app.common.cache.RedisKeys;
import top.xym.community.app.common.exception.ErrorCode;
import top.xym.community.app.common.exception.ServerException;
import top.xym.community.app.mapper.UserMapper;
import top.xym.community.app.model.dto.WxLoginDTO;
import top.xym.community.app.model.entity.User;
import top.xym.community.app.model.vo.UserLoginVO;
import top.xym.community.app.service.AuthService;
import top.xym.community.app.utils.AESUtil;
import top.xym.community.app.utils.CommonUtils;
import top.xym.community.app.utils.JwtUtils;
import top.xym.community.app.utils.SecurityUtils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static top.xym.community.app.common.constant.Constant.*;

@Slf4j
@Service
@AllArgsConstructor
public class AuthServiceImpl extends ServiceImpl<UserMapper, User> implements AuthService {

    private final RedisCache redisCache;
    private final JwtUtils jwtUtils;

    @Override
    public UserLoginVO loginByPhone(String phone, String code) {
        // 获取验证码cacheKey
        String smsCacheKey = RedisKeys.getSmsCodeKey(phone);
        // 从redis中获取验证码
        Integer redisCode = redisCache.get(smsCacheKey, Integer.class);
        // 校验验证码合法性
        if (ObjectUtils.isEmpty(redisCode) || !redisCode.toString().equals(code)) {
            throw new ServerException(ErrorCode.SMS_CODE_ERROR);
        }
        // 删除用过的验证码
        redisCache.delete(smsCacheKey);
        // 根据手机号获取用户
        User user = baseMapper.getByPhone(phone);
        if (ObjectUtils.isEmpty(user)) {
            throw new ServerException("账号不存在，请先微信注册");
        }
//        if (ObjectUtils.isEmpty(user)) {
//            log.info("⽤户不存在，创建⽤户, phone: {}", phone);
//            user = new User();
//            user.setNickName(phone);
//            user.setPhone(phone);
//            user.setAvatar("默认头像的url");
//            baseMapper.insert(user);
//        }
        // 构造token
        String accessToken = jwtUtils.generateToken(Long.valueOf(user.getUserId()));
        Long userId = Long.valueOf(user.getUserId());
        String tokenKey = RedisKeys.getUserTokenKey(userId);
        redisCache.set(tokenKey, accessToken, 86400, TimeUnit.SECONDS);
        // 构造登录返回vo
        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setUserId(user.getUserId());
        userLoginVO.setPhone(user.getPhone());
        userLoginVO.setWxOpenId(user.getWxOpenId());
        userLoginVO.setAccessToken(accessToken);
        return userLoginVO;
    }

    @Override
    public UserLoginVO weChatLogin(WxLoginDTO loginDTO) {
        String url = "https://api.weixin.qq.com/sns/jscode2session?" +
                "appid=" + APP_ID +
                "&secret=" + APP_SECRET +
                "&js_code=" + loginDTO.getCode() +
                "&grant_type=authorization_code";
        RestTemplate restTemplate = new RestTemplate();
        String jsonData = restTemplate.getForObject(url, String.class);
        if (StringUtils.contains(jsonData, WX_ERR_CODE)) {
            // 出错了
            throw new ServerException("openId获取失败，" + jsonData);
        }
        // 解析返回数据
        JSONObject jsonObject = JSON.parseObject(jsonData);
        log.info("wxData: {}", jsonData);
        String openId = Objects.requireNonNull(jsonObject).getString(WX_OPENID);
        String sessionKey = jsonObject.getString(WX_SESSION_KEY);
        // 对用户加密数据解密
        String jsonUserData = AESUtil.decrypt(loginDTO.getEncryptedData(), sessionKey, loginDTO.getIv());
        log.info("wxUserINfo: {}", jsonUserData);

        JSONObject wxUserData = JSON.parseObject(jsonUserData);

        User user = baseMapper.getByWxOpenId(openId);
        if (ObjectUtils.isEmpty(user)) {
            log.info("用户不存在，创建用户，openId: {}", openId);
            user = new User();
            user.setWxOpenId(openId);
            user.setAvatar(wxUserData.getString("avatarUrl"));
            user.setGender(wxUserData.getString("gender"));
            user.setNickName(wxUserData.getString("nickName"));
            baseMapper.insert(user);
        }
        String accessToken = jwtUtils.generateToken(Long.valueOf(user.getUserId()));
        Long userId = Long.valueOf(user.getUserId());
        String tokenKey = RedisKeys.getUserTokenKey(userId);
        redisCache.set(tokenKey, accessToken, 86400, TimeUnit.SECONDS);
        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setUserId(user.getUserId());
        if (StringUtils.isNotBlank(user.getPhone())) {
            userLoginVO.setPhone(user.getPhone());
        }
        userLoginVO.setWxOpenId(user.getWxOpenId());
        userLoginVO.setAccessToken(accessToken);
        return userLoginVO;
    }

    @Override
    public void logout() {
        // 从 Spring Security 上下文获取 userId
        Long userId = SecurityUtils.getCurrentUserId();

        // 删除 redis 中的 token
        String tokenKey = RedisKeys.getUserTokenKey(userId);
        redisCache.delete(tokenKey);
    }

    @Override
    public void bindPhone(String phone, String code, String accessToken) {
        if (!CommonUtils.checkPhone(phone)) {
            throw new ServerException(ErrorCode.PARAMS_ERROR);
        }

        String smsCacheKey = RedisKeys.getSmsCodeKey(phone);
        Integer redisCode = redisCache.get(smsCacheKey, Integer.class);

        if (ObjectUtils.isEmpty(redisCode) || !redisCode.equals(code)) {
            throw new ServerException(ErrorCode.SMS_CODE_ERROR);
        }

        redisCache.delete(smsCacheKey);

        // 从 Spring Security 获取当前登录用户
        Long userId = SecurityUtils.getCurrentUserId();
        User user = baseMapper.selectById(userId);

        // 判断手机号是否已被占用
        User userByPhone = baseMapper.getByPhone(phone);
        if (ObjectUtils.isNotEmpty(userByPhone) && !userByPhone.getUserId().equals(userId)) {
            throw new ServerException(ErrorCode.PHONE_IS_EXIST);
        }

        user.setPhone(phone);
        baseMapper.updateById(user);
    }


}
