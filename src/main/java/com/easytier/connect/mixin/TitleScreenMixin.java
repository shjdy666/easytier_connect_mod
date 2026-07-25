package com.easytier.connect.mixin;

import com.easytier.connect.EasyTierConnectMod;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("☁ 陶瓦联机"),
            btn -> EasyTierConnectMod.openTerracotta()
        ).dimensions(this.width - 100, this.height - 60, 90, 20).build());
    }
}