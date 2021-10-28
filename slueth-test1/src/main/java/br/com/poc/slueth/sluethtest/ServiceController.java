package br.com.poc.slueth.sluethtest;

import java.time.LocalDateTime;

import brave.baggage.BaggageField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.BaggageInScope;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.cloud.sleuth.annotation.TagValueResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletResponse;


@Slf4j
@RestController
public class ServiceController {


    private final BaggageField countryCodeField;

    private final Service2Client service2Client;

    private final Tracer tracer;

    public ServiceController(BaggageField countryCodeField, Service2Client service2Client, Tracer tracer) {
        this.countryCodeField = countryCodeField;
        this.service2Client = service2Client;
        this.tracer = tracer;
    }

    @GetMapping("/start/{name}")
    public Mono<String> start(@PathVariable("name") String name) {
        log.info("Hello example "+ name);

        BaggageInScope countryCode = this.tracer.createBaggage("country-code").set("FO");

        this.countryCodeField.updateValue("new-value");

        // Start a span. If there was a span present in this thread it will become
        // the `newSpan`'s parent.
        Span newSpan = this.tracer.currentSpan();
        newSpan.tag("taxValue", "150");

        newSpan = this.tracer.nextSpan().name("calculateTax");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.start())) {
            // ...
            // You can tag a span
            newSpan.tag("taxValue", "120");
            // ...
            // You can log an event on a span
            newSpan.event("taxCalculated");
        }
        finally {
            // Once done remember to end the span. This will allow collecting
            // the span to send it to a distributed tracing system e.g. Zipkin
            newSpan.end();
        }

        return this.service2Client.start();
    }

    @GetMapping("/readtimeout")
    public Mono<String> timeout() throws InterruptedException {
        return service2Client.timeout(LocalDateTime.now().toString());
    }

    @PostMapping("/start")
    public Mono<String> postStart() {
        return start("TESTE");
    }

    @PostMapping("/readtimeout")
    public Mono<String> postTimeout() throws InterruptedException {
        return timeout();
    }


}
