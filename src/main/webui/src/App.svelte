<script lang="ts">
  import Router from 'svelte-spa-router';
  import routes from './lib/routes';
  import Menu from './lib/Menu.svelte';
  import { onMount, onDestroy } from 'svelte';

  onMount(async () => {
    const response = await fetch(window.location.origin + '/config/presets.json');
    const data = await response.json();
    console.log(data);
  })

	onDestroy(() => {
		console.log('the component is being destroyed');
	});
</script>
<header>
  <h1>Pockets</h1>
  <Menu/>
</header>
{window.location.origin}
<main>
  <Router {routes}/>
</main>
<footer>
  Written with ❤️ using Java, and <a href="https://quarkus.io">Quarkus</a><br />
  (including <a href="https://quarkus.io/guides/qute-reference">Qute</a>, <a href="https://quarkus.io/guides/hibernate-orm-panache">Panache</a>, <a href="https://quarkiverse.github.io/quarkiverse-docs/quarkus-quinoa">Quinoa</a>, and <a href="https://camel.apache.org/camel-quarkus/">Apache Camel</a>).<br />
  UI uses <a href="https://svelte.dev/">Svelte</a>, <a href="https://vitejs.dev/">Vite</a> and <a href="https://lucide.dev/">Lucide</a>
</footer>
