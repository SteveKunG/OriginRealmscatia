package com.stevekung.originatia.event.handler;

import java.util.Collections;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;
import com.stevekung.originatia.gui.screen.WarpSelectionScreen;
import com.stevekung.originatia.gui.screen.widget.ItemButton;
import com.stevekung.originatia.keybinding.KeyBindingHandler;
import com.stevekung.originatia.utils.ItemUtilsOR;
import com.stevekung.originatia.utils.Utils;
import com.stevekung.stevekungslib.utils.GameProfileUtils;
import com.stevekung.stevekungslib.utils.TextComponentUtils;
import com.stevekung.stevekungslib.utils.client.ClientUtils;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.util.InputMappings;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class MainEventHandler
{
    private final Minecraft mc;

    public static boolean playStoneSound;
    public static ItemButton itemButton;

    public MainEventHandler()
    {
        this.mc = Minecraft.getInstance();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (GameProfileUtils.isSteveKunG() && ClientUtils.isKeyDown(GLFW.GLFW_KEY_F7))
        {
            if (this.mc.objectMouseOver != null)
            {
                if (this.mc.objectMouseOver instanceof BlockRayTraceResult)
                {
                    BlockRayTraceResult result = (BlockRayTraceResult)this.mc.objectMouseOver;
                    BlockState state = this.mc.world.getBlockState(result.getPos());
                    StringBuilder stringbuilder = new StringBuilder(BlockStateParser.toString(state));
                    System.out.println(stringbuilder);
                    this.mc.keyboardListener.setClipboardString(stringbuilder.toString());
                }
                if (this.mc.objectMouseOver instanceof EntityRayTraceResult)
                {
                    EntityRayTraceResult result = (EntityRayTraceResult)this.mc.objectMouseOver;
                    Entity entity = result.getEntity();

                    if (entity instanceof ItemFrameEntity)
                    {
                        ItemFrameEntity frame = (ItemFrameEntity)entity;
                        this.mc.keyboardListener.setClipboardString("/give @s " + frame.getDisplayedItem().getItem().getRegistryName() + frame.getDisplayedItem().getTag());
                    }
                }
            }

            if (this.mc.currentScreen != null && this.mc.currentScreen instanceof ContainerScreen)
            {
                ContainerScreen container = (ContainerScreen)this.mc.currentScreen;
                Slot slot = container.getSlotUnderMouse();

                if (slot != null && slot.getHasStack())
                {
                    ItemStack itemStack = slot.getStack();
                    this.mc.keyboardListener.setClipboardString("/give @s " + itemStack.getItem().getRegistryName() + itemStack.getTag());
                }
            }
        }
    }

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event)
    {
        Screen gui = event.getGui();
        int width = gui.width / 2;
        int height = gui.height / 2 - 106;

        if (Utils.INSTANCE.isOriginRealms())
        {
            if (gui instanceof InventoryScreen)
            {
                InventoryScreen invScreen = (InventoryScreen)gui;
                this.addButtonsToInventory(event, width, height, invScreen.getRecipeGui().isVisible());
            }
        }
    }

    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public void onPostGuiDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event)
    {
        Screen gui = event.getGui();

        for (ItemButton button : gui.buttons.stream().filter(button -> button != null && button instanceof ItemButton).map(button -> (ItemButton)button).collect(Collectors.toList()))
        {
            boolean hover = event.getMouseX() >= button.x && event.getMouseY() >= button.y && event.getMouseX() < button.x + button.getWidth() && event.getMouseY() < button.y + button.getHeight();

            if (hover && button.visible)
            {
                GuiUtils.drawHoveringText(event.getMatrixStack(), Collections.singletonList(button.getName()), event.getMouseX(), event.getMouseY(), gui.width, gui.height, -1, this.mc.fontRenderer);
                RenderSystem.disableLighting();
                break;
            }
        }
    }

    @SubscribeEvent
    public void onPressKey(InputEvent.KeyInputEvent event)
    {
        if (InputMappings.isKeyDown(this.mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) && KeyBindingHandler.KEY_QUICK_NAVIGATOR.isKeyDown())
        {
            this.mc.displayGuiScreen(new WarpSelectionScreen());
        }
        else
        {
            if (KeyBindingHandler.KEY_QUICK_NAVIGATOR.isKeyDown())
            {
                this.mc.player.sendChatMessage("/navigator");
            }
        }
    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event)
    {
        if (MainEventHandler.playStoneSound && event.getName().equals("block.stone.place"))
        {
            event.setResultSound(null);
            MainEventHandler.playStoneSound = false;
        }
    }

    @SubscribeEvent
    public void onClientLoggedIn(ClientPlayerNetworkEvent.LoggedInEvent event)
    {
        if (Utils.INSTANCE.isOriginRealms())
        {
            RenderTypeLookup.setRenderLayer(Blocks.TRIPWIRE, RenderType.getCutout());
        }
    }

    @SubscribeEvent
    public void onClientLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event)
    {
        if (Utils.INSTANCE.isOriginRealms())
        {
            RenderTypeLookup.setRenderLayer(Blocks.TRIPWIRE, RenderType.getTripwire());
        }
    }

    private void addButtonsToInventory(GuiScreenEvent.InitGuiEvent.Post event, int width, int height, boolean recipeBook)
    {
        event.addWidget(itemButton = new ItemButton(width + (recipeBook ? 120 : 44), height + 84, ItemUtilsOR.makeSimpleItem(4017, TextComponentUtils.component("Auction House")), button -> this.mc.player.sendChatMessage("/auctionhouse")));
    }
}