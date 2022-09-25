package com.offcn.user.dao;
import com.offcn.user.pojo.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/****
 * @Author:ujiuye
 * @Description:User的Dao
 * @Date 2021/2/1 14:19
 *****/
public interface UserMapper extends BaseMapper<User> {
    /***
     * 增加用户积分
     * @param username
     * @param points
     * @return
     */
    @Update("UPDATE tb_user SET points=points+#{points} WHERE  username=#{username}")
    int addUserPoints(@Param("username") String username, @Param("points") Integer points);
}
