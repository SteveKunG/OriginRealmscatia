package com.stevekung.originrealmscatia.mixin.fixes;

import java.util.UUID;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.social.FilterList;
import net.minecraft.client.gui.social.SocialInteractionsScreen;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.network.play.NetworkPlayerInfo;

@Mixin(FilterList.class)
public class MixinFilterList
{
    @Shadow
    @Final
    private Minecraft field_244651_o;

    @Redirect(method = "func_244759_a(Ljava/util/Collection;D)V", at = @At(value = "INVOKE", target = "net/minecraft/client/network/play/ClientPlayNetHandler.getPlayerInfo(Ljava/util/UUID;)Lnet/minecraft/client/network/play/NetworkPlayerInfo;"))
    private NetworkPlayerInfo filterPlayerUUID(ClientPlayNetHandler handler, UUID uuid)
    {
        NetworkPlayerInfo networkplayerinfo = this.field_244651_o.player.connection.getPlayerInfo(uuid);
        return networkplayerinfo.getGameProfile().getName().startsWith(":") ? null : networkplayerinfo;
    }

    @Inject(method = "func_244657_a(Lnet/minecraft/client/network/play/NetworkPlayerInfo;Lnet/minecraft/client/gui/social/SocialInteractionsScreen$Mode;)V", cancellable = true, at = @At(value = "INVOKE", target = "net/minecraft/client/gui/social/FilterList.addEntry (Lnet/minecraft/client/gui/widget/list/AbstractList$AbstractListEntry;)I", shift = Shift.BEFORE))
    private void filterPlayer(NetworkPlayerInfo playerInfo, SocialInteractionsScreen.Mode mode, CallbackInfo info)
    {
        if (playerInfo.getGameProfile().getName().startsWith(":"))
        {
            info.cancel();
        }
    }
}