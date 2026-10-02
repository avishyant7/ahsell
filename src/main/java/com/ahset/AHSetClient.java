package com.ahset;

import com.mojang.brigadier.arguments.LongArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class AHSetClient implements ClientModInitializer {
    private boolean enabled;
    private long price;
    private int cooldown;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            dispatcher.register(ClientCommands.literal("ahset")
                .then(ClientCommands.literal("off").executes(context -> {
                    enabled = false;
                    cooldown = 0;
                    if (context.getSource().getPlayer() != null) {
                        context.getSource().getPlayer().sendSystemMessage(Component.literal("AHSet: OFF"));
                    }
                    return 1;
                }))
                .then(ClientCommands.argument("price", LongArgumentType.longArg(1)).executes(context -> {
                    price = LongArgumentType.getLong(context, "price");
                    enabled = true;
                    cooldown = 0;
                    if (context.getSource().getPlayer() != null) {
                        context.getSource().getPlayer().sendSystemMessage(
                            Component.literal("AHSet: ON, selling main-hand item for $" + price + " each.")
                        );
                    }
                    return 1;
                }))
            );
        });
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(Minecraft client) {
        if (!enabled || client.player == null || client.level == null || client.getConnection() == null) {
            return;
        }
        if (client.gui.screen() != null) {
            return;
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        ItemStack hand = client.player.getMainHandItem();
        if (hand.isEmpty()) {
            enabled = false;
            client.player.sendSystemMessage(Component.literal("AHSet: OFF, main hand is empty."));
            return;
        }
        client.getConnection().sendCommand("ah sell " + price);
        cooldown = 20;
    }
}
