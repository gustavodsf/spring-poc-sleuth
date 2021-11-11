package br.com.poc.slueth.sluethtest;

import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.AppenderAttachable;


public abstract class TraceAppenderConfig<T> extends UnsynchronizedAppenderBase<T> implements AppenderAttachable<T> {
    
    protected String topic = null;
    
    protected String useToFilterInfo = null;
    
    protected boolean appendTimestamp = true;
    
    protected Integer partition = null;

    protected Map<String,Object> producerConfig = new HashMap<String, Object>();

    protected boolean checkPrerequisites() {
        boolean errorFree = true;

        if (producerConfig.get(BOOTSTRAP_SERVERS_CONFIG) == null) {
            addError("No \"" + BOOTSTRAP_SERVERS_CONFIG + "\" set for the appender named [\""
                    + name + "\"].");
            errorFree = false;
        }

        if (topic == null) {
            addError("No topic set for the appender named [\"" + name + "\"].");
            errorFree = false;
        }
        
        if (useToFilterInfo == null) {
            addError("No useToFilterInfo set for the appender named [\"" + name + "\"].");
            errorFree = false;
        }

        return errorFree;
    }


    public void setTopic(String topic) {
        this.topic = topic;
    }
    
    public void setUseToFilterInfo(String useToFilterInfo) {
        this.useToFilterInfo = useToFilterInfo;
    }
    
    public void setPartition(Integer partition) {
        this.partition = partition;
    }

    public void addProducerConfig(String keyValue) {
        String[] split = keyValue.split("=", 2);
        if(split.length == 2)
            addProducerConfigValue(split[0], split[1]);
    }

    public void addProducerConfigValue(String key, Object value) {
        this.producerConfig.put(key,value);
    }

    public Map<String, Object> getProducerConfig() {
        return producerConfig;
    }
    
    public boolean isAppendTimestamp() {
        return appendTimestamp;
    }

    public void setAppendTimestamp(boolean appendTimestamp) {
        this.appendTimestamp = appendTimestamp;
    }
}
