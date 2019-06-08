package net.dirtcraft.plugin.slashgive.command;

import com.google.gson.*;

import net.dirtcraft.plugin.slashgive.Lang;
import net.dirtcraft.plugin.slashgive.Permissions;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.*;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Give implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Player target = args.<Player>getOne("target").get();
        String itemName = args.<String>getOne("item").get();
        Integer quantity = args.<Integer>getOne("quantity").orElse(1);
        Integer meta = args.<Integer>getOne("meta").orElse(0);
        String nbtData = args.<String>getOne("nbt").orElse(null);

        if (src != target && !src.hasPermission(Permissions.GIVE_OTHERS)){
            throw new CommandPermissionException(Text.of(Lang.EXCEPTION_PERMISSION_OTHERS));
        }
        if (quantity > 64 && !src.hasPermission(Permissions.GIVE_EXTRA)){
            throw new CommandPermissionException(Text.of(Lang.EXCEPTION_PERMISSION_EXTRA));
        }
        if (nbtData != null && !src.hasPermission(Permissions.GIVE_NBT)) {
            throw new CommandException(Text.of(Lang.EXCEPTION_PERMISSION_NBT));
        }
        if (!Sponge.getRegistry().getType(ItemType.class, itemName).isPresent()){
            throw new CommandException(Text.of(Lang.EXCEPTION_ARGUMENTS_ITEM));
        }

        DataContainer item = DataContainer.createNew();
        item.set(DataQuery.of("ItemType"),itemName);
        item.set(DataQuery.of("Count"), quantity);
        item.set(DataQuery.of("UnsafeDamage"),meta);
        if (nbtData != null){
            JsonElement nbtJson;
            try {
                nbtJson = new JsonParser().parse(nbtData);
                item.set(DataQuery.of("UnsafeData"),parseNbt(nbtJson.getAsJsonObject()));
            } catch (JsonParseException e){
                throw new CommandException(Text.of(Lang.EXCEPTION_ARGUMENTS_NBT));
            }
        }
        target.getInventory().offer(ItemStack.builder().fromContainer(item).build());
        return CommandResult.success();
    }

    private DataContainer parseNbt(JsonObject jsonData){
        DataContainer object = DataContainer.createNew();
        for (Map.Entry<String,JsonElement> entry : jsonData.entrySet()){
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (value.isJsonObject()){
                object.set(DataQuery.of(key),parseNbt(value.getAsJsonObject()));
            } else if (value.isJsonArray()) {
                JsonArray arrayJson =  (JsonArray) value;
                if(arrayJson.size()==0){
                    List<DataContainer> arrayList = new ArrayList<>();
                    object.set(DataQuery.of(key),arrayList);
                } else if (arrayJson.get(0).isJsonObject()){
                    List<DataContainer> arrayList = new ArrayList<>();
                    arrayJson.forEach((obj)->arrayList.add(parseNbt(obj.getAsJsonObject())));
                    object.set(DataQuery.of(key),arrayList);
                } else {
                    //String dataType = arrayJson.get(0).toString().toLowerCase();
                    //Todo numbered arrays
                }
            } else {
                String strippedValue = value.toString().replace("\"", "");
                Pattern pattern = Pattern.compile("^(\\d+)(\\D)?$");
                Matcher matcher = pattern.matcher(strippedValue);
                if (matcher.matches()){
                    String dataType = matcher.group(2)==null?"i":matcher.group(2).toLowerCase();
                    switch (dataType){
                        case "i": object.set(DataQuery.of(key), Integer.parseInt(matcher.group(1))); break;
                        case "b": object.set(DataQuery.of(key), Byte.parseByte(matcher.group(1))); break;
                        case "s": object.set(DataQuery.of(key), Short.parseShort(matcher.group(1))); break;
                        case "l": object.set(DataQuery.of(key), Long.parseLong(matcher.group(1))); break;
                        case "f": object.set(DataQuery.of(key), Float.parseFloat(matcher.group(1))); break;
                        case "d": object.set(DataQuery.of(key), Double.parseDouble(matcher.group(1))); break;
                    }
                }else {
                    object.set(DataQuery.of(key), strippedValue);
                }

            }
        }
        return object;
    }
}
