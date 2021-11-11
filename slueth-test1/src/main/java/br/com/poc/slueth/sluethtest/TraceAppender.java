package br.com.poc.slueth.sluethtest;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.kafka.common.KafkaException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachableImpl;

public class TraceAppender extends TraceAppenderConfig<ILoggingEvent> {
	
	private SplunkKafkaProducer splunkKafkaProducer = null;
    private final AppenderAttachableImpl<ILoggingEvent> aai = new AppenderAttachableImpl<>();
    private final ConcurrentLinkedQueue<ILoggingEvent> queue = new ConcurrentLinkedQueue<>();
    
    
    @Override
    public void start() {
        if (!checkPrerequisites()) return;

        if (partition != null && partition < 0) {
            partition = null;
        }

        splunkKafkaProducer = new SplunkKafkaProducer();

        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        if (splunkKafkaProducer != null && splunkKafkaProducer.isInitialized()) {
            try {
            	splunkKafkaProducer.get().close();
            } catch (KafkaException e) {
                this.addWarn("Failed to shut down kafka producer: " + e.getMessage(), e);
            }
            splunkKafkaProducer = null;
        }
    }
    
    
    @Override
    public void addAppender(Appender<ILoggingEvent> newAppender) {
        aai.addAppender(newAppender);
    }

    @Override
    public Iterator<Appender<ILoggingEvent>> iteratorForAppenders() {
        return aai.iteratorForAppenders();
    }

    @Override
    public Appender<ILoggingEvent> getAppender(String name) {
        return aai.getAppender(name);
    }

    @Override
    public boolean isAttached(Appender<ILoggingEvent> appender) {
        return aai.isAttached(appender);
    }

    @Override
    public void detachAndStopAllAppenders() {
        aai.detachAndStopAllAppenders();
    }

    @Override
    public boolean detachAppender(Appender<ILoggingEvent> appender) {
        return aai.detachAppender(appender);
    }

    @Override
    public boolean detachAppender(String name) {
        return aai.detachAppender(name);
    }

    @Override
    public void doAppend(ILoggingEvent e) {
        ensureDeferredAppends();
        if (e instanceof ILoggingEvent) {
            deferAppend(e);
        } else {
            super.doAppend(e);
        }
    }
    
	@Override
	protected void append(ILoggingEvent eventObject) {
		GeneralTrace generalTrace = new GeneralTrace(eventObject);
		switch(generalTrace.getLevel()) {
			case "INFO":
				sendFilteredMessage(generalTrace);
				break;
			case "ERROR":
				sendMessage(generalTrace);
				break;
			case "WARN":
				sendMessage(generalTrace);
				break;
			default:
				sendFilteredMessage(generalTrace);
				break;
		}
	}
	
	private void sendFilteredMessage(GeneralTrace generalTrace) {
		if( !generalTrace.getLogger().contains(useToFilterInfo) ) {
			return ;
		}

		this.sendMessage(generalTrace);
	}
	
	private void sendMessage(GeneralTrace generalTrace) {
		
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer());
		gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer());
		Gson gson = gsonBuilder.setPrettyPrinting().create();
		
		String json = gson.toJson(generalTrace);
		
	    splunkKafkaProducer.send(json, partition, topic);
	}
	
	
	private void deferAppend(ILoggingEvent event) {
        queue.add(event);
    }

    private void ensureDeferredAppends() {
    	ILoggingEvent event;
        while ((event = queue.poll()) != null) {
            super.doAppend(event);
        }
    }
}
