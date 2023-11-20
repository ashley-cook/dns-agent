# dns-agent

A Java agent that changes hostname resolution to use the JDK 9+
HostsFileNameService but fall back on PlatformNameService when 
the given hostname is not found in the hosts file.

This is **not likely to work on Java 8** or below, and has only been tested on Java 11.

## Usage

Example JVM arguments are given:
```
-javaagent:dns-agent-1.0.jar -Djdk.net.hosts.file=hosts.txt
```

The hosts file follows the standard `/etc/hosts` format and an example is given:
```
127.0.0.1	my.example.website
```