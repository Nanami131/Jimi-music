package com.example.jimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jimi.common.R;
import com.example.jimi.controller.MinioUploadController;
import com.example.jimi.handler.ConsumerDTOHandler;
import com.example.jimi.mapper.ConsumerMapper;
import com.example.jimi.model.domain.Consumer;
import com.example.jimi.model.domain.ConsumerDTO;
import com.example.jimi.model.request.ConsumerRequest;
import com.example.jimi.service.ConsumerService;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static com.example.jimi.constant.LoadConstants.SALT;

@Service
public class ConsumerServiceImpl extends ServiceImpl<ConsumerMapper, Consumer>
        implements ConsumerService {

    @Autowired
    private ConsumerMapper consumerMapper;


    /**
     * 新增用户
     */
    @Override
    public R addUser(ConsumerRequest registryRequest) {
        Consumer consumer = new Consumer();
        BeanUtils.copyProperties(registryRequest, consumer);
        //MD5加密
        String password = DigestUtils.md5DigestAsHex((SALT + registryRequest.getPassword()).getBytes(StandardCharsets.UTF_8));
        consumer.setPassword(password);
        //没钱 暂时不搞手机号和邮箱验证
        if (StringUtils.isBlank(consumer.getPhoneNum())) {
            consumer.setPhoneNum(null);
        }
        if ("".equals(consumer.getEmail())) {
            consumer.setEmail(null);
        }
        consumer.setAvator("img/avatorImages/user.jpg");
        QueryWrapper<Consumer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", registryRequest.getUsername())
                .or()
                .eq("email", consumer.getEmail())
                .or()
                .eq("phone_num", consumer.getPhoneNum());

        List<Consumer> existingConsumers = consumerMapper.selectList(queryWrapper);
        if (!existingConsumers.isEmpty()) {
            for (Consumer existing : existingConsumers) {
                if (registryRequest.getUsername().equals(existing.getUsername())) {
                    return R.warning("用户名已注册");
                }
                //避免空值
                if (Objects.equals(consumer.getEmail(), existing.getEmail())) {
                    return R.fatal("邮箱不允许重复");
                }
                if (Objects.equals(consumer.getPhoneNum(), existing.getPhoneNum())) {
                    return R.fatal("手机号已被注册");
                }
            }
        }

        try {
            if (consumerMapper.insert(consumer) > 0) {
                return R.success("注册成功");
            }
            return R.error("注册失败");
        } catch (DuplicateKeyException e) {
            return R.fatal(e.getMessage());
        }
    }


    @Override
    public R updateUserMsg(ConsumerRequest updateRequest) {
        //不能调用这个接口修改密码
        if(!Objects.equals(ConsumerDTOHandler.getConsumerInfo().getId(),updateRequest.getId())){
            return R.error("修改失败");
        }
        Consumer consumer = new Consumer();
        BeanUtils.copyProperties(updateRequest, consumer);
        if (consumerMapper.updateById(consumer) > 0) {
            return R.success("修改成功");
        } else {
            return R.error("修改失败");
        }
    }

    @Override
    public R updatePassword(ConsumerRequest updatePasswordRequest) {

       if (!this.verityPasswd(updatePasswordRequest.getUsername(),updatePasswordRequest.getOldPassword())) {
            return R.error("密码输入错误");
        }

        Consumer consumer = new Consumer();
        consumer.setId(updatePasswordRequest.getId());
        String secretPassword = DigestUtils.md5DigestAsHex((SALT + updatePasswordRequest.getPassword()).getBytes(StandardCharsets.UTF_8));
        consumer.setPassword(secretPassword);

        if (consumerMapper.updateById(consumer) > 0) {
            return R.success("密码修改成功");
        } else {
            return R.error("密码修改失败");
        }
    }

    /**
     * 缩减验证
     * @param updatePasswordRequest
     * @return
     */
    @Override
    public R updatePassword01(ConsumerRequest updatePasswordRequest) {
        Consumer consumer = new Consumer();
        consumer.setId(updatePasswordRequest.getId());
        String secretPassword = DigestUtils.md5DigestAsHex((SALT + updatePasswordRequest.getPassword()).getBytes(StandardCharsets.UTF_8));
        consumer.setPassword(secretPassword);

        if (consumerMapper.updateById(consumer) > 0) {
            return R.success("密码修改成功");
        } else {
            return R.error("密码修改失败");
        }
    }


    /**
     * 退出登录
     * @return
     */
    @Override
    public R logout(HttpSession session) {
        session.invalidate(); // 销毁 session
        return R.success("退出登录成功");
    }


    @Override
    public R updateUserAvator(MultipartFile avatorFile, int id) {
        String fileName = avatorFile.getOriginalFilename();
        String imgPath = "/img/avatorImages/" + fileName;
        Consumer consumer = new Consumer();
        consumer.setId(id);
        consumer.setAvator(imgPath);
        String s = MinioUploadController.uploadAtorImgFile(avatorFile);
        if (s.equals("File uploaded successfully!")&&consumerMapper.updateById(consumer) > 0) {
            return R.success("上传成功", imgPath);
        } else {
            return R.error("上传失败");
        }
    }

    @Override
    public boolean existUser(String username) {
        QueryWrapper<Consumer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username);
        return consumerMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean verityPasswd(String username, String password) {
        QueryWrapper<Consumer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username);
        String secretPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes(StandardCharsets.UTF_8));

        queryWrapper.eq("password",secretPassword);
        return consumerMapper.selectCount(queryWrapper) > 0;
    }


    // 删除用户
    @Override
    public R deleteUser(Integer id) {
        if (consumerMapper.deleteById(id) > 0) {
            return R.success("删除成功");
        } else {
            return R.error("删除失败");
        }
    }

    @Override
    public R allUser() {
        return R.success(null, consumerMapper.selectList(null));
    }

    @Override
    public R userOfId(Integer id) {
        QueryWrapper<Consumer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",id);
        return R.success(null, consumerMapper.selectList(queryWrapper));
    }

    @Override
    public R loginStatus(ConsumerRequest loginRequest, HttpSession session, HttpServletResponse response) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        if (this.verityPasswd(username, password)) {
            // 设置 JSESSIONID 的 SameSite=None

            Consumer consumer = new Consumer();
            consumer.setUsername(username);
            consumer=consumerMapper.selectOne(new QueryWrapper<>(consumer));
            session.setAttribute("user",new ConsumerDTO().
                    setId(consumer.getId()).
                    setEmail(consumer.getEmail()).
                    setPhoneNum(consumer.getPhoneNum()).
                    setUsername(consumer.getUsername())
            );
            Object user = session.getAttribute("user");
            System.out.println(2+user.toString());
            String cookieValue = "JSESSIONID=" + session.getId() + "; Path=/; HttpOnly; Secure; SameSite=None";
            response.addHeader("Set-Cookie", cookieValue);
            session.setMaxInactiveInterval(11200);
            return R.success("登录成功", consumerMapper.selectList(new QueryWrapper<>(consumer)));
        } else {
            return R.error("用户名或密码错误");
        }
    }
    @Override
    public R loginEmailStatus(ConsumerRequest loginRequest, HttpSession session) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        Consumer consumer1 = findByEmail(email);
        if (this.verityPasswd(consumer1.getUsername(), password)) {
            session.setAttribute("username", consumer1.getUsername());
            Consumer consumer = new Consumer();
            consumer.setUsername(consumer1.getUsername());
            return R.success("登录成功", consumerMapper.selectList(new QueryWrapper<>(consumer)));
        } else {
            return R.error("用户名或密码错误");
        }
    }

    @Override
    public Consumer findByEmail(String email) {
        QueryWrapper<Consumer> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("email",email);
        Consumer consumer = consumerMapper.selectOne(queryWrapper);
        return consumer;
    }

}
