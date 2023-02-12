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
    console.log("Describe pocket", pocket);
    let result = pocket.name;
    if (pocket.extradimensional) {
      result += "*";
    }
    if ($activeProfileData.capacityType == "weight" && pocket.weight) {
      const unit = pocket.weight == 1 ? "lb" : "lbs";
      result += `, ${pocket.weight} ${unit}`;
    } else if (pocket.bulk) {
      result += `, ${pocket.bulk}`;
    }
    if (pocket.value || pocket.category || pocket.rarity) {
      result += "<br />";
      const addendum = [];
      if (pocket.category) {
        if (pocket.rarity && pocket.rarity != "none") {
          result += `${pocket.category} <em>(${pocket.rarity})</em>`;
        } else {
          addendum.push(pocket.category);
        }
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
        result += `<br />${pocket.compartments[0].constraint}`;
      } else if (pocket.compartments.length > 1) {
        result += `<br />This pocket has ${pocket.compartments.length} compartments`;
        if (pocket.compartments.find((c) => c.constraint)) {
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
        const unit = compartment.max_weight == 1 ? "lb" : "lbs";
        parts.push(`${compartment.max_weight} ${unit}`);
    }
    if (compartment.max_volume) {
      const unit = compartment.max_volume == 1 ? "foot" : "feet";
      parts.push(`${compartment.max_volume} cubic ${unit}`);
    }
    if (compartment.max_bulk) {
      parts.push(`${compartment.max_bulk} Bulk`);
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
