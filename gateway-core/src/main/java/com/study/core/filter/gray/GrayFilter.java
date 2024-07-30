package com.study.core.filter.gray;

import com.study.common.constants.FilterConst;
import com.study.core.context.GatewayContext;
import com.study.core.filter.FilterAspect;
import com.study.core.filter.IFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName GrayFilter
 * @Description
 * @Author
 * @Date 2024-07-29 21:59
 * @Version
 */

@Slf4j
@FilterAspect(id = FilterConst.GRAY_FILTER_ID, name = FilterConst.GRAY_FILTER_NAME,
        order = FilterConst.GRAY_FILTER_ORDER)
public class GrayFilter implements IFilter {
    @Override
    public void doFilter(GatewayContext context) throws Exception {
        //灰度测试
        String gray = context.getGatewayRequest().getHeaders().get("gray");
        if(gray != null && gray.equals("true")) {
            context.setGray(true);
        }
        
        //真正的灰度用户选取
        String clientIP = context.getGatewayRequest().getClientIP();
        int res = clientIP.hashCode() & (1024 - 1);//等级与对1024取模
        if(res==1){
            context.setGray(true);
        }
    }
}
