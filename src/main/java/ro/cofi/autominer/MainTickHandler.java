package ro.cofi.autominer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class MainTickHandler implements ClientTickEvents.EndTick {

    private boolean enabled = false;
    private BlockPos lastBlockPos = null;
    private Direction lastDirection = Direction.UP;

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

        if (!enabled || client.player == null || client.world == null
                || client.interactionManager == null) {
            return;
        }

        // 手持非鎬時停止
        ItemStack held = client.player.getMainHandStack();
        if (!held.isIn(ItemTags.PICKAXES)) {
            return;
        }

        // 有準心目標時更新記憶的方塊位置與面
        if (client.crosshairTarget != null &&
            client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult hit = (BlockHitResult) client.crosshairTarget;
            lastBlockPos = hit.getBlockPos();
            lastDirection = hit.getSide();
        }

        if (lastBlockPos == null) return;

        // 直接呼叫 interactionManager 驅動挖掘，不依賴按鍵
        ClientPlayerInteractionManager mgr = client.interactionManager;
        mgr.updateBlockBreakingProgress(lastBlockPos, lastDirection);
    }
}
