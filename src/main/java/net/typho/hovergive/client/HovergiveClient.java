package net.typho.hovergive.client;

import com.mojang.serialization.JsonOps;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;

public class HovergiveClient implements ClientModInitializer {
    public static final KeyBinding COPY_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.hovergive", GLFW.GLFW_KEY_Z, "key.categories.inventory"));

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (COPY_KEYBINDING.wasPressed()) {
                if (client.player != null) {
                    ItemStack held = client.player.getMainHandStack();

                    if (held == null) {
                        held = client.player.getOffHandStack();
                    }

                    if (held != null) {
                        StringBuilder command = new StringBuilder("/give @s ");

                        command.append(held.getRegistryEntry().getIdAsString());

                        List<Component<?>> components = new LinkedList<>();

                        ComponentMap comps = held.getComponents();
                        ComponentMap def = held.getDefaultComponents();

                        for (Component<?> next : comps) {
                            if (def.contains(next.type()) && def.get(next.type()) == next.value()) {
                                continue;
                            }

                            components.add(next);
                        }

                        if (!components.isEmpty()) {
                            command.append('[')
                                    .append(String.join(
                                            ", ",
                                            components.stream()
                                                    .map(comp -> comp.type() + "=" + comp.encode(JsonOps.INSTANCE).getOrThrow().toString())
                                                    .toArray(String[]::new)
                                            ))
                                    .append(']');
                        }

                        GLFW.glfwSetClipboardString(client.getWindow().getHandle(), command);
                    }
                }
            }
        });
    }
}
