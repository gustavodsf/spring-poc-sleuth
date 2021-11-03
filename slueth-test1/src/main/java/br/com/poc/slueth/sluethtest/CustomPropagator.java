package br.com.poc.slueth.sluethtest;

import brave.internal.propagation.StringPropagationAdapter;
import brave.propagation.Propagation;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import org.springframework.stereotype.Component;
import zipkin2.internal.HexCodec;

import java.util.Arrays;
import java.util.List;

// @Component
class CustomPropagator extends Propagation.Factory implements Propagation<String>
{
    @Override
    public List<String> keys() {
        return Arrays.asList("myCustomTraceId", "myCustomSpanId");
    }
    @Override
    public <R> TraceContext.Injector<R> injector(Setter<R, String> setter) {
        return (traceContext, request) -> {
            setter.put(request, "myCustomTraceId", "Gustavo Teste 123");
            setter.put(request, "myCustomSpanId", "Daniel 123");
        };
    }
    @Override
    public <R> TraceContext.Extractor<R> extractor(Getter<R, String> getter) {
        return request ->
                TraceContextOrSamplingFlags.create(TraceContext.newBuilder()
                        .traceId(HexCodec.lowerHexToUnsignedLong(getter.get(request,
                                "myCustomTraceId")))
                        .spanId(HexCodec.lowerHexToUnsignedLong(getter.get(request,
                                "myCustomSpanId"))).build());
    }
    @Override
    public <K> Propagation<K> create(KeyFactory<K> keyFactory) {
        return StringPropagationAdapter.create(this, keyFactory);
    }
}
