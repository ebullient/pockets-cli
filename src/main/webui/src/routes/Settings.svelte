<script lang="ts">
  import { additionalCurrencies, applyPreset, activeProfileData, addCurrency, fetchedPresets, activePresetName } from "../lib/stores";
  import ApplySvg from "../svg/zap.svelte";
  import Button from "../lib/ButtonTooltip.svelte";
  import Header from "../lib/Header.svelte";
  import ProfileSelect from "../lib/ProfileSelect.svelte";
  import Select from "../lib/SelectTooltip.svelte";
  import ExportSvg from "../svg/log-out.svelte";
  import ImportSvg from "../svg/log-in.svelte";
  import TrashSvg from "../svg/trash-2.svelte";

  let preset = "";

</script>

<form id="settings">
  <Header>Settings</Header>
  <ProfileSelect create />

  <h4 class="buttons profile">
    Profile: {$activeProfileData.name}
    <span class="buttons">
      {#if $activeProfileData.preset == "" }
      <span class="buttons" id="apply-presets">
        <Select name="presets" tip="Choose configuration preset" left>
          <select id="presets" bind:value={preset}>
            <option value="" />
            {#each Object.entries($fetchedPresets) as [key, preset]}
              <option value={key}>{preset.name}</option>
            {/each}
          </select>
        </Select>
        <Button name="profile-apply" tip="Apply selected preset" left
          clickfn={() => applyPreset($fetchedPresets, preset, $activeProfileData.name)}><ApplySvg /></Button>
      </span>
      {:else}
      <span class="subtext">({$activePresetName})</span>
      {/if}
      <span class="buttons">
        <Button name="profile-import" tip="Import profile" left><ImportSvg /></Button>
        <Button name="profile-export" tip="Export profile" left><ExportSvg /></Button>
        {#if $activeProfileData.name != "default"}
        <Button name="profile-delete" tip="Delete profile" left><TrashSvg /></Button>
        {/if}
      </span>
    </span>
  </h4>

  {#if $activeProfileData.name != "default"}
  <div class="buttons">
    <label for="name">Name</label><input name="name" type="text" value="{$activeProfileData.name}"/>
  </div>
  <p>
    Unique identifier for this profile. Changing this value after pockets have been
    created will be disruptive.<br />
  </p>

  <div class="buttons">
    <div>
      <label for="description">Description</label>
      <p>Notes for future you.</p>
    </div>
    <textarea name="description"/>
  </div>

  {/if}

  <h4>Encumbrance</h4>
  {#if $activeProfileData.capacityType}
  <p>This profile uses <b>{$activeProfileData.capacityType}</b> as a measure of encumbrance.</p>
  {:else}
  <p>Select and apply a preset to see encumbrance rules.</p>
  {/if}

  {#if $activeProfileData.capacityType == "weight"}
  <p>
  When using <b>weight</b>, the capacity of containers, vessels, and pockets
  is computed by the cumulative weight (in pounds) or volume (cubic feet)
  of the items within it.
  </p>
  {:else if $activeProfileData.capacityType == "bulk"}
  <p>
  The Bulk value of an item reflects how difficult the item is to handle,
  representing its size, weight, and general awkwardness. Items can have
  negligible bulk (&mdash;); they can be light (L); or they can have a
  bulk value assigned. Ten light items count as 1 bulk. Items of negligible
  bulk do not usually contribute to cumulative bulk values unless there are
  a lot of them.
  </p>
  {/if}

  <h4>Currency</h4>
  {#if $activeProfileData.preset}
  <p>This profile defines the currency shown in the following table.</p>
  <table>
    <thead><tr><th>Currency Name</th><th>Notation</th><th>Value</th></tr></thead>
    <tbody>
      {#each $activeProfileData.currency as coin}
      <tr>
        <td>{coin.name}</td>
        <td>{coin.notation}</td>
        <td>{coin.unitConversion}</td>
      </tr>
      {/each}
    </tbody>
  </table>
  {:else}
  <p>Select and apply a preset to assign a currency to this profile.</p>
  {/if}
  {#if $additionalCurrencies && Object.keys($additionalCurrencies).length > 0 }
  <p>Additional currencies are avaialable for this preset. Click to add them:</p>
  <ul>
    {#each Object.entries($additionalCurrencies) as [key, currency]}
    <li><span class="title">{key}</span>:
      {#each currency as coin, i (coin.name)}
      <button on:click={() => addCurrency(coin, $activeProfileData.name)}>{coin.name}</button>{#if i < currency.length - 1}, {/if}
      {/each}
    </li>
    {/each}
  </ul>
  {/if}

</form>
