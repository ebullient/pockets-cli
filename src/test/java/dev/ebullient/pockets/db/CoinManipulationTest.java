package dev.ebullient.pockets.db;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.db.Currency.CoinPurse;
import dev.ebullient.pockets.index.Index;
import dev.ebullient.pockets.io.PocketTui;

public class CoinManipulationTest {

    static PocketTui tui = new PocketTui();
    static Index index = new Index(tui);

    static Pocket pocket = new Pocket();

    CoinPurse purse;

    @BeforeAll
    static public void init() {
        index.init();
    }

    @BeforeEach
    public void reset() {
        pocket.items.clear();
        purse = new CoinPurse(pocket, tui, index);
        assertThat(purse.totalCpValue()).isEqualTo(0);
    }

    @Test
    public void testInsufficientFunds() {
        assertThat(purse.deduct(1)).isFalse();
    }

    @Test
    public void testDeductCp() {
        purse.cp.add(1);
        assertThat(purse.deduct(1)).isTrue();
        assertThat(purse.cp.cpValue()).isEqualTo(0);
    }

    @Test
    public void testAddRemove() {
        purse.add(Currency.pp, 1);
        purse.add(Currency.gp, 1);
        purse.add(Currency.ep, 1);
        purse.add(Currency.sp, 1);
        purse.add(Currency.cp, 1);

        int oneOfEach = (int) (Currency.pp.cpEx + Currency.gp.cpEx + Currency.ep.cpEx + Currency.sp.cpEx + Currency.cp.cpEx);
        assertThat(purse.totalCpValue()).isEqualTo(oneOfEach);

        purse.add(Currency.cp, 1); // add one more
        assertThat(purse.deduct(oneOfEach)).isTrue();
        assertThat(purse.totalCpValue()).isEqualTo(1);
    }
}
