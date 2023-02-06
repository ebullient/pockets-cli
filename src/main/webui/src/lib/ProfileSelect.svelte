<script lang="ts">
  import { allProfiles, configStore, createProfile } from "./stores";
  import SelectTooltip from "./SelectTooltip.svelte";
  import Button from "../lib/ButtonTooltip.svelte";
  import AddSvg from "../svg/plus.svelte";

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
      <select name="profiles" id="profiles" bind:value={$configStore.activeProfile}>
        {#each $allProfiles as x}
          <option value="{x.k}">{x.v}</option>
        {/each}
      </select>
    </SelectTooltip>
    {/if}
    {#if create}
    <Button buttonClass="buttons" name="profile-add" tip="Add another profile" left
        clickfn={() => createProfile()}><AddSvg /></Button>
    {/if}
  </span>
</div>
