<script>
  export let tip = "";
  export let name = "";
  export let filename = "download";
  export let buttonClass = "button";
  export let jsonData = "";
  export let right = false;
  export let left = false;
  export let active = false;

  const blob = new Blob([jsonData], { type: 'application/json;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
</script>

<a href="{url}" download="{filename}.json" aria-describedby="{name}" class="button {buttonClass}" >
  <span class="tooltip-slot"><slot /></span>
  <span class="tooltip {name}" id="{name}" class:active class:left class:right>
    <div class="tip">{tip}</div>
  </span>
</a>
