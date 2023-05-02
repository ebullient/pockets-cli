<script lang="ts">
  import { activeProfileData, activeProfileKey, addPocket, deletePocket, additionalPockets, activePresetData } from "./stores";
  import { pocketCapacity, pocketDescription, sortedPockets } from "../lib/pockets";
  import { writable } from "svelte/store";
  import Modal, { bind } from 'svelte-simple-modal';
  import AddSvg from "../svg/plus.svelte";
  import PencilSvg from "../svg/pencil.svelte";
  import TrashSvg from "../svg/trash-2.svelte";
  import EditPocket from "./EditPocket.svelte";
  import Button from "./ButtonTooltip.svelte";
  import type { PocketRef } from "../@types/pockets";

  const pocketModal = writable(null);
  const showPocketModal = (pocket: PocketRef, i: number) => {
    pocketModal.set(bind(EditPocket, {initPocket: pocket, index: i, modal: pocketModal}));
  }
  const closePocketModal = () => {
    pocketModal.set(null);
    document.body.style.removeProperty('position');
    document.body.style.removeProperty('top');
    document.body.style.removeProperty('overflow');
    document.body.style.removeProperty('width');
  }
</script>

<h3 id="pockets">Pockets</h3>

{#if $activeProfileData.preset}
  <div class="buttons">
    <p class="button-row">Pockets can be created and customized on the fly. These predefined pockets simplify pocket creation.</p>
    <Modal id="add-pocket-modal" show={$pocketModal} on:closing={closePocketModal}>
      <Button buttonClass="buttons only" name="pocket-add" tip="Add a custom pocket" left
          clickfn={() => showPocketModal(null, -1)}><AddSvg /></Button>
    </Modal>
  </div>
  <table>
    <thead>
      <tr><th /><th class="text">Pocket</th><th>Capacity</th><th class="action">&nbsp;</th></tr>
      <tr class="secondary"><th colspan="4">Preset pockets</th></tr>
    </thead>
    <tbody>
      {#each sortedPockets($activePresetData.pocketRef) as pocket, i}
      <tr>
        <td>{pocket.emoji}</td>
        <td class="text">{@html pocketDescription(pocket, $activePresetData)}</td>
        <td>{@html pocketCapacity(pocket)}</td>
        <td class="action">&nbsp;</td>
      </tr>
      {/each}
    </tbody>
    {#if $activeProfileData.pocketRef.length > 0}
    <thead>
      <tr class="secondary"><th colspan="4">Additional pockets</th></tr>
    </thead>
    <tbody>
      {#each sortedPockets($activeProfileData.pocketRef) as pocket, i}
      <tr>
        <td>{pocket.emoji}</td>
        <td class="text">{@html pocketDescription(pocket, $activePresetData)}</td>
        <td>{@html pocketCapacity(pocket)}</td>
        <td class="action">
          <div>
            <Modal id="edit-pocket-modal" show={$pocketModal} on:closing={closePocketModal}>
              <Button buttonClass="buttons" name="pocket-edit" tip="Edit pocket" left
                  clickfn={() => showPocketModal(pocket, i)}><PencilSvg /></Button>
            </Modal>
            <Button buttonClass="buttons" name="pocket-delete" tip="Delete pocket" left
                clickfn={() => deletePocket(pocket, $activeProfileKey)}><TrashSvg /></Button>
          </div>
        </td>
      </tr>
      {/each}
    </tbody>
  {/if}
</table>

  {#if $additionalPockets && Object.keys($additionalPockets).length > 0 }
  <p>Additional pockets are available with this preset. Click to add them:</p>
  <ul>
    {#each Object.entries($additionalPockets) as [key, pockets]}
    <li><span class="title">{key}</span>:
      {#each pockets as pocket, i (pocket.name)}
      <Button name="add-{i}" tip="Add to pocket list" left
          clickfn={() => addPocket(pocket, $activeProfileKey)}>{pocket.name}</Button>{#if i < pockets.length - 1}, {/if}
      {/each}
    </li>
    {/each}
  </ul>
  {/if}
{:else}
  <p>Select a preset to assign pockets to this profile</p>
{/if}
