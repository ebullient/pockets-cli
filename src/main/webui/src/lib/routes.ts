
import Settings from '../routes/Settings.svelte';
import Help from '../routes/Help.svelte';

const routes = {
  "/settings": Settings,
  "/help": Help,
  "*": Settings
};

export default routes;
