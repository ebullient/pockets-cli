<script>
  export let tip = "";
  export let name = "";
  export let buttonClass = "";
  export let right = false;
  export let left = false;
  export let active = false;
  export let disabled = false;
  export let disabledTip = "Fix outstanding required elements";
  export let clickfn = () => console.log("button clicked");
</script>

<button type="button" aria-describedby="{name}" on:click={clickfn} class="{buttonClass}" {disabled}>
  <span class="tooltip-slot"><slot /></span>
  <span class="tooltip {name}" id="{name}" class:active class:left class:right>
    <div class="tip">{disabled ? disabledTip : tip}</div>
  </span>
</button>
