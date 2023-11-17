package homebrew.agent.util;

import java.util.function.Consumer;

/**
 * Minimal class used to track lambdas for method hooking. Links to very little in order to be compatible with
 * Bootstrap class loading. Exposed to the Bootstrap class loader via the Boot-Class-Path manifest attribute.
 */
public class Hooking$State {

    public static final LambdaTable<Consumer<?>> consumerTable = new LambdaTable<>();

}
