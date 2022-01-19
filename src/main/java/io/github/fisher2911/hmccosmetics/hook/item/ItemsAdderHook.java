  package io.github.fisher2911.hmccosmetics.hook.item;

  import dev.lone.itemsadder.api.CustomStack;
  import org.bukkit.inventory.ItemStack;

  public class ItemsAdderHook implements ItemHook {

    public static final String ID = "ITEM_ADDER";
    private static final String IDENTIFIER = "itemsadder";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public ItemStack getItem(final String id) {
        final CustomStack stack = CustomStack.getInstance(id);
        if ( stack == null ) return null;
        return stack.getItemStack();
    }
}
