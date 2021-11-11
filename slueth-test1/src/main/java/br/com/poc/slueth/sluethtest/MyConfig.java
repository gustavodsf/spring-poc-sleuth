package br.com.poc.slueth.sluethtest;

import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;
import zipkin2.Span;
import brave.baggage.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.autoconfig.zipkin2.ZipkinAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.Sender;

@Configuration(proxyBeanMethods = false)
public class MyConfig {

    @Value( "${dock.starter.core.header.uuid.request}" )
    private String uuidRequest;
    
    @Value( "${dock.starter.core.topic}" )
    private String topic;
    
    private Integer partition = null;

    @Bean
    public BaggagePropagationCustomizer baggagePropagationCustomizer() {
        return (factoryBuilder) -> {
            factoryBuilder.add(
                    BaggagePropagationConfig.SingleBaggageField.remote(BaggageField.create(uuidRequest)));
        };
    }

    @Bean
    public CorrelationScopeCustomizer correlationScopeCustomizer() {
        return builder -> {
            builder.add(CorrelationScopeConfig.SingleCorrelationField.newBuilder(BaggageField.create(uuidRequest))
                    .flushOnUpdate()
                    .build());
        };
    }

    @Bean(ZipkinAutoConfiguration.REPORTER_BEAN_NAME)
    Reporter<Span> myReporter(@Qualifier(ZipkinAutoConfiguration.SENDER_BEAN_NAME) Sender mySender) {
        return AsyncReporter.create(mySender);
    }

    @Bean(ZipkinAutoConfiguration.SENDER_BEAN_NAME)
    Sender mySender() {
        return new SplunkSender(topic, partition);
    }

    @Bean
    SpanHandler handlerOne() {
        return new SpanHandler() {
            @Override
            public boolean end(TraceContext traceContext, MutableSpan span, Cause cause) {
                String[] pattern = span.name().split(" ");
                span.tag("pattern", pattern.length > 0 ? pattern[1] : "");
                return true; // keep this span
            }
        };
    }
}
