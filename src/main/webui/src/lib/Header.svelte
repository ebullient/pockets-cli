<script>
  import { configStore, isDirty, resetToDefaults, resetToPrevious, saveConfig } from "../lib/stores";
  import Button from "../lib/ButtonTooltip.svelte";
  import ResetSvg from "../svg/delete.svelte";
  import RestoreSvg from "../svg/rotate-ccw.svelte";
  import SaveSvg from "../svg/save.svelte";
</script>

<h2 class="buttons">
  <slot />
  <span class="buttons">
    <Button name="config-reset-default" tip="Reset to defaults" left
        clickfn={() => resetToDefaults()}><ResetSvg /></Button>
    <Button name="config-reset" tip="Reset to previous" left
        clickfn={() => resetToPrevious()}><RestoreSvg /></Button>
    <Button name="config-save" tip="Save" left
        clickfn={() => saveConfig($configStore)}
        buttonClass={ $isDirty ? "dirty" : "clean" }><SaveSvg /></Button>
  </span>
</h2>
