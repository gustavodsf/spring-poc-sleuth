package br.com.poc.slueth.sluethtest;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class TraceAppender extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        GeneralTrace generalTrace =  new GeneralTrace(iLoggingEvent);

    }
}