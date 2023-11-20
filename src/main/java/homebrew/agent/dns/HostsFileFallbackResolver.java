package homebrew.agent.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * INetAddress.NameService implementation that provides the functionality of
 * Java >=9 HostsFileNameService, but will fall back on the platform name service.
 */
public class HostsFileFallbackResolver implements INameService {

    private final INameService platformNameService;
    private final INameService hostsFileNameService;

    public HostsFileFallbackResolver() {
        this.platformNameService = INameService.getPlatformNameService();
        this.hostsFileNameService = INameService.getHostsFileNameService(System.getProperty("jdk.net.hosts.file"));
    }

    @Override
    public InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException {
        try {
            return hostsFileNameService.lookupAllHostAddr(host);
        } catch (UnknownHostException e) {
            return platformNameService.lookupAllHostAddr(host);
        }
    }

    @Override
    public String getHostByAddr(byte[] addr) throws UnknownHostException {
        try {
            return hostsFileNameService.getHostByAddr(addr);
        } catch (UnknownHostException e) {
            return platformNameService.getHostByAddr(addr);
        }
    }

}
