package dev.ebullient.pockets;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

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

    public final static ObjectMapper MAPPER = new ObjectMapper()
            .setVisibility(VisibilityChecker.Std.defaultInstance().with(JsonAutoDetect.Visibility.ANY));

    public final static String JSON_SCHEMA = "https://raw.githubusercontent.com/ebullient/pockets-cli/main/index-schema.json";
    public final static String JSON_EXAMPLE = "    {\n" +
            "      \"items\" : {\n" +
            "        \"bedroll\" : {\n" +
            "          \"name\" : \"Bedroll\",\n" +
            "          \"weight\" : 7.0,\n" +
            "          \"value\" : \"100cp\",\n" +
            "          \"rarity\" : \"none\",\n" +
            "          \"type\" : \"adventuring-gear\"\n" +
            "        },\n" +
            "        \"carpet-of-flying-6-ft-9-ft\" : {\n" +
            "          \"name\" : \"Carpet of Flying, 6 ft. × 9 ft.\",\n" +
            "          \"wondrous\" : true,\n" +
            "          \"tier\" : \"major\",\n" +
            "          \"rarity\" : \"very rare\",\n" +
            "          \"type\" : \"wondrous\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"pockets\" : {\n" +
            "        \"backpack\" : {\n" +
            "          \"name\" : \"Backpack\",\n" +
            "          \"weight\" : 5.0,\n" +
            "          \"value\" : \"200cp\",\n" +
            "          \"rarity\" : \"none\",\n" +
            "          \"type\" : \"adventuring-gear\",\n" +
            "          \"compartments\" : [ {\n" +
            "            \"max_weight\" : 30.0,\n" +
            "            \"max_volume\" : 1.0\n" +
            "          } ]\n" +
            "        }\n" +
            "      }\n" +
            "    }\n";

}
