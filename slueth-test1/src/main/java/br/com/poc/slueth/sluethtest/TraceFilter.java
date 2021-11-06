package br.com.poc.slueth.sluethtest;

import org.slf4j.MDC;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.autoconfig.instrument.web.SleuthWebProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

@Component
@Order(SleuthWebProperties.TRACING_FILTER_ORDER + 5)
class TraceFilter extends GenericFilterBean {

    private final Tracer tracer;
    private final TraceProperties traceProperties;

    TraceFilter(Tracer tracer, TraceProperties traceProperties) {
        this.tracer = tracer;
        this.traceProperties = traceProperties;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        try {
            this.tagBasicInfo(servletRequest, servletResponse);
            this.tagRequestBody(servletRequest);
            filterChain.doFilter(servletRequest, servletResponse);
        }catch (Exception exception) {
            Span currentSpan = this.tracer.currentSpan();
            currentSpan.tag("error.message", exception.getMessage());
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private void tagBasicInfo(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) servletResponse);
        Span currentSpan = this.tracer.currentSpan();

        currentSpan.tag("result.status", String.valueOf(responseWrapper.getStatus()));
        currentSpan.tag("url", new URL(httpServletRequest.getRequestURL().toString()).toString());
        currentSpan.tag("uuid.request", getHeaderValue(httpServletRequest, traceProperties.getUuidRequestName()));
        currentSpan.tag("environment", traceProperties.getEnvironment());
        currentSpan.tag("host", InetAddress.getLocalHost().getHostName());
        currentSpan.tag("received.from.address", ipChains(httpServletRequest));
        currentSpan.tag("request.header", getRequestHeaders(httpServletRequest));

        if(httpServletRequest.getQueryString() != null) {
            currentSpan.tag("query.params", httpServletRequest.getQueryString());
        }
    }

    private void tagRequestBody(ServletRequest servletRequest) throws IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        Span currentSpan = this.tracer.currentSpan();

        if(Arrays.asList("POST", "PUT").contains(httpServletRequest.getMethod())) {
            String characterEncoding = httpServletRequest.getCharacterEncoding();
            Charset charset = Charset.forName(characterEncoding);
            String bodyInStringFormat = readInputStreamInStringFormat(httpServletRequest.getInputStream(), charset);
            currentSpan.tag("request.body", bodyInStringFormat);
        }
    }

    private String ipChains (HttpServletRequest request){
        return String.join(";", Collections.list(request.getHeaders(traceProperties.getHostRequestName())));
    }

    private String getHeaderValue(HttpServletRequest httpServletRequest, String tagName){
        String result = httpServletRequest.getHeader(tagName);
        MDC.put(tagName,result);
        return result == null ? "" : result;
    }

    private String getRequestHeaders(HttpServletRequest request) {
        HashMap<String, String> map = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            map.put(key, request.getHeader(key));
        }
        return map.toString();
    }

    private String readInputStreamInStringFormat(InputStream stream, Charset charset) throws IOException {
        final int MAX_BODY_SIZE = 1024;
        final StringBuilder bodyStringBuilder = new StringBuilder();
        if (!stream.markSupported()) {
            stream = new BufferedInputStream(stream);
        }

        stream.mark(MAX_BODY_SIZE + 1);
        final byte[] entity = new byte[MAX_BODY_SIZE + 1];
        final int bytesRead = stream.read(entity);

        if (bytesRead != -1) {
            bodyStringBuilder.append(new String(entity, 0, Math.min(bytesRead, MAX_BODY_SIZE), charset));
            if (bytesRead > MAX_BODY_SIZE) {
                bodyStringBuilder.append("...");
            }
        }
        stream.reset();

        return bodyStringBuilder.toString();
    }

}
