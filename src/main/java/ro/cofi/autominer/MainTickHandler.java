package ro.cofi.autominer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class MainTickHandler implements ClientTickEvents.EndTick {

    private boolean enabled = false;
    private BlockPos lastBlockPos = null;

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
        while (AutoMinerClient.toggleKey.wasPressed()) {
            enabled = !enabled;
            AutoMinerClient.LOGGER.info("AutoMiner {}", enabled ? "enabled" : "disabled");
        }

        if (!enabled || client.player == null || client.world == null) {
            client.options.attackKey.setPressed(false);
            return;
        }

        // 手持非鎬時停止
        ItemStack held = client.player.getMainHandStack();
        if (!held.isIn(ItemTags.PICKAXES)) {
            client.options.attackKey.setPressed(false);
            return;
        }

        // 有準心目標時更新記憶的方塊位置
        if (client.crosshairTarget != null &&
            client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            lastBlockPos = ((BlockHitResult) client.crosshairTarget).getBlockPos();
        }

        // 有記憶的目標方塊就持續挖
        if (lastBlockPos != null) {
            client.options.attackKey.setPressed(true);
        } else {
            client.options.attackKey.setPressed(false);
        }
    }
}
