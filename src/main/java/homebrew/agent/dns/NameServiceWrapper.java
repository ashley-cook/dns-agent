package homebrew.agent.dns;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NameServiceWrapper implements INameService {

    private final Object nameService;

    public NameServiceWrapper(Object nameService) {
        this.nameService = nameService;
    }

    @Override
    public InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException {
        try {
            Class<?> nameServiceClass = nameService.getClass();
            Method method = nameServiceClass.getDeclaredMethod("lookupAllHostAddr", String.class);
            method.setAccessible(true);
            return (InetAddress[]) method.invoke(nameService, host);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof UnknownHostException) {
                throw (UnknownHostException) e.getCause();
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getHostByAddr(byte[] addr) throws UnknownHostException {
        try {
            Class<?> nameServiceClass = nameService.getClass();
            Method method = nameServiceClass.getDeclaredMethod("getHostByAddr", byte[].class);
            method.setAccessible(true);
            return (String) method.invoke(nameService, (Object) addr);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof UnknownHostException) {
                throw (UnknownHostException) e.getCause();
            }
            throw new RuntimeException(e);
        }
    }

}
