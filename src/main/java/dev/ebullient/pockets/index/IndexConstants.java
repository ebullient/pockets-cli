package dev.ebullient.pockets.index;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

public class IndexConstants {

    public final static ObjectMapper FROM_JSON = new ObjectMapper()
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
