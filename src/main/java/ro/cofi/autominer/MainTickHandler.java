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

    private int pauseTicksRemaining = 0;
    private int nextPauseTicks = randomNextPause();
    private int skipTicks = 0;

    private int randomNextPause() {
        // 每 8~20 秒暫停一次（160~400 ticks）
        return 160 + random.nextInt(240);
    }

    private int randomPauseDuration() {
        // 暫停 0.5~2 秒（10~40 ticks）
        return 10 + random.nextInt(30);
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

        // 暫停計時器
        if (pauseTicksRemaining > 0) {
            pauseTicksRemaining--;
            return;
        }

        // 計算下次暫停
        nextPauseTicks--;
        if (nextPauseTicks <= 0) {
            pauseTicksRemaining = randomPauseDuration();
            nextPauseTicks = randomNextPause();
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

        // 直接呼叫 interactionManager 驅動挖掘
        ClientPlayerInteractionManager mgr = client.interactionManager;
        mgr.updateBlockBreakingProgress(lastBlockPos, lastDirection);
    }
}
