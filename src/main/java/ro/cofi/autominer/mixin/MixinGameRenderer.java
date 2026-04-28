package ro.cofi.autominer.mixin;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 此 Mixin 為預留擴充用，目前不做任何注入。
 * 核心自動挖掘邏輯完全由 MainTickHandler 透過 ClientTickEvents 實作，
 * 不需要 Mixin 干預。
 */
@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
    // 預留空間，未來若需要 hook render 可在此擴充
}
