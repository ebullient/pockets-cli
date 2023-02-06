<script lang="ts">
  import type { PocketRef } from "../@types/pockets";
  import { derived, type Writable } from "svelte/store";
  import { writable } from 'svelte/store';
  import { activePresetData, activeProfileData, activeProfileKey, addPocket, updatePocket } from "./stores";
  import Button from "./ButtonTooltip.svelte";
  import AddSvg from "../svg/plus.svelte";
  import TrashSvg from "../svg/trash-2.svelte";
  import Check from "../svg/check.svelte";

  export let modal;
  export let index;
  export let initPocket: PocketRef;

  const pocket: Writable<PocketRef> = writable(initPocket ? initPocket : {
    name: "",
    compartments: [{}]
  });
  const addPocketDisabled = derived(([pocket, activeProfileData]), ([$p, $apd]) => {
    console.log(index, $p.name, $apd.pockets.find((other, i) => other.name == $pocket.name && i != index))
    return !$p.name || $apd.pockets.find((other, i) => other.name == $pocket.name && i != index) != undefined;
  })

  async function submitForm() {
    console.log("submit form", initPocket, $pocket, index);
    if (index >= 0) {
      updatePocket(index, $pocket, $activeProfileKey);
    } else {
      addPocket($pocket, $activeProfileKey);
    }
    modal.set(null);
  }
</script>

<form id="edit-pocket">
  <h2>Add pocket</h2>

  <div class="buttons">
    <label for="description">Name</label>
    <input type="text" name="name" class="only" required
        placeholder="Box of things (required)"
        bind:value={$pocket.name} class:required="{$addPocketDisabled}"/>
  </div>
  <p>{$addPocketDisabled ? "❗️ Specify a name for your pocket. Make sure it does not conflict with existing pockets." : "Generic name for this pocket."}</p>

  <div class="buttons">
    <label for="emoji">Emoji</label>
    <input type="text" name="emoji" class="only" size="1" placeholder="📦 (optional)"
        bind:value={$pocket.emoji} />
  </div>
  <p>Single character or emoji, like 🗃️ or 🗳️, shown near the pocket in listings.</p>

  {#if $activePresetData.capacityType == "weight"}
  <div class="buttons">
    <label for="weight">Weight (lbs)</label>
    <input class="only" name="weight" type="number" step="0.01" min=0 bind:value={$pocket.weight} placeholder="5" aria-label="Weight in lbs" />
  </div>
  <p>How much does the pocket itself weigh (in pounds)?</p>
  {:else}
  <div class="buttons">
    <label for="bulk">Bulk</label>
    <input type="text" name="name" class="only" required bind:value={$pocket.bulk} placeholder="-"/>
  </div>
  <p>How bulky is this pocket? It can be negligible (&mdash;), light (L), or use a whole number.</p>
  {/if}

  <div class="buttons">
    <label for="extradimensional">Extradimensional</label>
    <input type="checkbox" name="extradimensional" class="only" bind:checked={$pocket.extradimensional} />
  </div>
  <p>Extradimensional pockets have different/special encumbrance behavior: the weight or bulk of
    items stored in an extradimensional space do not change the cumulative weight or bulk of the pocket. </p>

  <div class="buttons">
    <span><label for="item-value" class="combined">Value</label> (<label for="quantity" class="combined">Quantity</label>)</span>
    <span class="buttons end">
      <input type="text" name="item-value" bind:value={$pocket.value} placeholder="2gp" />
      <input type="number" name="quantity" size="3" bind:value={$pocket.quantity} placeholder="1" min=1 />
    </span>
  </div>
  <p>Optional string representing the value (in a valid currency) for this pocket. This value is usually
    for one item, but in some cases, the item is so cheap that you get more than one for the lowest
    currency value. For example, you may be able to buy more than one sack for one copper piece.</p>

  <h3 class="buttons">
    Compartments
    <Button buttonClass="buttons {$pocket.compartments.length == 0 ? "required" : ''}" name="profile-add" tip="Add another compartment" left
      clickfn={() => pocket.update((p) => {
        p.compartments.push({});
        return p;
      })}><AddSvg /></Button>
  </h3>
  <table>
    <thead>
      <tr>
        <th class="text constraint">Constraint</th>
        <th class="capacity">Capacity: {#if $activePresetData.capacityType == "weight"}weight (lbs), volume (cubic feet){:else}Bulk{/if}</th>
        <th class="action">&nbsp;</th>
      </tr>
    </thead>
    <tbody>
      {#each $pocket.compartments as compartment}
      <tr>
        <td>
          <input name="constraint" type="text" bind:value={compartment.constraint} placeholder="5 sheets of paper" />
        </td>
        {#if $activePresetData.capacityType == "weight"}
        <td>
          <input class="only" name="max_weight" type="number" step="0.01" min=0 bind:value={compartment.max_weight} placeholder="5" aria-label="Weight in lbs" />
          <input class="only" name="max_volume" type="number" step="0.01" min=0 bind:value={compartment.max_volume} placeholder="5" aria-label="Volume in cubic feet" />
        </td>
        {:else}
        <td><input name="bulk" type="text" bind:value={compartment.max_bulk} placeholder="1" aria-label="Bulk as either a number, 1, or a string: negligible or -, S, M, L" /></td>
        {/if}
        <td class="action">
          <div>
            <Button buttonClass="buttons" name="compartment-delete" tip="Delete compartment" left
            clickfn={() => pocket.update((p) => {
              p.compartments = p.compartments.filter(x => x !== compartment);
              return p;
            })}><TrashSvg /></Button>
          </div>
        </td>
      </tr>
      {/each}
    </tbody>
  </table>

  <p>Note: Constraints and values for maximum bulk or maximum carryable weight/volume are optional.</p>

  <div class="buttons">
    <span></span>
    <Button name="add-pocket" tip="All done! Add pocket to list" buttonClass="buttons only"
        clickfn={submitForm} disabled="{$addPocketDisabled}"
        left><Check /> OK&nbsp;</Button>
  </div>
</form>


