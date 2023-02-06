
import Settings from '../routes/Settings.svelte';
import Pockets from '../routes/Pockets.svelte';
import Help from '../routes/Help.svelte';

const routes = {
  "/settings": Settings,
  "/help": Help,
  "/pockets": Pockets,
  "*": Pockets
};

export default routes;
