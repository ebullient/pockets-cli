<script lang="ts">
  import { allProfiles, activeProfileName, createProfile } from "./stores";
  import SelectTooltip from "./SelectTooltip.svelte";
  import Button from "../lib/ButtonTooltip.svelte";
  import AddSvg from "../svg/plus.svelte";
  import {link} from 'svelte-spa-router'

  export let create = false;
</script>

<div class="buttons">
  {#if $allProfiles.length > 1}
  <label for="profiles">Choose a profile</label>
  {:else if create}
  <span>Add an additional profile (optional)</span>
  {/if}
  <span class="buttons">
    {#if $allProfiles.length > 1}
    <SelectTooltip name="profile-select" tip="Change the active profile" left >
      <select name="profiles" id="profiles" bind:value={$activeProfileName}>
        {#each $allProfiles as x}
          <option value="{x}">{x}</option>
        {/each}
      </select>
    </SelectTooltip>
    {/if}
    {#if create}
    <Button name="profile-add" tip="Add another profile" left
        clickfn={() => createProfile()}><AddSvg /></Button>
    {/if}
  </span>
</div>
