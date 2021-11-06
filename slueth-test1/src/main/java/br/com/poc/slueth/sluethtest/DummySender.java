package br.com.poc.slueth.sluethtest;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import zipkin2.Call;
import zipkin2.codec.Encoding;
import zipkin2.reporter.Sender;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class DummySender extends Sender {

    private boolean spanSent = false;

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
            log.info(new String(bytes, StandardCharsets.UTF_8)) ;
        }
        return Call.create(null);
    }
}
