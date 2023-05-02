<script lang="ts">
  import type { Currency } from "../@types/pockets";
  import { activePresetData, activeProfileData, activeProfileKey, addCurrency, additionalCurrencies, deleteCurrency } from "./stores";
  import { writable } from "svelte/store";
  import Modal, { bind } from 'svelte-simple-modal';
  import Button from "./ButtonTooltip.svelte";
  import EditCurrency from "./EditCurrency.svelte";
  import AddSvg from "../svg/plus.svelte";
  import PencilSvg from "../svg/pencil.svelte";
  import TrashSvg from "../svg/trash-2.svelte";

  const currencyModal = writable(null);
  const showCurrencyModal = (coin: Currency, i: number) => {
    currencyModal.set(bind(EditCurrency, {initCurrency: coin, index: i, modal: currencyModal}));
  }
  const closeCurrencyModal = () => {
    currencyModal.set(null);
    document.body.style.removeProperty('position');
    document.body.style.removeProperty('top');
    document.body.style.removeProperty('overflow');
    document.body.style.removeProperty('width');
  }
  function sortedCurrency(list: Currency[]): Currency[] {
    return list.sort((a, b) => b.unitConversion - a.unitConversion);
  }
</script>

<h3 id="currency">Currency</h3>

{#if $activeProfileData.preset}
<div class="buttons">
  <p class="button-row">This profile defines the currency shown in the following table.</p>
  <Modal id="add-currency-modal" show={$currencyModal} on:closing={closeCurrencyModal}>
    <Button buttonClass="buttons only" name="currency-add" tip="Add custom currency" left
        clickfn={() => showCurrencyModal(null, -1)}><AddSvg /></Button>
  </Modal>
</div>
<table>
  <thead>
    <tr><th>Currency Name</th><th>Notation</th><th>Value</th><th class="action">&nbsp;</th></tr>
    <tr class="secondary"><th colspan="4">Preset currency</th></tr>
  </thead>
  {#if $activePresetData.currency.length > 0}
  <tbody>
    {#each sortedCurrency($activePresetData.currency) as coin, i}
    <tr>
      <td>{coin.name}</td>
      <td>{coin.notation}</td>
      <td>{coin.unitConversion}</td>
      <td class="action">&nbsp;</td>
    </tr>
    {/each}
  </tbody>
  {/if}
  {#if $activeProfileData.currency.length > 0}
  <thead>
    <tr class="secondary"><th colspan="4">Additional currency</th></tr>
  </thead>
  <tbody>
    {#each sortedCurrency($activeProfileData.currency) as coin, i}
    <tr>
      <td>{coin.name}</td>
      <td>{coin.notation}</td>
      <td>{coin.unitConversion}</td>
      <td class="action">
        <div>
          <Modal id="edit-currency-modal" show={$currencyModal} on:closing={closeCurrencyModal}>
            <Button buttonClass="buttons" name="coin-edit" tip="Edit currency" left
                clickfn={() => showCurrencyModal(coin, i)}><PencilSvg /></Button>
          </Modal>
          <Button buttonClass="buttons" name="coin-delete" tip="Delete currency" left
            clickfn={() => deleteCurrency(coin, $activeProfileKey)}><TrashSvg /></Button>
        </div>
      </td>
    </tr>
    {/each}
  </tbody>
  {/if}
</table>
{:else}
<p>Select and apply a preset to assign a currency to this profile.</p>
{/if}

{#if $additionalCurrencies && Object.keys($additionalCurrencies).length > 0 }
<p>Additional currencies are available with this preset. Click to add them:</p>
<ul>
  {#each Object.entries($additionalCurrencies) as [key, currency]}
  <li><span class="title">{key}</span>:
    {#each currency as coin, i (coin.name)}
    <Button name="add-{i}" tip="Add to currency list" left
        clickfn={() => addCurrency(coin, $activeProfileKey)}>{coin.name}</Button>{#if i < currency.length - 1}, {/if}
    {/each}
  </li>
  {/each}
</ul>
{/if}
