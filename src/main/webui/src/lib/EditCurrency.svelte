<script lang="ts">
  import type { Currency } from "../@types/pockets";
  import { activeProfileData, activeProfileKey, addCurrency, updateCurrency } from "./stores";
  import { derived, writable, type Writable } from 'svelte/store';
  import Button from "./ButtonTooltip.svelte";
  import Check from "../svg/check.svelte";
  import type { SvelteComponent } from "svelte";

  export let initCurrency: Currency;
  export let index: number;
  export let modal: Writable<SvelteComponent>;

  const coin = writable(initCurrency ? initCurrency : {
    name: "",
    notation: "",
    unitConversion: 1
  });
  const addCurrencyDisabled = derived(([coin, activeProfileData]), ([$c, $apd]) => {
    console.log(index, $c, $apd.currency.find((other, i) => other.notation == $coin.notation && i != index))
    return !$c.notation || !$c.unitConversion || $apd.currency.find((other, i) => other.notation == $coin.notation && i != index) != undefined;
  })

  async function submitCurrencyForm(coin) {
    console.log("submit form", initCurrency, coin, index);
    if (index >= 0) {
      updateCurrency(index, coin, $activeProfileKey);
    } else {
      addCurrency(coin, $activeProfileKey);
    }
    modal.set(null);
  }
</script>

<form>
  <h2>Add pocket</h2>

  <div class="buttons row">
    <label for="description">Name</label>
    <input type="text" name="name" class="only" required bind:value={$coin.name} placeholder="Copper (cp)"/>
  </div>

  <div class="buttons">
    <label for="notation">Notation</label>
    <input type="text" name="notation" class="only" required
        class:required="{$addCurrencyDisabled}"
        bind:value={$coin.notation} placeholder="cp" />
  </div>
  <p>{$addCurrencyDisabled ? "❗️ Specify an abbreviation that appears in value strings: cp, gp, etc. Make sure it does not conflict with other definitions." : "Abbreviation that appears in value strings: cp, gp, etc."}</p>

  <div class="buttons">
    <label for="unitConversion">Unit Conversion</label>
    <input type="number" name="unitConversion" required class="only"
        class:required="{$addCurrencyDisabled}"
        bind:value={$coin.unitConversion} placeholder="1" min=1 />
  </div>
  <p>{$addCurrencyDisabled ? "❗️ Specify the conversion rate for this currency compared to the smallest denomination." : "Conversion rate of this coin to the smallest denomination."} For example: a copper piece has a unit conversion of 1 (smallest),
      a silver piece has a unit conversion of 10 (1 silver piece is worth 10 copper pieces).</p>

  <div class="buttons">
    <span></span>
    <Button name="add-currency" tip="All done! Add currency to list" buttonClass="buttons only ok"
      clickfn={() => submitCurrencyForm($coin)} disabled="{$addCurrencyDisabled}"
      left><Check /> <span class="text">OK</span></Button>
  </div>
</form>


