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

import java.util.Random;

public class MainTickHandler implements ClientTickEvents.EndTick {

    private boolean enabled = false;
    private BlockPos lastBlockPos = null;
    private Direction lastDirection = Direction.UP;

    private final Random random = new Random();

    // 固定 20 秒挖掘後暫停 10 秒
    private static final int MINE_DURATION = 20 * 20;  // 400 ticks
    private static final int PAUSE_DURATION = 10 * 20; // 200 ticks

    private int mineTicksRemaining = MINE_DURATION;
    private int pauseTicksRemaining = 0;

    private int skipTicks = 0;

    public boolean isEnabled() {
        return enabled;
    }

    public int getPauseTicksRemaining() {
        return pauseTicksRemaining;
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        try {
            tick(client);
        } catch (Exception e) {
            AutoMinerClient.LOGGER.error("Unexpected exception in tick", e);
        }
    }

    private void tick(MinecraftClient client) {
        while (AutoMinerClient.toggleKey.wasPressed()) {
            enabled = !enabled;
            AutoMinerClient.LOGGER.info("AutoMiner {}", enabled ? "enabled" : "disabled");
        }

        if (!enabled || client.player == null || client.world == null
                || client.interactionManager == null) {
            return;
        }

        ItemStack held = client.player.getMainHandStack();
        if (!held.isIn(ItemTags.PICKAXES)) {
            return;
        }

        if (client.crosshairTarget != null &&
            client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult hit = (BlockHitResult) client.crosshairTarget;
            lastBlockPos = hit.getBlockPos();
            lastDirection = hit.getSide();
        }

        if (lastBlockPos == null) return;

        // 暫停中
        if (pauseTicksRemaining > 0) {
            pauseTicksRemaining--;
            if (pauseTicksRemaining == 0) {
                mineTicksRemaining = MINE_DURATION;
            }
            return;
        }

        // 挖掘倒數
        mineTicksRemaining--;
        if (mineTicksRemaining <= 0) {
            pauseTicksRemaining = PAUSE_DURATION;
            return;
        }

        // 隨機跳過幾 tick
        if (skipTicks > 0) {
            skipTicks--;
            return;
        }
        if (random.nextInt(20) == 0) {
            skipTicks = random.nextInt(3);
        }

        ClientPlayerInteractionManager mgr = client.interactionManager;
        mgr.updateBlockBreakingProgress(lastBlockPos, lastDirection);
    }
}
