package br.com.poc.slueth.sluethtest;

import java.nio.charset.StandardCharsets;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import zipkin2.Call;
import zipkin2.codec.Encoding;
import zipkin2.reporter.Sender;

@Slf4j
public class SplunkSender extends Sender {
	
	private boolean spanSent = false;
	private String topic;
	private Integer partition;
	private SplunkKafkaProducer splunkKafkaProducer;
	
	public SplunkSender(String topic, Integer partition) {
		this.topic = topic;
		this.partition = partition;
		this.splunkKafkaProducer = new SplunkKafkaProducer();
	}

    boolean isSpanSent() {
        return this.spanSent;
    }

	@Override
	public Encoding encoding() {
		 return Encoding.JSON;
	}

	@Override
	public int messageMaxBytes() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int messageSizeInBytes(List<byte[]> encodedSpans) {
		return encoding().listSizeInBytes(encodedSpans);
	}

	@Override
	public Call<Void> sendSpans(List<byte[]> encodedSpans) {
        this.spanSent = true;
        for(byte[] bytes :  encodedSpans){
        	String message = new String(bytes, StandardCharsets.UTF_8);
        	log.info(message);
        	this.splunkKafkaProducer.send(message, partition, topic);
            
        }
        return Call.create(null);
	}

}
