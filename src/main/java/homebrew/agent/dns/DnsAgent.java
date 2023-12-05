package homebrew.agent.dns;

import java.lang.instrument.Instrumentation;
import java.net.InetAddress;

public class DnsAgent {

    public static void premain(String args, Instrumentation instrumentation) {
        InetAddress.class.getModule().addOpens(InetAddress.class.getPackageName(), INameService.class.getModule());

        try {
            INameService.install(
                    new FallbackNameService(
                            new AdvancedHostsFileNameService(System.getProperty("agent.dns.hosts")),
                            INameService.getPlatformNameService()
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
