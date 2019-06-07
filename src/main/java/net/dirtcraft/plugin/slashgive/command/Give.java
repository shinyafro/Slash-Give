package net.dirtcraft.plugin.slashgive.command;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

public class Give implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Player target = args.<Player>getOne("target").get();
        String itemName = args.<String>getOne("item").get();
        Integer quantity = args.<Integer>getOne("quantity").orElse(1);
        Integer meta = args.<Integer>getOne("meta").orElse(0);
        //String nbtData = args.<String>getOne("nbt").orElse(null); TODO - Figure out how to parse a string into NBTTagCompound and add it to an item if the user has the permissions.

        if (src != target && !src.hasPermission("slashgive.others")){
            throw new CommandPermissionException(Text.of("You don't have permission to give items to other players!"));
        }
        if (!Sponge.getRegistry().getType(ItemType.class, itemName).isPresent()){
            throw new CommandException(Text.of("Invalid item"));
        }
        if (quantity > 64 && !src.hasPermission("slashgive.extra")){
            throw new CommandPermissionException(Text.of("You don't have permission to give more then 64."));
        }

        ItemStack item = ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, itemName).get())
                .quantity(quantity)
                .build();

        //We rebuild the item to add the metadata
        item = ItemStack.builder()
                .fromContainer(item.toContainer().set(DataQuery.of("UnsafeDamage"),meta))
                .build();

        target.getInventory().offer(item);
        return CommandResult.success();
    }
}
