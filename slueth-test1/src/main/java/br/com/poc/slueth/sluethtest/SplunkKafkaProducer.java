package br.com.poc.slueth.sluethtest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringSerializer;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class SplunkKafkaProducer {

	private volatile Producer<String, String> producer;

    public void send(String message, Integer partition,String  topicName){
    	String key = UUID.randomUUID().toString();
    	
    	final ProducerRecord<String, String> record = buildProducerRecord(key, message, partition, topicName);
    	
    	final Producer<String, String> producer = this.get();

    
    	try {
             producer.send(record, new Callback() {
                 @Override
                 public void onCompletion(RecordMetadata metadata, Exception exception) {
                     if (exception != null) {
                    	 handleFailure(key, message, exception);
                     }
                 }
             });
         } catch (Exception e) {
        	 handleFailure(key, message, e);
         }  
    }
        
    private ProducerRecord<String, String> buildProducerRecord(String key, String value, Integer partition, String topic) {
        List<Header> recordHeaders = List.of(new RecordHeader("event-source", "scanner".getBytes()));
        return new ProducerRecord<>(topic, partition, key, value, recordHeaders);
    }
    
    public Producer<String, String> get() {
        Producer<String, String> result = this.producer;
        if (result == null) {
            synchronized(this) {
                result = this.producer;
                if(result == null) {
                    this.producer = result = this.initialize();
                }
            }
        }

        return result;
    }

    protected Producer<String, String> initialize() {
        Producer<String, String> producer = null;
        try {
            producer = createProducer();
        } catch (Exception e) {
        	handleFailure(UUID.randomUUID().toString(),"error creating producer", e);
        }
        return producer;
    }

    public boolean isInitialized() { 
    	return producer != null; 
    }
    
    
    private void handleFailure(String key, String value, Throwable ex) {
        log.error("Mandando uma mensagem com a exception {}", ex.getMessage());
        try {
            throw ex;
        } catch (Throwable throwable) {
            log.error("Error in OnFailure: {}", throwable.getMessage());
        }


    }
    
    protected Producer<String, String> createProducer() {
    	Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ProducerConfig.RETRIES_CONFIG, 0);
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
		props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		
        return new KafkaProducer<>(new HashMap<>(props));
    }

	
}
