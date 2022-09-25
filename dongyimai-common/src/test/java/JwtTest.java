import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    /****
     * 创建Jwt令牌，头部（基本）、载荷（有效）、签证（安全）
     * JWT实际就是令牌的规范，本身也是一个令牌字符串，JJWT是对JWT进行加密、构建、验证等功能封装好的库，直接调用方法就可以对JWT进行操作
     */
    @Test
    public void testCreateJwt() {
        JwtBuilder builder = Jwts.builder() //构建令牌
                .setId("888")//设置唯一编号
                .setSubject("小白")//设置主题可以是JSON数据
                .setIssuedAt(new Date())//设置签发日期
                //.setExpiration(new Date())//用于设置过期时间，参数为Date类型数据
                .signWith(SignatureAlgorithm.HS256, "ujiuye");//设置签名使用HS256算法，并设置SecretKey(字符串)
        //构建并返回一个字符串
        // 自定义数据，Claims自定义
        Map<String,Object> map = new HashMap<>();
        map.put("name","中公优就业");
        map.put("address","北京市朝阳区五方桥基地");
        builder.addClaims(map);
        System.out.println(builder.compact());
    }
    /***
     *解析Jwt令牌数据
     */
    @Test
    public void testParseJwt(){
        // 将字符串值替换成自己测试时上步的输出结果，io.jsonwebtoken.ExpiredJwtException设置了过期时间
        String compactJwt="eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4ODgiLCJzdWIiOiLlsI_nmb0iLCJpYXQiOjE2NTk2MDQwMDIsImFkZHJlc3MiOiLljJfkuqzluILmnJ3pmLPljLrkupTmlrnmoaXln7rlnLAiLCJuYW1lIjoi5Lit5YWs5LyY5bCx5LiaIn0.FdlKqmaHdyXw49pi9xefr2GvAdk9LeHlDncK1A9jSGs";
        Claims claims = Jwts.parser().
                setSigningKey("ujiuye").
                parseClaimsJws(compactJwt).
                getBody();
        System.out.println(claims);
    }
}
