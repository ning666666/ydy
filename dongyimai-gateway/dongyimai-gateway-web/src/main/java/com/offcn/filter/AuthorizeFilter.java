package com.offcn.filter;

import com.offcn.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 配置全局过滤器：用户未登录请求进来网关是会先提示用户登录，虽然登录的时候生成了令牌，但是访问敏感模块，
 * 我们还是要校验一下是否带上了令牌，以及令牌是否正确，特定情况下每个模块都是需要写的，但统一写在网关模块
 * GlobalFilter识别过滤器
 */
//1.目的：判断令牌，决定放行/拦截
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    //令牌头名字，请求头中安全加密字段，用Authorization去标记，表示令牌的名字
    private static final String AUTHORIZE_TOKEN = "Authorization";
    //给登录页传递一个原始资源访问地址
    private static final String USER_LOGIN_URL = "http://localhost:9101/oauth/login";

    /***
     *全局过滤器
     *@param exchange
     *@param chain
     *@return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //从exchange获取Request、Response对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //获取请求的URI
        String path = request.getURI().getPath();
        //判断是否需要拦截
        //如果是登录、goods等开放的微服务[这里的goods部分开放],则直接放行,这里不做完整演示，完整演示需要设计一套权限系统
        /*if (path.startsWith("/api/user/login") || path.startsWith("/api/brand/search/")) {
            //chain执行放行，还能执行拦截
            Mono<Void> filter = chain.filter(exchange);
            return filter;
        }*/
        //将是否需要用户登录过滤也加入其中
        if (URLFilter.hasAuthorize(path)) {
            // 放行
            return chain.filter(exchange);
        }
        //2.找令牌
        //获取头文件中的令牌信息
        String tokent = request.getHeaders().getFirst(AUTHORIZE_TOKEN);

        //如果头文件中没有，则从请求参数中获取
        if (StringUtils.isEmpty(tokent)) {
            tokent = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
        }
        //从cookie中找，需要手动添加
        //从cookie中获取令牌数据
        //header中没有令牌数据，则从cookie获取
        // 之前登录成功后会生成令牌并存入cookie,而OAuth集成dongyimai-user-service后就不再校验令牌,而是先用公钥解密,再解析
        // 所以不从cookie取登录令牌,而是从header中取OAuth验证的令牌
        // 也可以理解为不使用"测试用户 111"账户校验, 而是使用"dongyimai dongyimai"校验
        if (StringUtils.isEmpty(tokent)) {
            HttpCookie first = request.getCookies().getFirst(AUTHORIZE_TOKEN);
            if (first != null) {
                tokent = first.getValue();
            }
        }
        //如果为空，则输出错误代码
        if (StringUtils.isEmpty(tokent)) {
            //设置方法不允许被访问，405错误代码
            //response.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED);
            //response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //return response.setComplete();
            //原来未登录直接报错,改后未登录跳转到登录页面
            return needAuthorization(USER_LOGIN_URL, exchange, path);
        }

        //3.解析令牌数据，能解析出来，说明是正确的令牌，就放行
        //加入auth2以后不再需要网关模块再去解析令牌，因为令牌以及用私钥加密，敏感模块（需要登录的模块）
        //都配置了公钥，敏感模块先用公钥解密，再校验令牌合法（并不需要解析令牌）的话就可以进行资源放行、不需要再登录
      /*  try {
            Claims claims = JwtUtil.parseJWT(tokent);
            //我们这里做的是对敏感模块也就是登录模块的，没登录提示去登录，登录了以后再请求过来，需要做令牌校验
            //令牌校验后的放行，但有些目的模块还需要用到用户的信息比如支付时需要知道是哪个用户付的款
            //将令牌数据添加到头文件中，目的模块可在Header头文件中得到用户信息
            request.mutate().header(AUTHORIZE_TOKEN,claims.toString());
            //这里实际是有两次请求，如果用户第一次请求访问user的findAll，是没有登录的，那过滤器拦截第一次请求
            //让用户去登录，登录以后页面会继续访问user的findAll，这时拦截的第二次请求会在登录请求中的cookies
            //保存的令牌信息，获取出来放入请求头文件中，这样第二次请求访问user的findAll就能获取到令牌信息了
        } catch (Exception e) {
            e.printStackTrace();
            //解析失败，响应401错误
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }*/
        // 判断是否有bearer前缀，没有这个前缀就添加上并存入头文件，不加令牌解析校验不了
        if (!tokent.startsWith("bearer ") && !tokent.startsWith("Bearer ")) {
            tokent = "bearer " + tokent;
        }
        request.mutate().header(AUTHORIZE_TOKEN, tokent);

        //放行
        return chain.filter(exchange);
    }


    /***
     *过滤器执行顺序
     *@return
     */
    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 响应设置
     *
     * @param url
     * @param exchange
     * @return
     */
    public Mono<Void> needAuthorization(String url, ServerWebExchange exchange, String from) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SEE_OTHER);
        // url - 跳转的登录模块地址
        // from - 将跳转前的地址记录, 方便登录成功后再自动跳回来
        response.getHeaders().set("Location", url + "?from=" + from);
        return exchange.getResponse().setComplete();
    }
}