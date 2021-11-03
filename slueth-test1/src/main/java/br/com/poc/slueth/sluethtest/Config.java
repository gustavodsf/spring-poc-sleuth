package br.com.poc.slueth.sluethtest;

import brave.baggage.*;
import brave.context.slf4j.MDCScopeDecorator;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.http.HttpRequestParser;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import brave.sampler.Sampler;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.instrument.web.HttpClientRequestParser;
import org.springframework.cloud.sleuth.instrument.web.HttpServerRequestParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletResponse;


@Configuration(proxyBeanMethods = false)
public class Config {
    @Bean
    BaggageField countryCodeField() {
        return BaggageField.create("country-code");
    }

    @Bean
    CurrentTraceContext.ScopeDecorator mdcScopeDecorator() {
        return MDCScopeDecorator.newBuilder()
                .clear()
                .add(CorrelationScopeConfig.SingleCorrelationField.newBuilder(countryCodeField())
                        .flushOnUpdate()
                        .build())
                .build();
    }

    @Bean(name = { HttpClientRequestParser.NAME, HttpServerRequestParser.NAME })
    HttpRequestParser sleuthHttpServerRequestParser() {
        return (req, context, span) -> {
            HttpRequestParser.DEFAULT.parse(req, context, span);
            String url = req.url();
            if (url != null) {
                span.tag("http.status.code", req.toString());
            }
        };
    }

    @Bean
    public Sampler defaultSampler() {
        return Sampler.ALWAYS_SAMPLE;
    }

    @Bean
    SpanHandler handlerOne() {
        return new SpanHandler() {
            @Override
            public boolean end(TraceContext traceContext, MutableSpan span, Cause cause) {
                span.name("one");
                return true; // keep this span
            }
        };
    }
    @Bean
    SpanHandler handlerTwo() {
        return new SpanHandler() {
            @Override
            public boolean end(TraceContext traceContext, MutableSpan span, Cause cause) {
                span.name(span.name() + " bar");
                return true; // keep this span
            }
        };
    }

    // Example of a servlet Filter for non-reactive applications
    @Bean
    Filter traceIdInResponseFilter(Tracer tracer) {
        return (request, response, chain) -> {
            Span currentSpan = tracer.currentSpan();
            if (currentSpan != null) {
                HttpServletResponse resp = (HttpServletResponse) response;
                // putting trace id value in [mytraceid] response header
                resp.addHeader("mytraceid", currentSpan.context().traceId());
            }
            chain.doFilter(request, response);
        };
    }

    @Bean
    public BaggagePropagationCustomizer baggagePropagationCustomizer() {
        return (factoryBuilder) -> {
            factoryBuilder.add(
                    BaggagePropagationConfig.SingleBaggageField.remote(BaggageField.create("Correlation-Id")));
        };
    }

    @Bean
    public CorrelationScopeCustomizer correlationScopeCustomizer() {
        return builder -> {
            builder.add(CorrelationScopeConfig.SingleCorrelationField.newBuilder(BaggageField.create("Correlation-Id"))
                    .flushOnUpdate()
                    .build());
        };
    }

}
