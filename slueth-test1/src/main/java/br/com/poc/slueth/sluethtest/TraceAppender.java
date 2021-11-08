package br.com.poc.slueth.sluethtest;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class TraceAppender extends AppenderBase<ILoggingEvent> {

    private static final String THREAD_MAIN_NAME = "main";

    @Override
    public void start() {
        log.info("Started Trace Appender");
        super.start();
    }

    @Override
    public void stop() {
        log.info("Closing Trace Appender");
        super.stop();

    }

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        GeneralTrace generalTrace =  new GeneralTrace(iLoggingEvent);
        System.out.println("Appender");

    }
}
