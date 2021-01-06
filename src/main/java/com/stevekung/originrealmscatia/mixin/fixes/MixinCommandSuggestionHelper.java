package com.stevekung.originrealmscatia.mixin.fixes;

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.stevekung.originrealmscatia.utils.Utils;

import net.minecraft.client.gui.CommandSuggestionHelper;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@Mixin(CommandSuggestionHelper.class)
public class MixinCommandSuggestionHelper
{
    @Redirect(method = "init()V", at = @At(value = "INVOKE", target = "net/minecraft/client/multiplayer/ClientSuggestionProvider.getPlayerNames()Ljava/util/Collection;"))
    private Collection<String> getPlayerNames(ClientSuggestionProvider provider)
    {
        return Utils.INSTANCE.filteredPlayers(provider.getPlayerNames());
    }
}