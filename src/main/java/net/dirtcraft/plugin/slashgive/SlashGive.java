package net.dirtcraft.plugin.slashgive;

import net.dirtcraft.plugin.slashgive.command.Give;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

@Plugin(
        id = "slash-give",
        name = "Slash Give",
        description = "Control giving of items to players.",
        url = "https://dirtcraft.net/",
        authors = {
                "juliann",
                "ShinyAfro"
        }
)
public class SlashGive {

    @Listener (order = Order.FIRST)
    public void onConstructionEvent(GameConstructionEvent event) {
        CommandSpec give = CommandSpec.builder()
                .permission(Permissions.GIVE_MAIN)
                .arguments(
                        GenericArguments.playerOrSource(Text.of("target")),
                        GenericArguments.string(Text.of("item")),
                        GenericArguments.optional(GenericArguments.integer(Text.of("quantity"))),
                        GenericArguments.optional(GenericArguments.integer(Text.of("meta"))),
                        GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("nbt")))
                )
                .executor(new Give())
                .build();

        Sponge.getCommandManager().register(this, give, "give", "i", "minecraft:give", "randomtweaks:give");
    }
}
