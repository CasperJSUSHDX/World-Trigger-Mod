package com.JSUSHDX.WorldTriggerMod.gametest.support;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

/**
 * A {@link GameTestInstance} that runs a {@link Consumer<GameTestHelper>} captured directly at
 * registration time, instead of looking one up from {@code Registries.TEST_FUNCTION} the way
 * vanilla's {@code FunctionGameTestInstance} does.
 * <p>
 * That registry turned out to be unusable for mod test bodies: {@code BuiltInRegistries.TEST_FUNCTION}
 * is frozen before any mod constructor runs (confirmed by an actual {@code runGameTestServer}
 * failure - {@code IllegalStateException: Registry is already frozen} - when trying
 * {@code Registry.register(BuiltInRegistries.TEST_FUNCTION, ...)} from the mod constructor, and
 * {@code TestFunctionLoader.registerLoader(...)} is equally too late since its one-shot replay
 * already ran). This class sidesteps the registry entirely by holding the function directly, since
 * all our tests are registered programmatically via {@code RegisterGameTestsEvent} rather than
 * deserialized from datapack JSON, so a real {@link #codec()} is never exercised.
 */
public class WTFunctionTestInstance extends GameTestInstance {
    private final Consumer<GameTestHelper> body;

    public WTFunctionTestInstance(Consumer<GameTestHelper> body, TestData<Holder<TestEnvironmentDefinition<?>>> info) {
        super(info);
        this.body = body;
    }

    @Override
    public void run(GameTestHelper helper) {
        this.body.accept(helper);
    }

    @Override
    public MapCodec<? extends GameTestInstance> codec() {
        throw new UnsupportedOperationException("wtmod test instances are registered programmatically, not data-driven");
    }

    @Override
    protected MutableComponent typeDescription() {
        return Component.translatable("test_instance.type.function");
    }
}
