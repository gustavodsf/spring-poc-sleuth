package br.com.poc.slueth.sluethtest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ThreadLoggingInitializer {

    private static org.slf4j.Logger LOG = LoggerFactory.getLogger(ThreadLoggingInitializer.class);

    /* @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        LoggerContext loggerContext = ((Logger) LoggerFactory.getLogger("")).getLoggerContext();
        loggerContext.addTurboFilter(new TurboFilter() {
            @Override
            public FilterReply decide(Marker marker, Logger logger, Level level, String s, Object[] objects, Throwable throwable) {
                if(logger.getName().contains(SluethTestApplication.class.getPackageName())){
                    return null;
                }
                return null;
            }
        });
        LOG.info("ThreadLogging support initialized");
    } */
}
