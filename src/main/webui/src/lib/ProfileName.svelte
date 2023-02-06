<script lang="ts">
  import { configStore } from "./stores";
</script>



{#if $configStore.profiles[$configStore.activeProfile].state == 'isNew'}
<div class="buttons">
  <label for="name">Id</label>
  <input name="name" type="text" class="only"
      bind:value={$configStore.profiles[$configStore.activeProfile].id}/>
</div>
<p>
  Unique identifier for this profile (slugified-form). This is a set-once operation! Choose wisely.<br />
</p>
{/if}

<div class="buttons">
  <div>
    <label for="description">Description</label>
    <p>A descriptive name or notes for future you.</p>
  </div>
  <textarea name="description" class="only" bind:value={$configStore.profiles[$configStore.activeProfile].description}></textarea>
</div>
