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
        // 切換鍵：偵測按下邊緣（按下瞬間才觸發，不重複）
        boolean currentlyPressed = AutoMinerClient.toggleKey.isPressed();
        if (currentlyPressed && !lastPressed) {
            enabled = !enabled;
            AutoMinerClient.LOGGER.info("AutoMiner {}", enabled ? "enabled" : "disabled");
        }
        lastPressed = currentlyPressed;

        if (!enabled) return;
        if (client.player == null || client.world == null) return;

        ItemStack held = client.player.getMainHandStack();

        // 使用 ItemTag 判斷，相容所有材質與模組新增的鎬
        if (!held.isIn(ItemTags.PICKAXES)) return;

        // 準心必須對著方塊
        if (client.crosshairTarget == null) return;
        if (client.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        // 模擬持續按住左鍵（挖掘）
        client.options.attackKey.setPressed(true);
    }
}
