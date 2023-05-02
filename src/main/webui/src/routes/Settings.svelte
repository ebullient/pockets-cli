<script lang="ts">
  import Header from "../lib/Header.svelte";

  import ProfileName from "../lib/ProfileName.svelte";
  import ProfileEncumbrance from "../lib/ProfileEncumbrance.svelte";
  import ProfileCurrency from "../lib/ProfileCurrency.svelte";

  import ProfilePockets from "../lib/ProfilePockets.svelte";
  import ProfileSelect from "../lib/ProfileSelect.svelte";
  import ProfileHeader from "../lib/ProfileHeader.svelte";
</script>

<form id="settings">
  <Header>Settings</Header>

  <ProfileSelect create />

  <nav id="profile"><a href="#encumbrance">Encumbrance</a><a href="#currency">Currency</a><a href="#pockets">Pockets</a></nav>

  <ProfileHeader />

  <ProfileName />

  <ProfileEncumbrance />

  <ProfileCurrency />

  <ProfilePockets />

</form>
