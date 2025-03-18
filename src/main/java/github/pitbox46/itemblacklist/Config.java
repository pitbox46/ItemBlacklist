package github.pitbox46.itemblacklist;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;

public class Config {
    private static final Builder b = new Builder();

    public static DoubleValue BAN_RATE = b.push("banProperties")
            .comment("The chance that an item is checked. Lower values will give better performance. \n " +
                    "Does not apply to crafting and merchant trades")
            .defineInRange("banRate", 1.0, 0.0, 1.0);
    public static BooleanValue BAN_CONTAINER = b
            .comment("Should ban items from containers")
            .define("banContainerChanged", true);
    public static BooleanValue BAN_MERCHANT_TRADES = b
            .comment("Should take away merchant trades")
            .define("banMerchantTrades", true);
    public static BooleanValue BAN_CRAFTING = b
            .comment("Should ban crafting recipes that create the item")
            .define("banCraftingRecipes", true);
    public static BooleanValue BAN_ITEM_ENTITY = b
            .comment("Should ban the item when it's dropped")
            .define("banItemEntity", true);

    public static BooleanValue SHOW_MESSAGES = b.pop().push("miscProperties")
            .comment("Should non-ops be able to see the banned items")
            .define("showMessages", true);

    public static final ForgeConfigSpec SERVER = b.pop().build();

    public static boolean testBanRate() {
        if (BAN_RATE.get() >= 1.0) {
            return true;
        }
        return Math.random() < BAN_RATE.get();
    }
}
