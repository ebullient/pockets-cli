package dev.ebullient.pockets.commands;

import static dev.ebullient.pockets.io.PocketTui.Tui;
import static dev.ebullient.pockets.io.PocketsFormat.ITEM_PREFIX;
import static dev.ebullient.pockets.io.PocketsFormat.POCKET_PREFIX;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.apache.camel.Produce;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.actions.ModificationRequest;
import dev.ebullient.pockets.actions.ModificationResponse;
import dev.ebullient.pockets.actions.ProfileContext;
import dev.ebullient.pockets.commands.mixin.ActiveProfileMixinOption;
import dev.ebullient.pockets.config.PresetValues;
import dev.ebullient.pockets.config.Presets;
import dev.ebullient.pockets.config.ProfileConfigData;
import dev.ebullient.pockets.config.Types.PresetFlavor;
import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.Profile;
import dev.ebullient.pockets.io.InvalidPocketState;
import dev.ebullient.pockets.io.PocketTui;
import dev.ebullient.pockets.io.PocketsFormat;
import dev.ebullient.pockets.io.Templater;
import io.quarkus.qute.TemplateInstance;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

public abstract class BaseCommand implements Callable<Integer> {
    public interface CliModifyPocket {
        ModificationResponse modifyPockets(ModificationRequest request);
    }

    @Produce("direct:modifyPockets")
    CliModifyPocket modifyPocketRoute;

    @Inject
    ProfileContext profileContext;

    @Mixin
    ActiveProfileMixinOption activeProfileMixin;

    @Inject
    Templater quteTemplates;

    @Option(order = 2, names = { "-T", "--types" }, description = "List pocket and item types and exit.", help = true)
    boolean showTypes;

    @ArgGroup(heading = "%nDate, Time, Description%n", exclusive = false)
    protected ChangeDescriptionOptions cdOptions = new ChangeDescriptionOptions();

    @Override
    @ActivateRequestContext
    @Transactional
    public final Integer call() {
        try {
            Tui.debugf("BaseCommand call: pre-active profile");
            Optional<String> activeProfile = activeProfileMixin.findActiveProfileId();
            if (activeProfile.isEmpty()) {
                return ExitCode.USAGE;
            }
            profileContext.setActiveProfile(activeProfile.get());
            if (showTypes) {
                showTypes();
                return ExitCode.OK;
            }

            Tui.debugf("BaseCommand launching command with active profile set");
            return delegateCall();
        } catch (InvalidPocketState ips) {
            Tui.error(ips);
            return ips.getExitCode();
        } catch (ParameterException | IllegalArgumentException ex) {
            Tui.errorf(ex, "An error occurred", ex.getMessage());
            return ExitCode.USAGE;
        } catch (Exception e) {
            Tui.errorf(e, "An error occurred", e.getMessage());
            return ExitCode.SOFTWARE;
        }
    }

    public abstract Integer delegateCall() throws Exception;

    void showTypes() {
        Profile p = profileContext.getActiveProfile();
        PresetValues presets = Presets.getPresets(p.config.preset);

        Tui.println(PocketsFormat.table(String.format(POCKET_PREFIX + "Preset Pocket Types (%s)", presets.getName()),
                List.of("pocket-type", "Pocket Type"),
                presets.getPocketRef().values().stream()
                        .map(x -> List.of(x.id, x.name))
                        .sorted(Comparator.comparing(a -> a.get(0)))
                        .collect(Collectors.toList())));

        Tui.println(PocketsFormat.table(String.format(ITEM_PREFIX + "Preset Item Types (%s)", presets.getName()),
                List.of("item-type", "Item Type"),
                presets.getItemRef().values().stream()
                        .filter(x -> !p.config.hasPocketRef(x.id))
                        .map(x -> List.of(x.id, x.name))
                        .sorted(Comparator.comparing(a -> a.get(0)))
                        .collect(Collectors.toList())));
    }

    ModificationRequest createModificationRequest(String defaultMemo) {
        ModificationRequest req = new ModificationRequest();
        req.setMemo(cdOptions.memo == null ? defaultMemo : cdOptions.memo);
        if (cdOptions.dateTime != null) {
            req.setDatetime(cdOptions.dateTime);
        }
        return req;
    }

    void showPocket(Pocket pocket) {
        Profile profile = profileContext.getActiveProfile();
        ProfileConfigData pcd = profile.config;

        if (Tui.isVerboseEnabled()) {
            TemplateInstance tpl = quteTemplates.getTemplateInstance("stream:templates/cli-pocket.txt");
            tpl.data("useBulk", pcd.preset == PresetFlavor.pf2e);
            tpl.data("p", pocket);
            Tui.println(tpl.render());
        } else {
            Tui.println(pocket.emoji + " " + pocket.asMemo(pcd));
        }
    }

    void showPocketItems(Pocket pocket) {
        if (pocket.pocketItems.isEmpty()) {
            return;
        }
        Profile profile = profileContext.getActiveProfile();
        ProfileConfigData pcd = profile.config;

        if (Tui.isVerboseEnabled()) {
            TemplateInstance tpl = quteTemplates.getTemplateInstance("stream:templates/cli-pocket-items.txt");
            tpl.data("useBulk", pcd.preset == PresetFlavor.pf2e);
            tpl.data("p", pocket);
            Tui.println(tpl.render());
        } else {
            Tui.println(pocket.emoji + " " + PocketsFormat.nameNumberId(pocket) + " contains "
                    + PocketsFormat.pocketItemList(pocket));
        }
    }

    void showItem(Item item) {
        Profile profile = profileContext.getActiveProfile();
        ProfileConfigData pcd = profile.config;

        if (Tui.isVerboseEnabled()) {
            TemplateInstance tpl = quteTemplates.getTemplateInstance("stream:templates/cli-item.txt");
            tpl.data("useBulk", pcd.preset == PresetFlavor.pf2e);
            tpl.data("i", item);
            Tui.println(tpl.render());
        } else {
            Tui.println("🏷️ " + item.asMemo(pcd));
        }
    }

    public Pocket selectPocketByLongNameOrId(Profile profile, String nameOrId) {
        final Pocket pocket;
        Optional<Long> longId = Transform.toLong(nameOrId);
        if (longId.isPresent()) {
            pocket = selectPocketById(longId.get());
        } else {
            pocket = selectPocketByName(profile, nameOrId);
        }
        return pocket;
    }

    public Pocket selectPocketById(Long longId) {
        Pocket pocket = Pocket.findById(longId);
        if (pocket == null) {
            Tui.printlnf("%nThe specified value [%s] doesn't match any known pockets.%n", longId);
            PocketsFormat.showPockets(profileContext.getActiveProfile().allPockets());
            throw new InvalidPocketState(PocketTui.NOT_FOUND);
        }
        return pocket;
    }

    public Pocket selectPocketByName(Profile profile, String nameOrId) {
        Pocket pocket = profile.getPocket(nameOrId);
        if (pocket != null) {
            return pocket;
        }
        Optional<Pocket> findIt = Pocket.findByNaturalIdOptional(profile.id, nameOrId);
        if (findIt.isEmpty()) {
            findIt = Pocket.findByNaturalIdOptional(profile.id, Transform.slugify(nameOrId));
            if (findIt.isEmpty()) {
                List<Pocket> search = Pocket.listByNaturalIdLike(profile.id, Transform.slugify(nameOrId));
                if (search.size() == 1) {
                    return search.get(0);
                }
                Tui.printlnf("%nThe specified value [%s] doesn't match any known pockets.%n", nameOrId);
                PocketsFormat.showPockets(profile.allPockets());
                throw new InvalidPocketState(PocketTui.NOT_FOUND);
            }
        }
        return findIt.get();
    }

    public Item selectItemByLongNameOrId(Profile profile, String nameOrId) {
        final Item item;
        Optional<Long> longId = Transform.toLong(nameOrId);
        if (longId.isPresent()) {
            item = selectItemById(longId.get());
        } else {
            item = selectItemByName(profile, nameOrId);
        }
        return item;
    }

    public Item selectItemById(Long longId) {
        Item item = Item.findById(longId);
        if (item == null) {
            Tui.printlnf("%nThe specified value [%s] doesn't match any known items.%n", longId);
            PocketsFormat.showItems(profileContext.getActiveProfile().allItems());
            throw new InvalidPocketState(PocketTui.NOT_FOUND);
        }
        return item;
    }

    public Item selectItemByName(Profile profile, String nameOrId) {
        Item item = profile.getItem(nameOrId);
        if (item != null) {
            return item;
        }
        Optional<Item> findIt = Item.findByNaturalIdOptional(profile.id, nameOrId);
        if (findIt.isEmpty()) {
            findIt = Item.findByNaturalIdOptional(profile.id, Transform.slugify(nameOrId));
            if (findIt.isEmpty()) {
                List<Item> search = Item.listByNaturalIdLike(profile.id, Transform.slugify(nameOrId));
                if (search.size() == 1) {
                    return search.get(0);
                }
                Tui.printlnf("%nThe specified value [%s] doesn't match any known items.%n", nameOrId);
                PocketsFormat.showItems(profile.allItems());
                throw new InvalidPocketState(PocketTui.NOT_FOUND);
            }
        }
        return findIt.get();
    }

    static class ChangeDescriptionOptions {
        @Option(order = 3, names = { "-d", "--datetime" },
                description = "When did this change happen?%n  Session number, real date, in-game date")
        String dateTime = null;

        @Option(order = 4, names = { "-m", "--memo" }, description = "Memo / description of changes")
        String memo = null;
    }
}
