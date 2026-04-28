package ro.cofi.autominer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoMinerClient implements ClientModInitializer {

    public static final String MOD_ID = "auto-miner";
    public static final String MOD_NAME = "AutoMiner";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing {}", MOD_NAME);

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.auto-miner.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.auto-miner"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(new MainTickHandler());

        LOGGER.info("{} successfully loaded", MOD_NAME);
    }
}
