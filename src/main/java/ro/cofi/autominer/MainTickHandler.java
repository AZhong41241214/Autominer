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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Random;

public class MainTickHandler implements ClientTickEvents.EndTick {

    private boolean enabled = false;

    private final Random random = new Random();

    private static final int MINE_DURATION  = 12 * 20;
    private static final int PAUSE_DURATION = 4 * 20;

    private int mineTicksRemaining  = MINE_DURATION;
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

        // 手持非鎬時停止
        ItemStack held = client.player.getMainHandStack();
        if (!held.isIn(ItemTags.PICKAXES)) {
            return;
        }

        // 自己計算射線，不依賴 crosshairTarget
        BlockHitResult hit = calcCrosshairTarget(client);
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = hit.getBlockPos();
        Direction dir = hit.getSide();

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

        // 每 4 tick 才挖一次
        if (client.world.getTime() % 4 != 0) return;

        client.interactionManager.updateBlockBreakingProgress(pos, dir);
    }

    /**
     * 自己用玩家視角計算準心射線，不依賴 client.crosshairTarget
     * 失去焦點後 yaw/pitch 不會重置，所以可以繼續運作
     */
    private BlockHitResult calcCrosshairTarget(MinecraftClient client) {
        if (client.player == null || client.world == null) return null;

        float pitch = client.player.getPitch();
        float yaw   = client.player.getYaw();

        float f  = (float) Math.cos(-yaw * 0.017453292F - Math.PI);
        float f1 = (float) Math.sin(-yaw * 0.017453292F - Math.PI);
        float f2 = (float) -Math.cos(-pitch * 0.017453292F);
        float f3 = (float) Math.sin(-pitch * 0.017453292F);

        Vec3d dir    = new Vec3d(f1 * f2, f3, f * f2);
        Vec3d eyePos = client.player.getCameraPosVec(1.0f);
        Vec3d target = eyePos.add(dir.multiply(client.player.getBlockInteractionRange()));

        HitResult result = client.world.raycast(new RaycastContext(
                eyePos, target,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                client.player
        ));

        if (result instanceof BlockHitResult bhr) {
            return bhr;
        }
        return null;
    }
}
