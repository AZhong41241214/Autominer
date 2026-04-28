package ro.cofi.autominer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.AxeItem;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class MainTickHandler implements ClientTickEvents.EndTick {

    /** 自動挖掘是否啟用 */
    private boolean enabled = false;

    @Override
    public void onEndTick(MinecraftClient client) {
        try {
            tick(client);
        } catch (Exception e) {
            AutoMinerClient.LOGGER.error("Unexpected exception in tick", e);
        }
    }

    private void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        // 偵測切換鍵
        while (AutoMinerClient.toggleKey.wasPressed()) {
            enabled = !enabled;
            String msgKey = enabled
                    ? "autominer.status.enabled"
                    : "autominer.status.disabled";
            client.player.sendMessage(Text.translatable(msgKey), true); // true = actionbar
        }

        if (!enabled) return;

        ClientPlayerEntity player = client.player;

        // 檢查手持物品是否為鎬（或斧/鏟，可自行調整）
        ItemStack held = player.getMainHandStack();
        Item item = held.getItem();
        boolean isPickaxe = item instanceof PickaxeItem;
        // 如果只想要鎬才觸發，移除下面兩行
        boolean isShovel = item instanceof ShovelItem;
        boolean isAxe = item instanceof AxeItem;

        if (!isPickaxe && !isShovel && !isAxe) return;

        // 確認準心對著方塊
        if (client.crosshairTarget == null) return;
        if (client.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        // 模擬持續按住滑鼠左鍵（攻擊/挖掘）
        // interactionManager.isBreakingBlock() 為 true 時表示正在挖
        // 直接呼叫 attackBlock 或觸發 attack key
        client.options.attackKey.setPressed(true);
    }
}
