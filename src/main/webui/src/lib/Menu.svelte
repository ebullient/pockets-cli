<script lang="ts">
  import {link} from 'svelte-spa-router'
  import SettingsSvg from "../svg/settings.svelte";
  import HelpSvg from "../svg/help-circle.svelte";
</script>

<nav>
  <ul>
    <li><a href="/settings" use:link><SettingsSvg /> Settings</a></li>
    <li><a href="/help" use:link><HelpSvg /> Help</a></li>
  </ul>
</nav>
