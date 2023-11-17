package homebrew.agent.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class Hooking implements Opcodes {

    public static InsnList buildConsumerCall(int index, String desc, int varIndex) {
        InsnList insnList = new InsnList();
        insnList.add(new FieldInsnNode(
                GETSTATIC,
                Type.getInternalName(Hooking$State.class),
                "consumerTable",
                "L" + Type.getInternalName(LambdaTable.class) + ";"));
        insnList.add(new LdcInsnNode(index));
        insnList.add(new MethodInsnNode(
                INVOKESTATIC,
                Type.getInternalName(Integer.class),
                "valueOf",
                "(I)L" + Type.getInternalName(Integer.class) + ";"));
        insnList.add(new MethodInsnNode(
                INVOKEVIRTUAL,
                Type.getInternalName(LambdaTable.class),
                "get",
                "(L" + Type.getInternalName(Integer.class) + ";)L" + Type.getInternalName(Object.class) + ";"));
        insnList.add(new TypeInsnNode(
                CHECKCAST,
                Type.getInternalName(Consumer.class)));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, varIndex));
        insnList.add(new MethodInsnNode(
                INVOKEINTERFACE,
                Type.getInternalName(Consumer.class),
                "accept",
                desc
        ));
        return insnList;
    }

    public static void before(MethodNode targetNode, Consumer<?> consumer, int varIndex) throws Exception {
        Method applyMethod = Consumer.class.getDeclaredMethod("accept", Object.class);
        String desc = Type.getMethodDescriptor(applyMethod);

        InsnList insnNodes = targetNode.instructions;
        insnNodes.insertBefore(insnNodes.getFirst(), buildConsumerCall(
                Hooking$State.consumerTable.put(consumer), desc, varIndex));
    }

}
