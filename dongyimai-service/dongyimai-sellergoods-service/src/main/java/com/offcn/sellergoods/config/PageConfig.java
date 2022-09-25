package com.offcn.sellergoods.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PageConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        //将来所有的拦截器都需要添加到该对象内部
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //创建具体的分页拦截器
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        //指定操作的数据库
        paginationInnerInterceptor.setDbType(DbType.MYSQL);
        //达到最大页，true回首页，false继续请求
        paginationInnerInterceptor.setOverflow(true);
        //单页最大显示数量
        paginationInnerInterceptor.setMaxLimit(500L);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }
}