package com.stevekung.originatia.event.handler;

import java.util.List;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.stevekung.originatia.gui.screen.widget.ItemButton;
import com.stevekung.originatia.mixin.AccessorScreen;
import com.stevekung.originatia.utils.ItemUtilsOR;
import com.stevekung.originatia.utils.Utils;
import com.stevekung.stevekungslib.utils.GameProfileUtils;
import com.stevekung.stevekungslib.utils.TextComponentUtils;
import com.stevekung.stevekungslib.utils.client.ClientUtils;
import me.shedaniel.architectury.event.events.GuiEvent;
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class MainEventHandler
{
    public static boolean playStoneSound;
    public static ItemButton itemButton;

    public MainEventHandler()
    {
        GuiEvent.INIT_POST.register(this::onInitGui);
        ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
        GuiEvent.RENDER_POST.register(this::onPostGuiDrawScreen);
    }

    public void onClientTick(Minecraft mc)
    {
        if (GameProfileUtils.isSteveKunG() && ClientUtils.isKeyDown(GLFW.GLFW_KEY_F7))
        {
            if (mc.hitResult != null)
            {
                if (mc.hitResult instanceof BlockHitResult)
                {
                    BlockHitResult result = (BlockHitResult)mc.hitResult;
                    BlockState state = mc.level.getBlockState(result.getBlockPos());
                    StringBuilder stringbuilder = new StringBuilder(BlockStateParser.serialize(state));
                    System.out.println(stringbuilder);
                    mc.keyboardHandler.setClipboard(stringbuilder.toString());
                }
            }
        }
    }

    public void onInitGui(Screen screen, List<AbstractWidget> widgets, List<GuiEventListener> children)
    {
        int width = screen.width / 2;
        int height = screen.height / 2 - 106;

        if (Utils.INSTANCE.isOriginRealms())
        {
            if (screen instanceof InventoryScreen)
            {
                InventoryScreen invScreen = (InventoryScreen)screen;
                this.addButtonsToInventory(widgets, width, height, invScreen.getRecipeBookComponent().isVisible());
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void onPostGuiDrawScreen(Screen screen, PoseStack poseStack, int mouseX, int mouseY, float delta)
    {
        for (ItemButton button : ((AccessorScreen)screen).getButtons().stream().filter(button -> button instanceof ItemButton).map(button -> (ItemButton)button).collect(Collectors.toList()))
        {
            boolean hover = mouseX >= button.x && mouseY >= button.y && mouseX < button.x + button.getWidth() && mouseY < button.y + button.getHeight();

            if (hover && button.visible)
            {
                screen.renderTooltip(poseStack, button.getName(), mouseX, mouseY);
                RenderSystem.disableLighting();
                break;
            }
        }
    }

    private void addButtonsToInventory(List<AbstractWidget> widgets, int width, int height, boolean recipeBook)
    {
        widgets.add(itemButton = new ItemButton(width + (recipeBook ? 120 : 44), height + 84, ItemUtilsOR.makeSimpleItem(4017, TextComponentUtils.component("Auction House")), button -> Minecraft.getInstance().player.chat("/auctionhouse")));
    }
}