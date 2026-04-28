package ro.cofi.autominer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.hit.HitResult;

public class MainTickHandler implements ClientTickEvents.EndTick {

    private boolean enabled = false;
    private boolean lastPressed = false;

    @Override
    public void onEndTick(MinecraftClient client) {
        try {
            tick(client);
        } catch (Exception e) {
            AutoMinerClient.LOGGER.error("Unexpected exception in tick", e);
        }
    }

    private void tick(MinecraftClient client) {
        // 切換鍵偵測
        boolean currentlyPressed = AutoMinerClient.toggleKey.isPressed();
        if (currentlyPressed && !lastPressed) {
            enabled = !enabled;
            AutoMinerClient.LOGGER.info("AutoMiner {}", enabled ? "enabled" : "disabled");
        }
        lastPressed = currentlyPressed;

        if (!enabled || client.player == null || client.world == null) {
            client.options.attackKey.setPressed(false);
            return;
        }

        ItemStack held = client.player.getMainHandStack();

        // 手持非鎬時停止
        if (!held.isIn(ItemTags.PICKAXES)) {
            client.options.attackKey.setPressed(false);
            return;
        }

        // 準心必須對著方塊
        if (client.crosshairTarget == null ||
            client.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            client.options.attackKey.setPressed(false);
            return;
        }

        // 模擬持續按住左鍵
        client.options.attackKey.setPressed(true);
    }
}
