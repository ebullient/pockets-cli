<script lang="ts">
  import { link } from "svelte-spa-router";
  import {
    activePresetName,
    activeProfileData,
    addPocket,
    presetPockets,
  } from "../lib/stores";
  import Header from "../lib/Header.svelte";
  import ProfileSelect from "../lib/ProfileSelect.svelte";
  import type { Compartment, Pocket } from "../@types/pockets";

  function pocketDescription(pocket: Pocket): string {
    let result = pocket.name;
    if (pocket.extradimensional) {
      result += "*";
    }
    if ($activeProfileData.capacityType == "weight" && pocket.weight) {
      result += `, ${pocket.weight} lbs`;
    } else if (pocket.bulk) {
      result += `, ${pocket.bulk}`;
    }
    if (pocket.rarity && pocket.rarity != "none") {
      result += `<br /><i>${pocket.rarity}</i>`;
    }
    if (pocket.value || pocket.category) {
      result += "<br />";
      const addendum = [];
      if (pocket.category) {
        addendum.push(pocket.category);
      }
      if (pocket.value) {
        addendum.push(pocket.value);
      }
      result += addendum.join(", ");
    }
    if (pocket.compartments) {
      if (
        pocket.compartments.length == 1 &&
        pocket.compartments[0].constraint
      ) {
        result += `<br />This may contain ${pocket.compartments[0].constraint}`;
      } else if (pocket.compartments.length > 1) {
        result += `<br />This pocket has ${pocket.compartments.length} compartments`;
        if (pocket.compartments.find((c) => c.constraint)) {
          result += ` which may contain:`;
          let i = 1;
          pocket.compartments
            .filter((c) => c.constraint)
            .forEach((c) => `<br \>${i++}: ${c}`);
        }
      }
    }
    return result;
  }

  function pocketCapacity(pocket: Pocket): string {
    if (!pocket.compartments) {
      return "";
    }
    let parts = [];
    pocket.compartments.forEach(c => {
      parts.push(pocketCompartmentCapacity(c));
    })

    return parts.length > 0
      ? parts.join("<br />")
      : "";
  }

  function pocketCompartmentCapacity(compartment: Compartment) {
    let parts = [];
    if (compartment.max_weight) {
        parts.push(`${compartment.max_weight} lbs`);
    }
    if (compartment.max_volume) {
      const unit = compartment.max_volume == 1 ? "foot" : "feet";
      parts.push(`${compartment.max_volume} cubic ${unit}`);
    }
    if (compartment.max_bulk) {
      parts.push(`${compartment.max_bulk} bulk`);
    }
    return parts.join(" or ");
  }
</script>

{#if $activeProfileData.preset}
  <form id="pockets">
    <Header>Pockets</Header>
    <ProfileSelect />
    <h4 class="buttons profile">
      Profile: {$activeProfileData.name}
      <span class="subtext">({$activePresetName})</span>
    </h4>

    You can always make custom pockets on the fly, but these predefined pockets are ready to use.

    {#if $activeProfileData.pockets.length > 0}
      <table>
        <thead>
          <tr>
            <th /><th>Pocket</th><th>Capacity</th>
          </tr>
        </thead>
        <tbody>
          {#each $activeProfileData.pockets as pocket}
            <tr>
              <td>{pocket.emoji}</td>
              <td>{@html pocketDescription(pocket)}</td>
              <td>{@html pocketCapacity(pocket)}</td>
            </tr>
          {/each}
        </tbody>
      </table>
    {/if}

    {#if $presetPockets && $presetPockets.length > 0}
      <p>
        The following pockets are available for your preset. Click to add them:
        {#each $presetPockets as pocket, i (pocket.name)}
          <button on:click={() => addPocket(pocket, $activeProfileData.name)}
            >{pocket.name}</button
          >{#if i < $presetPockets.length - 1}, {/if}
        {/each}
      </p>
    {/if}
  </form>
{:else}
  <h2>What have you got?</h2>

  <p>
    <a href="/settings" use:link>Select and apply a preset</a> to work with your
    pockets!
  </p>
{/if}
