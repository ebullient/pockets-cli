import { writable, derived, get } from 'svelte/store';
import type { Writable } from "svelte/store";
import type { Config, Currency, Pocket, Preset } from '../@types/pockets';
import { defaultData, defaultPresets } from './const';

const previousStr: Writable<string> = writable(JSON.stringify(defaultData));
const configStore: Writable<Config> = writable(JSON.parse(JSON.stringify(defaultData)));

export const fetchedPresets: Writable<Record<string, Preset>> = writable(defaultPresets);
export const activeProfileName: Writable<string> = writable("default");

export const allProfiles = derived(configStore, $c => Object.keys($c.profiles));
export const isDirty = derived([configStore, previousStr], ([$c, $p]) => JSON.stringify($c) != $p);

export const activeProfileData = derived([configStore, activeProfileName],
  ([$config, $active]) => {
    console.log("activeProfileData", $config, $active);
    return $config.profiles[$active]
  });

export const activePresetName = derived([fetchedPresets, activeProfileData],
  ([$presets, $p]) => $presets[$p.preset]
    ? $presets[$p.preset].name
    : undefined);

export const resetToPrevious = () => {
  configStore.set(JSON.parse(get(previousStr)));
}

export const resetToDefaults = () => {
  configStore.set(JSON.parse(JSON.stringify(defaultData)));
}

export const createProfile = () => {
  let name;
  configStore.update((cfg: Config) => {
    name = "profile-" + Object.keys(cfg.profiles).length;
    cfg.profiles[name] = JSON.parse(JSON.stringify(defaultData.profiles.default));
    cfg.profiles[name].name = name;
    return cfg;
  });
  activeProfileName.set(name);
};

export const applyPreset = (fetched: Record<string, Preset>, preset: string, profile: string) => {
  if (fetched[preset] && profile) {
    configStore.update((cfg: Config) => {
      cfg.profiles[profile].preset = preset;
      cfg.profiles[profile].capacityType = fetched[preset].capacityType;
      cfg.profiles[profile].currency = JSON.parse(JSON.stringify(fetched[preset].currency));
      return cfg;
    });
  }
}

export const addCurrency = (currency: Currency, profile: string) => {
  if (currency && profile) {
    configStore.update((cfg: Config) => {
      const currencies = cfg.profiles[profile].currency;
      if (currencies.find(c => c.notation == currency.notation)) {
        console.error("Conflict or duplicate. Currency with notation already exists", currency, currencies);
      } else {
        currencies.push(JSON.parse(JSON.stringify(currency)));
        currencies.sort((a, b) => b.unitConversion - a.unitConversion);
      }
      return cfg;
    });
  }
}

export const additionalCurrencies = derived([fetchedPresets, activeProfileData], ([$presets, $p]) => {
  const preset = $presets[$p.preset];
  if (!preset || !preset.additionalCurrency) {
    return undefined;
  }
  const result: Record<string, Currency[]> = {};
  Object.entries(preset.additionalCurrency).forEach(([key, value]) => {
    const currencies = value.filter(c => !$p.currency.find(x => c.notation == x.notation));
    if (currencies.length > 0) {
      result[key] = currencies;
    }
  });
  return result;
});

export const addPocket = (pocket: Pocket, profile: string) => {
  if (pocket && profile) {
    configStore.update((cfg: Config) => {
      const pockets = cfg.profiles[profile].pockets;
      if (pockets.find(p => p.name == pocket.name)) {
        console.error("Conflict or duplicate. Pocket with name already exists", pocket, pockets);
      } else {
        pockets.push(JSON.parse(JSON.stringify(pocket)));
      }
      return cfg;
    });
  }
};

export const presetPockets = derived([fetchedPresets, activeProfileData], ([$presets, $p]) => {
  const preset = $presets[$p.preset];
  if (preset) {
    return preset.pockets.filter(p => !$p.pockets.find(x => x.name == p.name));
  }
  return undefined;
});


