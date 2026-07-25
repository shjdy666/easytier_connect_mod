package com.easytier.connect.mixin;

import com.easytier.connect.EasyTierConnectMod;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        int y = this.height / 4 + 168;
        if (y > this.height - 40) y = this.height - 40;
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("☁ 陶瓦联机"),
            btn -> EasyTierConnectMod.openTerracotta()
        ).dimensions(this.width / 2 - 70, y, 140, 20).build());
    }
}