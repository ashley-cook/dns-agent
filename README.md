# dns-agent

A Java agent that changes hostname resolution to use an advanced
/etc/hosts-like file format but fall back on PlatformNameService when 
the given hostname is not found in the hosts file.

This is **not likely to work on Java 8** or below, and has only been tested on Java 11.

## Usage

Example JVM arguments are given:
```
-javaagent:dns-agent-1.1.jar -Dagent.dns.hosts=hosts.txt
```

The hosts file is inspired by [resolv_wrapper](https://cwrap.org/resolv_wrapper.html) and an example 
is given below:
```
# Advanced /etc/hosts file format
# Comments begin with an octothorpe
# Supported types: A, AAAA, CNAME

# TYPE          RECORD NAME             RECORD VALUE          
A               my.example.domain       127.0.0.1
A               *.my.example.domain     127.0.0.1
AAAA            my.example.domain       ::1
AAAA            *.my.example.domain     ::1
CNAME           user.example.domain     my.example.domain
CNAME           *.user.example.domain   my.example.domain   # I'm a hanging comment!
```

Please note that only A, AAAA, and CNAME record types are currently implemented.
