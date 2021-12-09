package dev.ebullient.pockets;

import java.util.Optional;
import java.util.concurrent.Callable;

import javax.transaction.Transactional;

import dev.ebullient.pockets.db.Pocket;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "create",
    mixinStandardHelpOptions = true, requiredOptionMarker = '*', showDefaultValues = true,
    header = "Create a new pocket")
public class PocketsCreate implements Callable<Integer> {
    enum Type {
        backpack, pouch, haversack, bagOfHolding, portableHole
    }

    @Parameters(index = "0", description = "Type of pocket.\n  Choices: ${COMPLETION-CANDIDATES}")
    Type type;

    @Parameters(index = "1", description = "Name for your new pocket. Use quotes for strings with spaces.", arity = "0..1")
    Optional<String> name;

    @Override
    @Transactional
    public Integer call() throws Exception {
        Log.debugf("Parameters: %s, %s", type, name);
        final Pocket pocket = createPocket(type, name);
        pocket.persist();  // <-- Save it!


        Log.outPrintf("✨ Created new pocket '%s' with id '%s'\n", pocket.name, pocket.id);
        return CommandLine.ExitCode.OK;
    }

    Pocket createPocket(Type type, Optional<String> name) {
        switch (type) {
            default:
            case backpack:
                return Pocket.createBackpack(name.orElse("Backpack"));
            case pouch:
                return Pocket.createPouch(name.orElse("Pouch"));
            case haversack:
                return Pocket.createHaversack(name.orElse("Haversack"));
            case bagOfHolding:
                return Pocket.createBagOfHolding(name.orElse("Bag of Holding"));
            case portableHole:
                return Pocket.createPortableHole(name.orElse("Portable Hole"));
        }
    }
}
