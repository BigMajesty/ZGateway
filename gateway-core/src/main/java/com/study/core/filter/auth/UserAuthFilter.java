package com.study.core.filter.auth;

import com.study.common.constants.FilterConst;
import com.study.common.enums.ResponseCode;
import com.study.common.exception.ResponseException;
import com.study.core.context.GatewayContext;
import com.study.core.filter.FilterAspect;
import com.study.core.filter.IFilter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultClaims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @ClassName UserAuthFilter
 * @Description 用户鉴权过滤器
 * @Author
 * @Date 2024-07-26 08:59
 * @Version
 */
@Slf4j
@FilterAspect(id = FilterConst.USER_AUTH_FILTER_ID, name = FilterConst.USER_AUTH_FILTER_NAME,
        order = FilterConst.USER_AUTH_FILTER_ORDER)
public class UserAuthFilter implements IFilter {
    
    private static final String SECRET_KEY = "zhjjjjjjjjjjjjj";
    private static final String COOKIE_NAME = "user-jwt";
    
    @Override
    public void doFilter(GatewayContext context) throws Exception {
        if(context.getRule().getFilterConfig(FilterConst.USER_AUTH_FILTER_ID) == null){
            return;
        }
        String token = context.getGatewayRequest().getCookie(COOKIE_NAME).value();
        if(StringUtils.isBlank(token)){
            throw new ResponseException(ResponseCode.UNAUTHORIZED);
        }

        try {
            long userId = parseUserId(token);
            System.out.println("从token中析出的userId："+userId);
            context.getGatewayRequest().setUserId(userId);
        } catch (Exception e) {
            throw new ResponseException(ResponseCode.UNAUTHORIZED);
        }
    }

    /**
     * 从token中解析出userId
     * @param token
     * @return
     */
    private long parseUserId(String token) {
        Jws<Claims> jwt = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
        return Long.parseLong(jwt.getBody().getSubject());
    }
}
