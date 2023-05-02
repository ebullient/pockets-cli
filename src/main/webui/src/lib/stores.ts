import { writable, derived, get, type Readable } from 'svelte/store';
import type { Writable } from "svelte/store";
import type { PocketConfig, ProfileConfig, Currency, PocketRef, Preset } from '../@types/pockets';
import { defaultConfig, defaultProfiles, defaultProfile, defaultPreset } from './const';

const uriBase = window.location.origin;

export const configStore: Writable<PocketConfig> = writable(JSON.parse(JSON.stringify(defaultConfig)));
export const allProfiles = derived(configStore, $c => Object.entries($c.profiles).map(([k, v]) => { return {k: k, v: v.id}; }));

const previousProfiles: Writable<string> = writable(JSON.stringify(defaultProfiles));
export const isDirty = derived([configStore, previousProfiles], ([$c, $p]) => JSON.stringify($c.profiles) != $p);

export const fetchedPresets: Writable<Record<string, Preset>> = writable({});

export const activeProfileKey = derived(configStore, $c => $c.activeProfile);
export const activeProfileData = derived(configStore, $c => $c.profiles[$c.activeProfile]);
export const activePresetData: Readable<Preset> = derived([configStore, fetchedPresets], ([$c, $f]) => {
  const activeProfileKey = $c.activeProfile || "default";
  const activeProfileData = $c.profiles[activeProfileKey];
  activeProfileData.id = slugify(activeProfileData.id);
  console.log(activeProfileKey, activeProfileData);
  const preset = activeProfileData.preset ? $f[activeProfileData.preset] : defaultPreset;
  return preset;
});

export const resetToPrevious = () => {
  configStore.update((cfg: PocketConfig) => {
    cfg.profiles = JSON.parse(get(previousProfiles));
    cfg.activeProfile = "default";
    return cfg;
  });
}

export const resetToDefaults = () => {
  configStore.set(JSON.parse(JSON.stringify(defaultConfig)));
}

export const loadConfig = async () => {
  const defaults = await fetch(uriBase + "/config/defaults.json");
  if (defaults.ok) {
    const data = await defaults.json();
    console.log("loadDefaults", data);
    fetchedPresets.set(data.presets);
  } else {
    console.error(defaults.status, defaults.statusText, defaults);
  }

  const current = await fetch(uriBase + "/config/current");
  if (current.ok) {
    const data = await current.json();
    console.log("loadConfig", data);
    mergeConfig(data);
  } else {
    console.error(current.status, current.statusText, current);
  }
}

const mergeConfig = async(data: PocketConfig) => {
  console.log("mergeConfig response", data);
  if (data.activeProfile) {
    const prevActiveName = get(activeProfileData).id;
    const presets = get(fetchedPresets);
    Object.keys(data.profiles).forEach(k => {
      data.profiles[k] = {
        ...JSON.parse(JSON.stringify(defaultProfile)),
        ...data.profiles[k]
      };
      const preset = data.profiles[k].preset;
      if (!preset || !presets[preset]) {
        data.profiles[k].preset = "";
      }
    });
    const newActiveProfile = Object.values(data.profiles).find(p => p.id === prevActiveName);
    if (newActiveProfile) {
      console.log("TODO: new active profile?")
    }
    if (!data.profiles[data.activeProfile]) {
      console.log("TODO: different active profile?")
    }
    previousProfiles.set(JSON.stringify(data.profiles));
    configStore.set(data);
  }
}

export const saveConfig = async (config: PocketConfig) => {
  console.log("saveConfig", config);
  const body = JSON.stringify(config);
  const response = await fetch(uriBase + "/config/current", {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json'
    },
    body
  });
  if (response.ok) {
    const data: PocketConfig = await response.json();
    mergeConfig(data);
  } else {
    console.error(response.status, response.statusText, response);
  }
}

export const createProfile = () => {
  configStore.update((cfg: PocketConfig) => {
    const id = "replace-me";
    cfg.activeProfile = id;
    cfg.profiles[id] = JSON.parse(JSON.stringify(defaultProfile));
    cfg.profiles[id].id = id;
    cfg.profiles[id].state = 'isNew';
    return cfg;
  });
};

export const updateProfile = (profile: ProfileConfig) => {
  configStore.update((cfg: PocketConfig) => {
    cfg.profiles[cfg.activeProfile] = {
      ...JSON.parse(JSON.stringify(defaultProfile)),
      ...profile};
    return cfg;
  });
}

export const deleteProfile = (profile: string) => {
  configStore.update((cfg: PocketConfig) => {
    delete cfg.profiles[profile];
    cfg.activeProfile = "default";
    return cfg;
  });
};

export const applyPreset = async (preset: string, activeKey: string) => {
  const f = get(fetchedPresets);
  let presetData = f[preset];
  if (!presetData.capacityType) {
    // fetch the preset data, we don't have it yet
    let response = await fetch(uriBase + "/preset/" + preset);
    presetData = await response.json();
    f[preset] = presetData;
    console.log("fetched preset", preset, f);
    fetchedPresets.set(f);
  }
  console.log(activeKey, preset, presetData);
  if (presetData.capacityType && activeKey) {
    configStore.update((cfg: PocketConfig) => {
      cfg.profiles[activeKey].preset = preset;
      return cfg;
    });
  }
}

export const addCurrency = (currency: Currency, profile: string) => {
  if (currency && profile) {
    configStore.update((cfg: PocketConfig) => {
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

export const updateCurrency = (index: number, currency: Currency, profile: string) => {
  if (currency && profile) {
    configStore.update((cfg: PocketConfig) => {
      const currencies = cfg.profiles[profile].currency;
      const old = currencies.splice(index, 1);
      if (currencies.find(c => c.notation == currency.notation)) {
        console.error("Conflict or duplicate. Currency with notation already exists", currency, currencies);
        currencies.push(...old);
      } else {
        currencies.push(JSON.parse(JSON.stringify(currency)));
      }
      currencies.sort((a, b) => b.unitConversion - a.unitConversion);
      return cfg;
    });
  }
}

export const deleteCurrency = (currency: Currency, profile: string) => {
  if (currency && profile) {
    configStore.update((cfg: PocketConfig) => {
      cfg.profiles[profile].currency = cfg.profiles[profile].currency.filter(c => c != currency);
      return cfg;
    });
  }
}

export const additionalCurrencies = derived([activePresetData, activeProfileData], ([$preset, $profile]) => {
  if (!$preset || !$preset.additionalCurrency) {
    return undefined;
  }
  const result: Record<string, Currency[]> = {};
  Object.entries($preset.additionalCurrency).forEach(([key, value]) => {
    const currencies = value.filter(c => !$profile.currency.find(x => c.notation == x.notation));
    if (currencies.length > 0) {
      result[key] = currencies;
    }
  });
  return result;
});

export const addPocket = (pocket: PocketRef, profile: string) => {
  if (pocket && profile) {
    configStore.update((cfg: PocketConfig) => {
      const pockets = cfg.profiles[profile].pocketRef;
      if (pockets.find(p => p.name == pocket.name)) {
        console.error("Conflict or duplicate. Pocket with name already exists", pocket, pockets);
      } else {
        pockets.push(JSON.parse(JSON.stringify(pocket)));
      }
      return cfg;
    });
  }
};

export const updatePocket = (index: number, pocket: PocketRef, profile: string) => {
  if (pocket && profile) {
    configStore.update((cfg: PocketConfig) => {
      const pockets = cfg.profiles[profile].pocketRef;
      const old = pockets.splice(index, 1);
      if (pockets.find(p => p.name == pocket.name)) {
        console.error("Conflict or duplicate. Pocket with name already exists", pocket, pockets);
        pockets.push(...old);
      } else {
        pockets.push(JSON.parse(JSON.stringify(pocket)));
      }
      return cfg;
    });
  }
}

export const deletePocket = (pocket: PocketRef, profile: string) => {
  if (pocket && profile) {
    configStore.update((cfg: PocketConfig) => {
      cfg.profiles[profile].pocketRef = cfg.profiles[profile].pocketRef.filter(p => p != pocket);
      return cfg;
    });
  }
};

export const additionalPockets = derived([activePresetData, activeProfileData], ([$preset, $profile]) => {
  if (!$preset || !$preset.additionalPockets) {
    return undefined;
  }
  const result: Record<string, PocketRef[]> = {};
  Object.entries($preset.additionalPockets).forEach(([key, value]) => {
    const pockets = value.filter(p => !$profile.pocketRef.find(x => p.name == x.name));
    if (pockets.length > 0) {
      result[key] = pockets;
    }
  });
  return result;
});

export const slugify = (s: string): string => {
  return s.toLowerCase()
      .replace(/\s+/g, '-')           // Replace spaces with -
      .replace(/[^\w\-]+/g, '')       // Remove all non-word chars
      .replace(/[^\x00-\xFF]/g, "")   // ASCII with accents
      .replace(/\-\-+/g, '-');         // Replace multiple - with single -
}
