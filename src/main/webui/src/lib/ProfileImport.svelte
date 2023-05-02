<script>
  import ApplySvg from "../svg/zap.svelte";
  import Button from "./ButtonTooltip.svelte";
  import { activeProfileData, updateProfile } from "./stores";
  export let modal;

  let fileVar;
  async function submitForm() {
    if (fileVar) {
      const text = await fileVar[0].text();
      try {
        const data = JSON.parse(text);
        if (data && typeof data.capacityType == 'string') {
          data.id = $activeProfileData.id;
          updateProfile(data);
        }
        modal.set(null);
      } catch(e) {
        console.error("File was not valid JSON", text);
      }
    }
  }
</script>

<h2>Import profile</h2>

<div class="buttons">
  <div class="input-file"><input type="file" bind:files={fileVar} accept=".json,application/json" /></div>
  <Button name="profile-import" tip="Import profile" left
      clickfn={submitForm}><ApplySvg /></Button>
</div>

