<script>
  export let tip = "";
  export let name = "";
  export let right = false;
  export let left = false;
  export let active = false;
</script>

<span class="tooltip-select">
  <slot />
  <span class="tooltip" id="{name}" class:active class:left class:right>
    <div class="tip">{tip}</div>
  </span>
</span>
