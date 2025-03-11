package io.github.spigotrce.paradiseclientfabric.mixin.inject.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.spigotrce.paradiseclientfabric.Constants;
import io.github.spigotrce.paradiseclientfabric.Helper;
import io.github.spigotrce.paradiseclientfabric.ParadiseClient_Fabric;
import io.github.spigotrce.paradiseclientfabric.WallPaper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.client.realms.gui.screen.RealmsNotificationsScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin class to customize the behavior of the Title Screen in Minecraft.
 * <p>
 * This class modifies the Title Screen to include a custom button recommending
 * the installation of "ViaFabricPlus" and customizes the background fade effect.
 * It also displays additional information about the client and game version.
 * </p>
 *
 * @author SpigotRCE
 * @since 2.9
 */
@SuppressWarnings("unused")
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    /**
     * The splash text renderer used to display splash texts on the Title Screen.
     */
    @Nullable
    @Shadow
    public SplashTextRenderer splashText;

    /**
     * The Realms Notifications Screen displayed on the Title Screen if active.
     */
    @Nullable
    @Shadow
    private RealmsNotificationsScreen realmsNotificationGui;

    /**
     * Alpha value for the background fade effect on the Title Screen.
     */
    @Mutable
    @Shadow
    public float backgroundAlpha;

    /**
     * Flag indicating whether the background fade effect is enabled.
     */
    @Mutable
    @Shadow
    public boolean doBackgroundFade;

    /**
     * The start time for the background fade effect, in milliseconds.
     */
    @Mutable
    @Shadow
    public long backgroundFadeStart;

    /**
     * The logo drawer used to render the logo on the Title Screen.
     */
    @Final
    @Shadow
    public LogoDrawer logoDrawer;

    /**
     * Constructs a new instance of the TitleScreenMixin.
     *
     * @param title The title of the screen.
     */
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    /**
     * Injects a custom button into the Title Screen if "viafabricplus" is not loaded.
     * The button directs the user to a URL for installation.
     *
     * @param ci Callback information.
     */
    @Inject(method = "init", at = @At(value = "TAIL"))
    public void init(CallbackInfo ci) {
        if (!FabricLoader.getInstance().isModLoaded("viafabricplus")) {

            String VFP_REMINDER = "We recommend installing ViaFabricPlus";
            this.addDrawableChild(ButtonWidget.builder(Text.literal(VFP_REMINDER),
                            onPress -> {
                                Util.getOperatingSystem().open("https://modrinth.com/mod/viafabricplus/versions");
                                MinecraftClient.getInstance().setScreen(new TitleScreen());
                            })
                    .width(this.textRenderer.getWidth(VFP_REMINDER) + 5)
                    .position((this.width / 2) - ((this.textRenderer.getWidth(VFP_REMINDER) + 5) / 2), this.height / 4 + 48 + 72 + 12 + 35 + 33)
                    .build()
            );
        }

        Text updateMessage1 = Helper.parseColoredText("&2Current version: &1" + Constants.VERSION + " &2Latetst version: &1" + ParadiseClient_Fabric.MISC_MOD.latestVersion + " &fClick to download");
        if (ParadiseClient_Fabric.MISC_MOD.isClientOutdated) {
            this.addDrawableChild(new PressableTextWidget(this.width - this.textRenderer.getWidth(updateMessage1) - 2, this.height - 20, this.textRenderer.getWidth(updateMessage1), 10, updateMessage1, (button) -> {
                Util.getOperatingSystem().open("https://paradise-client.xyz/downloads" + ParadiseClient_Fabric.MISC_MOD.latestVersion);
                MinecraftClient.getInstance().setScreen(new TitleScreen());
            }, this.textRenderer));
        }

        // Adding a button to switch themes dynamically
        // This button toggles between "hack" and "particle" themes
        String currentTheme = WallPaper.getTheme();
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Theme: " + WallPaper.getTheme()),
                        onPress -> {
                            // Switch between available themes
                            String newTheme = switch (WallPaper.getTheme()) {
                                case "ParadiseHack" -> "ParadiseParticle";
                                default -> "ParadiseParticle";
                            };

                            // Update theme in ConfigManager and interface
                            WallPaper.setTheme(newTheme);

                            // Update button text
                            onPress.setMessage(Text.literal("Theme: " + newTheme));
                        })
                .width(150)
                .position(this.width / 1 - 60, this.height / 4 + 160)
                .build()
        );

    }

    /**
     * Checks if the Realms Notifications GUI is displayed.
     * This method is shadowed from the original TitleScreen class.
     *
     * @return True if the Realms Notifications GUI is displayed, false otherwise.
     */
    @Shadow
    private boolean isRealmsNotificationsGuiDisplayed() {
        return false;
    }

    /**
     * Sets the alpha value for widgets.
     * This method is shadowed from the original TitleScreen class.
     *
     * @param alpha The alpha value to set.
     */
    @Shadow
    private void setWidgetAlpha(float alpha) {
    }

    /**
     * Renders the Title Screen with custom background and additional information.
     * This method handles background fading and custom text rendering.
     *
     * @param context The draw context used for rendering.
     * @param mouseX  The mouse X position.
     * @param mouseY  The mouse Y position.
     * @param delta   The delta time since the last frame.
     * @param ci      Callback information.
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.backgroundFadeStart == 0L && this.doBackgroundFade)
            this.backgroundFadeStart = Util.getMeasuringTimeMs();

        WallPaper.render(context, this.width, this.height);

        float f = 1.0F;
        if (this.doBackgroundFade) {
            float g = (float) (Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 2000.0F;
            if (g > 1.0F) {
                this.doBackgroundFade = false;
                this.backgroundAlpha = 1.0F;
            } else {
                g = MathHelper.clamp(g, 0.0F, 1.0F);
                f = MathHelper.clampedMap(g, 0.5F, 1.0F, 0.0F, 1.0F);
                this.backgroundAlpha = MathHelper.clampedMap(g, 0.0F, 0.5F, 0.0F, 1.0F);
            }

            this.setWidgetAlpha(f);
        }

        int i = MathHelper.ceil(f * 255.0F) << 24;
        if ((i & -67108864) != 0) {
            super.render(context, mouseX, mouseY, delta);
            this.logoDrawer.draw(context, this.width, f);
            if (this.splashText != null)
                if (!(Boolean) this.client.options.getHideSplashTexts().getValue())
                    this.splashText.render(context, this.width, this.textRenderer, i);
            context.drawTextWithShadow(this.textRenderer, Constants.WINDOW_TITLE, 2, this.height - 10, 16777215 | i);
            if (this.isRealmsNotificationsGuiDisplayed() && f >= 1.0F) {
                RenderSystem.enableDepthTest();
                this.realmsNotificationGui.render(context, mouseX, mouseY, delta);
            }
        }
        super.render(context, mouseX, mouseY, delta);
        ci.cancel();
    }
}
