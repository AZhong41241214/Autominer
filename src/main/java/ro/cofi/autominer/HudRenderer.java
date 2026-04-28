package ro.cofi.autominer;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class HudRenderer implements HudRenderCallback {

    private final MainTickHandler handler;

    public HudRenderer(MainTickHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        if (!handler.isEnabled()) return;

        int pauseRemaining = handler.getPauseTicksRemaining();
        if (pauseRemaining <= 0) return;

        // 換算成秒（無條件進位）
        int seconds = (int) Math.ceil(pauseRemaining / 20.0);
        String text = "§e暫停中... " + seconds + "s";

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int textWidth = client.textRenderer.getWidth(text);

        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight / 2 + 20; // 準心下方

        context.drawTextWithShadow(client.textRenderer, text, x, y, 0xFFFFFF);
    }
}
