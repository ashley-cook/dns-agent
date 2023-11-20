package homebrew.agent.dns;

import java.lang.reflect.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * <a href="https://stackoverflow.com/a/50000475">Stack Overflow</a>
 * <br/>
 * <a href="https://archive.ph/azvrE">Archived</a>
 */
public interface INameService extends InvocationHandler {

    static INameService getPlatformNameService() {
        try {
            final Class<?> clazz = Class.forName("java.net.InetAddress$PlatformNameService");
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return new NameServiceWrapper(constructor.newInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static INameService getHostsFileNameService(String hostsFileName) {
        try {
            final Class<?> clazz = Class.forName("java.net.InetAddress$HostsFileNameService");
            Constructor<?> constructor = clazz.getConstructor(String.class);
            constructor.setAccessible(true);
            return new NameServiceWrapper(constructor.newInstance(hostsFileName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void install(final INameService dns) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
        final Class<?> inetAddressClass = InetAddress.class;
        final Class<?> iface = Class.forName("java.net.InetAddress$NameService");
        Field nameServiceField = inetAddressClass.getDeclaredField("nameService");
        Object neu = Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] { iface }, dns);
        nameServiceField.setAccessible(true);
        nameServiceField.set(inetAddressClass, neu);
    }

    /**
     * Lookup a host mapping by name. Retrieve the IP addresses associated with a host
     *
     * @param host the specified hostname
     * @return array of IP addresses for the requested host
     * @throws UnknownHostException  if no IP address for the {@code host} could be found
     */
    InetAddress[] lookupAllHostAddr(final String host) throws UnknownHostException;

    /**
     * Lookup the host corresponding to the IP address provided
     *
     * @param addr byte array representing an IP address
     * @return {@code String} representing the host name mapping
     * @throws UnknownHostException
     *             if no host found for the specified IP address
     */
    String getHostByAddr(final byte[] addr) throws UnknownHostException;

    @Override default Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        switch(method.getName()) {
            case "lookupAllHostAddr": return lookupAllHostAddr((String)args[0]);
            case "getHostByAddr"    : return getHostByAddr    ((byte[])args[0]);
            default                 :
                final StringBuilder o = new StringBuilder();
                o.append(method.getReturnType().getCanonicalName());
                o.append(" ");
                o.append(method.getName());
                o.append("(");
                final Class<?>[] ps = method.getParameterTypes();
                for(int i=0;i<ps.length;++i) {
                    if(i>0) o.append(", ");
                    o.append(ps[i].getCanonicalName()).append(" p").append(i);
                }
                o.append(")");
                throw new UnsupportedOperationException(o.toString());
        }
    }
}