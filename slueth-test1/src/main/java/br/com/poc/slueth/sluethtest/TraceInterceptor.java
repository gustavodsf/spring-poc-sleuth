package br.com.poc.slueth.sluethtest;

import brave.propagation.ExtraFieldPropagation;
import org.slf4j.MDC;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class TraceInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        MDC.put("logId", UUID.randomUUID().toString());

        String logId = ExtraFieldPropagation.get(SluethTestApplication.LOG_CORRELATION_ID);
        if (StringUtils.isEmpty(logId)) {
            ExtraFieldPropagation.set(SluethTestApplication.LOG_CORRELATION_ID, UUID.randomUUID().toString());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) throws Exception {
        MDC.remove("logId");
    }
}