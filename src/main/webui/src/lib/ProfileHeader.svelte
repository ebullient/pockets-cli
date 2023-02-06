<script lang="ts">
  import {
    activePresetData,
    activeProfileData,
    activeProfileKey,
    applyPreset,
    fetchedPresets,
    deleteProfile,
  } from "../lib/stores";
  import { writable } from "svelte/store";
  import Modal, { bind } from 'svelte-simple-modal';

  import ApplySvg from "../svg/zap.svelte";
  import ExportSvg from "../svg/log-out.svelte";
  import ImportSvg from "../svg/log-in.svelte";
  import TrashSvg from "../svg/trash-2.svelte";

  import Button from "../lib/ButtonTooltip.svelte";
  import Select from "../lib/SelectTooltip.svelte";
  import DownloadLink from "../lib/DownloadLink.svelte";
  import ProfileImport from "../lib/ProfileImport.svelte";

  const modal = writable(null);
  const showModal = () => modal.set(bind(ProfileImport, {modal: modal}));

  let preset = "";
</script>

<h3 class="buttons profile">
  Profile: {$activeProfileData.id}
  <span class="buttons">
    {#if $activeProfileData.preset }
    <span class="subtext">({$activePresetData.name})</span>
    {:else}
    <span class="buttons" id="apply-presets">
      <Select name="presets" tip="Choose configuration preset" left>
        <select id="presets" bind:value={preset} class:required="{!preset}">
          <option value="" />
          {#each Object.entries($fetchedPresets) as [key, preset]}
            <option value={key}>{preset.name}</option>
          {/each}
        </select>
      </Select>
      <Button buttonClass="buttons {preset ? '' : "required"}" name="profile-apply" tip="Apply selected preset" left
        disabled="{!preset}" disabledTip="Select a preset"
        clickfn={() => applyPreset(preset, $activeProfileKey)}><ApplySvg /></Button>
    </span>
    {/if}
    <span class="buttons">
      <Modal show={$modal} id="profile-import-modal">
        <Button buttonClass="buttons" name="profile-import" tip="Import profile" left
            clickfn={showModal}><ImportSvg /></Button>
      </Modal>
      <DownloadLink buttonClass="buttons" name="profile-export" tip="Export profile" left
            filename={$activeProfileKey} jsonData={JSON.stringify($activeProfileData)}><ExportSvg /></DownloadLink>
      {#if $activeProfileKey != "default"}
      <Button buttonClass="buttons" name="profile-delete" tip="Delete profile" left
          clickfn={() => deleteProfile($activeProfileKey)}><TrashSvg /></Button>
      {/if}
    </span>
  </span>
</h3>
