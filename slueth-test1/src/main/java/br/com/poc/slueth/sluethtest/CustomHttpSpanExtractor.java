package br.com.poc.slueth.sluethtest;

import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.autoconfig.instrument.web.SleuthWebProperties;
import org.springframework.cloud.sleuth.autoconfig.instrument.web.TraceWebAutoConfiguration;
import org.springframework.cloud.sleuth.autoconfig.instrument.web.client.TraceWebAsyncClientAutoConfiguration;
import org.springframework.cloud.sleuth.instrument.web.TraceWebFilter;
import org.springframework.cloud.sleuth.instrument.web.client.TraceResponseHttpHeadersFilter;
import org.springframework.cloud.sleuth.instrument.web.client.TraceWebClientBeanPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(SleuthWebProperties.TRACING_FILTER_ORDER + 5)
class MyFilter extends GenericFilterBean {

    private final Tracer tracer;

    MyFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Span currentSpan = this.tracer.currentSpan();
        if (currentSpan == null) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        // for readability we're returning trace id in a hex form
        ((HttpServletResponse) servletResponse).addHeader("ZIPKIN-TRACE-ID", currentSpan.context().traceId());
        // we can also add some custom tags
        currentSpan.tag("custom", "tag");
        filterChain.doFilter(servletRequest, servletResponse);
    }
}