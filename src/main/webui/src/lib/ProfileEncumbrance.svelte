<script>
  import { activePresetData, activeProfileData } from "./stores";
</script>

<h3 id="encumbrance">Encumbrance</h3>

{#if $activePresetData.capacityType}
<p>This profile uses <b>{$activePresetData.capacityType}</b> as a measure of encumbrance.</p>
{:else}
<p>Select and apply a preset to see encumbrance rules.</p>
{/if}

{#if $activePresetData.capacityType == "weight"}
<p>
When using <b>weight</b>, the capacity of containers, vessels, and pockets
is computed by the cumulative weight (in pounds) or volume (cubic feet)
of the items within it.
</p>
{:else if $activePresetData.capacityType == "bulk"}
<p>
The Bulk value of an item reflects how difficult the item is to handle,
representing its size, weight, and general awkwardness. Items can have
negligible bulk (&mdash;); they can be light (L); or they can have a
bulk value assigned. Ten light items count as 1 bulk. Items of negligible
bulk do not usually contribute to cumulative bulk values unless there are
a lot of them.
</p>
{/if}
