package homebrew.agent.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class FallbackNameService implements INameService {

    private final INameService[] services;

    public FallbackNameService(INameService... services) {
        this.services = services;
    }

    @Override
    public InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException {
        UnknownHostException unknownHostException = null;
        for (INameService nameService : services) {
            try {
                return nameService.lookupAllHostAddr(host);
            } catch (UnknownHostException exception) {
                unknownHostException = exception;
            }
        }

        throw (unknownHostException != null) ? unknownHostException : new UnknownHostException("Zoinks!");
    }

    @Override
    public String getHostByAddr(byte[] addr) throws UnknownHostException {
        UnknownHostException unknownHostException = null;
        for (INameService nameService : services) {
            try {
                return nameService.getHostByAddr(addr);
            } catch (UnknownHostException exception) {
                unknownHostException = exception;
            }
        }

        throw (unknownHostException != null) ? unknownHostException : new UnknownHostException("Zoinks!");
    }

}
