import { writable, derived } from 'svelte/store';
import type { Writable } from "svelte/store";
import type { Config, Currency, Pocket, Preset } from '../@types/pockets';

const endpoint = "https://jsonplaceholder.typicode.com/posts";

export const presets: Record<string, Preset> = {
  config5e: {
    name: "D&D 5e",
    capacityType: "weight",
    currency: [
      {
        "notation": "pp",
        "name": "Platinum (pp)",
        "unitConversion": 1000
      },
      {
        "notation": "gp",
        "name": "Gold (gp)",
        "unitConversion": 100
      },
      {
        "notation": "ep",
        "name": "Electrum (ep)",
        "unitConversion": 50
      },
      {
        "notation": "sp",
        "name": "Silver (sp)",
        "unitConversion": 10
      },
      {
        "notation": "cp",
        "name": "Copper (cp)",
        "unitConversion": 1
      }
    ],
    pockets: [
      {
        "name": "Backpack",
        "weight": 5.0,
        "value": "200cp",
        "category": "Adventuring Gear",
        "emoji": "🎒",
        "compartments": [
          {
            "max_weight": 30.0,
            "max_volume": 1.0
          }
        ]
      }
    ]
  },
  configPf2e: {
    name: "Pathfinder 2e",
    capacityType: "bulk",
    currency: [
      {
        "name": "Platinum (pp)",
        "notation": "pp",
        "unitConversion": 1000
      },
      {
        "name": "Gold (gp)",
        "notation": "gp",
        "unitConversion": 100
      },
      {
        "name": "Silver (sp)",
        "notation": "sp",
        "unitConversion": 10
      },
      {
        "name": "Copper (cp)",
        "notation": "cp",
        "unitConversion": 1
      }
    ],
    pockets: [
      {
        "name": "Backpack",
        "bulk": "1",
        "value": "1sp",
        "category": "Adventuring Gear",
        "emoji": "🎒",
        "compartments": [
          {
            "max_bulk": "4",
            "constraint": "The first 2 Bulk of items don't count against your Bulk limits. If you're carrying or stowing the pack rather than wearing it, its Bulk is light instead of negligible."
          }
        ]
      }
    ]
  }
};

let defaultData: Config = {
  profiles: {
    default: {
      name: "default",
      preset: "",
      capacityType: "",
      currency: [],
      pockets: []
    }
  }
};

const previousStr = JSON.stringify(defaultData);
const previous = JSON.parse(previousStr);


const configStore: Writable<Config> = writable(JSON.parse(JSON.stringify(defaultData)));
export const allProfiles = derived(configStore, $c => Object.keys($c.profiles));
export const activeProfileName: Writable<string> = writable("default");

export const isDirty = derived(configStore, $c => {
  return JSON.stringify($c) != previousStr
});

export const activeProfileData = derived([configStore, activeProfileName],
  ([$config, $active]) => {
    console.log("activeProfileData", $config, $active);
    return $config.profiles[$active]
  });


export const resetToPrevious = () => {
  configStore.set(JSON.parse(JSON.stringify(previous)));
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

export const applyPreset = (preset: string, profile: string) => {
  if (presets[preset] && profile) {
    configStore.update((cfg: Config) => {
      cfg.profiles[profile].preset = preset;
      cfg.profiles[profile].capacityType = presets[preset].capacityType;
      cfg.profiles[profile].currency = JSON.parse(JSON.stringify(presets[preset].currency));
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
        currencies.sort((a, b) => b.unitConversion - a.unitConversion );
      }
      return cfg;
    });
  }
}

export const additionalCurrencies = derived(activeProfileData, $p => {
  if (!presets[$p.preset] || !presets[$p.preset].additionalCurrency) {
    console.log("Preset or additional currency does not exist", $p.preset);
    return undefined;
  }
  const result: Record<string, Currency[]> = {};
  Object.entries(presets[$p.preset].additionalCurrency).forEach(([key, value]) => {
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
        pockets.sort((a, b) => a.name.localeCompare(b.name) );
      }
      return cfg;
    });
  }
};

export const presetPockets = derived(activeProfileData, $p => {
  const key = $p.preset;
  if (presets[key]) {
    return presets[key].pockets.filter(p => !$p.pockets.find(x => x.name == p.name));
  }
  return undefined;
});
