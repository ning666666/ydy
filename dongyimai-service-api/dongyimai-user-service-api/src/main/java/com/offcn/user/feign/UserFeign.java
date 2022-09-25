package com.offcn.user.feign;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.user.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/****
 * @Author:ujiuye
 * @Description:
 * @Date 2021/2/1 14:19
 *****/
@FeignClient(name="user")
@RequestMapping("/user")
public interface UserFeign {
    /**
     * 根据用户名查询用户信息
     * @param username
     * @return
     */
    @GetMapping("/load/{username}")
    Result<User> findByUsername(@PathVariable(value = "username") String username);
    /***
     * 添加用户积分
     * @param points
     * @return
     */
    @GetMapping(value = "/points/add")
    Result addPoints(@RequestParam(value = "points")Integer points);
}