package br.com.poc.slueth.sluethtest;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
public class TraceProperties {

    private String environment = "dev";

    private String name;

    private String tag;

    private String uuidRequestName = "uuid_request";

    private String hostRequestName = "x-forwarded-for";

    private List<String> sanitizes = new ArrayList<>();
}