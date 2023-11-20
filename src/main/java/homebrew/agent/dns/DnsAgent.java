package homebrew.agent.dns;

import java.lang.instrument.Instrumentation;
import java.net.InetAddress;

public class DnsAgent {

    public static void premain(String args, Instrumentation instrumentation) {
        InetAddress.class.getModule().addOpens(InetAddress.class.getPackageName(), INameService.class.getModule());

        try {
            INameService.install(new HostsFileFallbackResolver());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
