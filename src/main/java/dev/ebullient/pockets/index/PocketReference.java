package dev.ebullient.pockets.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;

import dev.ebullient.pockets.db.Mapper;
import dev.ebullient.pockets.db.Pocket;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class PocketReference extends ItemReference {
    String emoji;
    boolean extradimensional;
    List<Compartment> compartments = new ArrayList<>();

    public PocketReference() {
    }

    public String getEmoji() {
        return this.emoji;
    }

    public String setEmoji(String emoji) {
        this.emoji = emoji;
        return this.emoji;
    }

    public Pocket createPocket(Optional<String> pocketName) {
        Pocket pocket = new Pocket();
        pocket.name = pocketName.orElse(this.name);
        pocket.slug = Mapper.slugify(pocket.name);
        pocket.pocketRef = this.idSlug;
        pocket.weight = this.weight;
        pocket.extradimensional = this.extradimensional;

        if (compartments.size() == 1) {
            Compartment c = compartments.get(0);
            pocket.max_weight = c.max_weight;
            pocket.max_volume = c.max_volume;
        } else {
            for (Compartment c : compartments) {
                if (c.max_volume != null) {
                    pocket.max_volume = pocket.max_volume == null ? c.max_volume : Double.sum(pocket.max_volume, c.max_volume);
                }
                if (c.max_weight != null) {
                    pocket.max_weight = pocket.max_weight == null ? c.max_weight : Double.sum(pocket.max_weight, c.max_weight);
                }
            }
        }
        return pocket;
    }

    public boolean hasConstraints() {
        return compartments.stream().anyMatch(c -> c.constraint != null);
    }

    public String constraint(@NotNull String slug) {
        if (compartments.size() == 1) {
            return "This may contain " + compartments.get(0).constraint;
        }

        StringBuilder result = new StringBuilder();
        result.append("There are ").append(compartments.size()).append(" pockets which may contain:\n");
        for (int i = 0; i < compartments.size(); i++) {
            String constraint = compartments.get(i).constraint;
            if (constraint != null && !constraint.isBlank()) {
                result.append("  ").append(i + 1).append(": ").append(constraint).append("\n");
            }
        }
        return result.toString();
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    static class Compartment {
        Double max_weight;
        Double max_volume;
        String constraint;

        public Compartment() {
        }

        public Compartment(Double max_weight, Double max_volume, String constraint) {
            this.max_volume = max_volume;
            this.max_weight = max_weight;
            this.constraint = constraint;
        }
    }

    public static String emojiForSlug(String idSlug) {
        switch (idSlug) {
            case "backpack":
                return "🎒";
            case "bag-of-holding":
                return "🧳";
            case "basket":
                return "🧺";
            case "chest":
                return "🧰";
            case "portable-hole":
                return "⚫️";
            case "pouch":
                return "👛";
            case "sack":
                return "🧸";
            case "crossbow-bolt-case":
                return "🏹";
            default:
                if (idSlug.endsWith("quiver")) {
                    return "🏹";
                }
                if (idSlug.contains("haversack")) {
                    return "👝";
                }
                if (idSlug.endsWith("-case")) {
                    return "🗞";
                }
                return "🥡";
        }
    }
}
