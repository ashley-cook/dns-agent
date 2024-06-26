package homebrew.agent.dns;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdvancedHostsFileNameService implements INameService {

    private static final int INADDR4SZ = 4;
    private static final int INADDR16SZ = 16;
    private static final int INT16SZ = 2;

    private static boolean isIPv4MappedAddress(byte[] addr) {
        if (addr.length < INADDR16SZ) {
            return false;
        }
        return (addr[0] == 0x00) && (addr[1] == 0x00) &&
                (addr[2] == 0x00) && (addr[3] == 0x00) &&
                (addr[4] == 0x00) && (addr[5] == 0x00) &&
                (addr[6] == 0x00) && (addr[7] == 0x00) &&
                (addr[8] == 0x00) && (addr[9] == 0x00) &&
                (addr[10] == (byte) 0xff) &&
                (addr[11] == (byte) 0xff);
    }

    private static byte[] convertFromIPv4MappedAddress(byte[] addr) {
        if (isIPv4MappedAddress(addr)) {
            byte[] newAddr = new byte[INADDR4SZ];
            System.arraycopy(addr, 12, newAddr, 0, INADDR4SZ);
            return newAddr;
        }
        return null;
    }

    private final String hostsFile;

    public AdvancedHostsFileNameService(String hostsFile) {
        this.hostsFile = hostsFile;
    }

    public static byte[] textToNumericFormatV4(String src)
    {
        byte[] res = new byte[INADDR4SZ];

        long tmpValue = 0;
        int currByte = 0;
        boolean newOctet = true;

        int len = src.length();
        if (len == 0 || len > 15) {
            return null;
        }

        for (int i = 0; i < len; i++) {
            char c = src.charAt(i);
            if (c == '.') {
                if (newOctet || tmpValue < 0 || tmpValue > 0xff || currByte == 3) {
                    return null;
                }
                res[currByte++] = (byte) (tmpValue & 0xff);
                tmpValue = 0;
                newOctet = true;
            } else {
                int digit = Character.digit(c, 10);
                if (digit < 0) {
                    return null;
                }
                tmpValue *= 10;
                tmpValue += digit;
                newOctet = false;
            }
        }
        if (newOctet || tmpValue < 0 || tmpValue >= (1L << ((4 - currByte) * 8))) {
            return null;
        }
        switch (currByte) {
            case 0:
                res[0] = (byte) ((tmpValue >> 24) & 0xff);
            case 1:
                res[1] = (byte) ((tmpValue >> 16) & 0xff);
            case 2:
                res[2] = (byte) ((tmpValue >>  8) & 0xff);
            case 3:
                res[3] = (byte) ((tmpValue) & 0xff);
        }
        return res;
    }

    public static byte[] textToNumericFormatV6(String src)
    {
        // Shortest valid string is "::", hence at least 2 chars
        if (src.length() < 2) {
            return null;
        }

        int colonp;
        char ch;
        boolean saw_xdigit;
        int val;
        char[] srcb = src.toCharArray();
        byte[] dst = new byte[INADDR16SZ];

        int srcb_length = srcb.length;
        int pc = src.indexOf ('%');
        if (pc == srcb_length -1) {
            return null;
        }

        if (pc != -1) {
            srcb_length = pc;
        }

        colonp = -1;
        int i = 0, j = 0;
        /* Leading :: requires some special handling. */
        if (srcb[i] == ':')
            if (srcb[++i] != ':')
                return null;
        int curtok = i;
        saw_xdigit = false;
        val = 0;
        while (i < srcb_length) {
            ch = srcb[i++];
            int chval = Character.digit(ch, 16);
            if (chval != -1) {
                val <<= 4;
                val |= chval;
                if (val > 0xffff)
                    return null;
                saw_xdigit = true;
                continue;
            }
            if (ch == ':') {
                curtok = i;
                if (!saw_xdigit) {
                    if (colonp != -1)
                        return null;
                    colonp = j;
                    continue;
                } else if (i == srcb_length) {
                    return null;
                }
                if (j + INT16SZ > INADDR16SZ)
                    return null;
                dst[j++] = (byte) ((val >> 8) & 0xff);
                dst[j++] = (byte) (val & 0xff);
                saw_xdigit = false;
                val = 0;
                continue;
            }
            if (ch == '.' && ((j + INADDR4SZ) <= INADDR16SZ)) {
                String ia4 = src.substring(curtok, srcb_length);
                /* check this IPv4 address has 3 dots, ie. A.B.C.D */
                int dot_count = 0, index=0;
                while ((index = ia4.indexOf ('.', index)) != -1) {
                    dot_count ++;
                    index ++;
                }
                if (dot_count != 3) {
                    return null;
                }
                byte[] v4addr = textToNumericFormatV4(ia4);
                if (v4addr == null) {
                    return null;
                }
                for (int k = 0; k < INADDR4SZ; k++) {
                    dst[j++] = v4addr[k];
                }
                saw_xdigit = false;
                break;  /* '\0' was seen by inet_pton4(). */
            }
            return null;
        }
        if (saw_xdigit) {
            if (j + INT16SZ > INADDR16SZ)
                return null;
            dst[j++] = (byte) ((val >> 8) & 0xff);
            dst[j++] = (byte) (val & 0xff);
        }

        if (colonp != -1) {
            int n = j - colonp;

            if (j == INADDR16SZ)
                return null;
            for (i = 1; i <= n; i++) {
                dst[INADDR16SZ - i] = dst[colonp + n - i];
                dst[colonp + n - i] = 0;
            }
            j = INADDR16SZ;
        }
        if (j != INADDR16SZ)
            return null;
        byte[] newdst = convertFromIPv4MappedAddress(dst);
        return Objects.requireNonNullElse(newdst, dst);
    }

    private static byte[] createAddressByteArray(String addrStr) {
        byte[] addrArray;
        addrArray = textToNumericFormatV4(addrStr);
        if (addrArray == null) {
            addrArray = textToNumericFormatV6(addrStr);
        }
        return addrArray;
    }

    private static boolean recordMatches(String host, String recordName) {
        if (recordName.endsWith(".")) {
            recordName = recordName.substring(0, recordName.length() - 1);
        }

        // exact match
        // look at the happy couple
        // they were made for each other
        // it's like love at first sight
        // get 'em out of here
        if (recordName.equalsIgnoreCase(host)) {
            return true;
        }

        if (!recordName.contains("*")) {
            return false;
        }

        // abc.example.com
        // A    *.example.com   127.0.0.1
        // A    abc.example.com 127.0.0.1

        int wildcardIndex = recordName.indexOf("*");
        String root = recordName.substring(wildcardIndex + 1);
        return host.endsWith(root);
    }

    @Override
    public InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException {
        List<InetAddress> addresses = new ArrayList<>();

        if (hostsFile == null) {
            throw new UnknownHostException("No hosts file specified");
        }

        try {
            for (String line : Files.readAllLines(Paths.get(hostsFile))) {
                // strip comments
                if (line.contains("#")) {
                    int commentIndex = line.indexOf("#");
                    line = line.substring(0, commentIndex);
                }

                String[] chunks = line.split("\\s+", 3);
                if (chunks.length < 3) {
                    continue;
                }

                String type = chunks[0];
                String recordName = chunks[1];
                String recordValue = chunks[2];

                if (!recordMatches(host, recordName)) {
                    continue;
                }

                switch (type) {
                    case "A":
                    case "AAAA":
                        byte[] addr = createAddressByteArray(recordValue);
                        addresses.add(InetAddress.getByAddress(host, addr));
                        break;
                    case "CNAME":
                        for (InetAddress result : InetAddress.getAllByName(recordValue)) {
                            addresses.add(InetAddress.getByAddress(host, result.getAddress()));
                        }
                        break;
                    default:
                        throw new IllegalStateException("Invalid record type: " + type);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (addresses.size() == 0) {
            throw new UnknownHostException("Unable to resolve host " + host
                    + " in hosts file " + hostsFile);
        }

        return addresses.toArray(new InetAddress[0]);
    }

    @Override
    public String getHostByAddr(byte[] addr) throws UnknownHostException {
        throw new UnknownHostException("Dunno, sorry!");
    }

}
