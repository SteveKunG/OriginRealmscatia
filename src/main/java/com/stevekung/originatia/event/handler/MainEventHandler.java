package com.stevekung.originatia.event.handler;

import java.util.Collections;
import java.util.Locale;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;
import com.stevekung.originatia.gui.screen.widget.ItemButton;
import com.stevekung.originatia.utils.ItemUtils;
import com.stevekung.originatia.utils.Utils;
import com.stevekung.stevekungslib.utils.GameProfileUtils;
import com.stevekung.stevekungslib.utils.TextComponentUtils;
import com.stevekung.stevekungslib.utils.client.ClientUtils;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class MainEventHandler
{
    private final Minecraft mc;

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
                this.addButtonsToInventory(event, width, height);
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
            boolean hover = event.getMouseX() >= button.x && event.getMouseY() >= button.y && event.getMouseX() < button.x + button.getWidth() && event.getMouseY() < button.y + button.getHeightRealms();

            if (hover && button.visible)
            {
                GuiUtils.drawHoveringText(event.getMatrixStack(), Collections.singletonList(button.getName()), event.getMouseX(), event.getMouseY(), gui.width, gui.height, -1, this.mc.fontRenderer);
                RenderSystem.disableLighting();
                break;
            }
        }
    }

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event)
    {
        ITextComponent component = event.getMessage();

        if (event.getType() == ChatType.SYSTEM)//TODO Config
        {
            String message = TextFormatting.getTextWithoutFormattingCodes(component.getString());

            if (false && message.toLowerCase(Locale.ROOT).contains("gg"))
            {
                event.setCanceled(true);
            }
            //System.out.println(StringEscapeUtils.escapeJava(TextFormatting.getTextWithoutFormattingCodes(component.getString())));

            if (message.startsWith("ꑠ§x"))
            {
                this.mc.world.playSound(this.mc.player, this.mc.player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    private void addButtonsToInventory(GuiScreenEvent.InitGuiEvent.Post event, int width, int height)
    {
        event.addWidget(new ItemButton(width + 44, height + 86, ItemUtils.makeSimpleItem(4017, TextComponentUtils.component("Auction House")), button -> this.mc.player.sendChatMessage("/auctionhouse")));
    }
}