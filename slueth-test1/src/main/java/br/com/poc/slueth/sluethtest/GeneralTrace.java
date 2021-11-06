package br.com.poc.slueth.sluethtest;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Data
@NoArgsConstructor
public class GeneralTrace {


    private LocalDateTime ts;

    private String level;

    private String logger;

    private String thread;

    @JsonInclude(Include.NON_NULL)
    private String stackTrace;

    @JsonInclude(Include.NON_NULL)
    private Map<String, String> mdc;

    private Object content;


    public GeneralTrace(ILoggingEvent e) {

        this.ts = LocalDateTime.ofInstant(Instant.ofEpochMilli(e.getTimeStamp()), ZoneId.systemDefault());
        this.content = e.getFormattedMessage();
        this.level = e.getLevel().toString();
        this.logger = e.getLoggerName();
        this.thread = e.getThreadName();

        if (e.hasCallerData()) {
            StackTraceElement st = e.getCallerData()[0];
            String callerData = String.format("%s.%s:%d", st.getClassName(), st.getMethodName(), st.getLineNumber());
            this.stackTrace = callerData;
        }

        Map<String, String> mdcPropertyMap = e.getMDCPropertyMap();
        if (mdcPropertyMap != null && !mdcPropertyMap.isEmpty()) {
            this.mdc = mdcPropertyMap;
        }

    }

}