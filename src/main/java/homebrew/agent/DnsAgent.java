package homebrew.agent;

import homebrew.agent.transformers.DnsTransformer;
import lombok.extern.slf4j.Slf4j;

import java.lang.instrument.Instrumentation;

@Slf4j
public class DnsAgent {

    public static void premain(String args, Instrumentation instrumentation) {
        log.info("Hello, world!");
        instrumentation.addTransformer(new DnsTransformer(), true);
    }

}
