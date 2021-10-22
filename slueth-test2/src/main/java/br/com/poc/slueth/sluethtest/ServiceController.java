package br.com.poc.slueth.sluethtest;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.BaggageInScope;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ServiceController {

    private static final Logger log = LoggerFactory.getLogger(ServiceController.class);

    private final RestTemplate restTemplate;
    private final Tracer tracer;
    private final String serviceAddress3;
    private final String serviceAddress4;
    private final int port;


    ServiceController(RestTemplate restTemplate, Tracer tracer,
                       @Value("${service3.address:localhost:8083}") String serviceAddress3,
                       @Value("${service4.address:localhost:8084}") String serviceAddress4,
                       @Value("${server.port:8082}") int port) {
        this.restTemplate = restTemplate;
        this.tracer = tracer;
        this.serviceAddress3 = serviceAddress3;
        this.serviceAddress4 = serviceAddress4;
        this.port = port;
    }

    // for the tracing presentation
    @GetMapping("/memeoverflow")
    public String memeOverflow() throws InterruptedException {
        throw new IllegalStateException("Meme overflow occurred");
    }

    @RequestMapping("/foo")
    public String service2MethodInController() throws InterruptedException {
        Thread.sleep(200);
        try (BaggageInScope baggage = this.tracer.getBaggage("key")) {
            log.info("Service2: Baggage for [key] is [" + (baggage == null ? null : baggage.get()) + "]");
            log.info("Hello from service2. Calling service3 and then service4");
            String service3 = restTemplate.getForObject("http://" + serviceAddress3 + "/bar", String.class);
            log.info("Got response from service3 [{}]", service3);
            String service4 = restTemplate.getForObject("http://" + serviceAddress4 + "/baz", String.class);
            log.info("Got response from service4 [{}]", service4);
            return String.format("Hello from service2, response from service3 [%s] and from service4 [%s]", service3, service4);
        }
    }

    @RequestMapping("/readtimeout")
    public String connectionTimeout() throws InterruptedException {
        Span span = this.tracer.nextSpan().name("second_span");
        Thread.sleep(500);
        try (Tracer.SpanInScope ws = this.tracer.withSpan(span.start())) {
            log.info("Calling a missing service");
            restTemplate.getForObject("http://localhost:" + port + "/blowup", String.class);
            return "Should blow up";
        }
        catch (Exception e) {
            log.error("Exception occurred while trying to send a request to a missing service", e);
            throw e;
        }
        finally {
            span.end();
        }
    }

    @RequestMapping("/blowup")
    public Callable<String> blowUp() throws InterruptedException {
        return () -> {
            Thread.sleep(4000);
            throw new RuntimeException("Should blow up");
        };
    }

}
