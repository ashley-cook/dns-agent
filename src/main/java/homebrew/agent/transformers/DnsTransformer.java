package homebrew.agent.transformers;

import homebrew.agent.ClassNodeTransformer;
import homebrew.agent.util.Hooking;
import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.net.InetAddress;

@Slf4j
public class DnsTransformer extends ClassNodeTransformer {

    @Override
    public boolean transform(ClassNode classNode) throws Exception {
        if (!classNode.name.equals("java/net/InetAddress")) {
            return false;
        }
        log.info("Found {}", classNode.name);

        Method getByName = InetAddress.class.getDeclaredMethod("getByName", String.class);
        MethodNode getByNameMethod = getMethod(classNode, getByName);

        Hooking.before(getByNameMethod, this::hook, 0);

        return true;
    }

    private void hook(Object object) {
        String host = (String) object;
        log.info("getByName: {}", host);
    }

}