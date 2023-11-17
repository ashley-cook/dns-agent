package homebrew.agent;

import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public abstract class ClassNodeTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        try {
            if (!this.transform(classNode)) {
                return null;
            }
        } catch (Throwable throwable) {
            log.error("Unable to run transformer: {}", getClass().toString());
            throwable.printStackTrace();
            return null;
        }

        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    public abstract boolean transform(ClassNode classNode) throws Exception;

    protected MethodNode getMethod(ClassNode classNode, String name, String descriptor) {
        List<MethodNode> methods = classNode.methods.stream()
                .filter(m -> m.name.equals(name))
                .collect(Collectors.toList());

        if (methods.size() == 1) {
            return methods.get(0);
        }

        if (descriptor == null) {
            throw new IllegalStateException("Ambiguous method definition");
        } else {
            Optional<MethodNode> methodNode = methods.stream().filter(m -> m.desc.equals(descriptor)).findAny();
            if (methodNode.isEmpty()) {
                throw new IllegalStateException("Method definition not found");
            } else {
                return methodNode.get();
            }
        }
    }

    protected MethodNode getMethod(ClassNode classNode, String name) {
        return getMethod(classNode, name, null);
    }

    protected MethodNode getMethod(ClassNode classNode, Method method) {
        return getMethod(classNode, method.getName(), Type.getMethodDescriptor(method));
    }

}