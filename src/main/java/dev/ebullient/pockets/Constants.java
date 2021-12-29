package dev.ebullient.pockets;

public class Constants {

    final static String LIST_DESCRIPTION = "%n"
            + "Pocket and Item identifiers are shown in brackets [id]; Quantities are shown using parenthesis (quantity).%n";

    final static String ADD_DESCRIPTION = "%n"
            + "Add items to a pocket specified by id. Use options to customize quantity, weight, and value.";

    public static final String POCKET_ENTITY = "Pocket";
    public static final String POCKET_TABLE = "pocket";
    public static final String POCKET_ID = "pocket_id";

    public static final String ITEM_ENTITY = "PocketItem";
    public static final String ITEM_TABLE = "pocket_item";
}
