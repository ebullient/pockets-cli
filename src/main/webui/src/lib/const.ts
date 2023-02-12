import type { Config, ConfigProfile, Preset } from "../@types/pockets";

export const defaultPresets: Record<string, Preset> = {
  config5e: {
    name: "D&D 5e",
    capacityType: "weight",
    currency: [
      {
        "notation": "gp",
        "name": "Gold (gp)",
        "unitConversion": 100
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

export const defaultProfile: ConfigProfile = {
  name: "default",
  preset: "",
  capacityType: "",
  currency: [],
  pockets: []
};

export const defaultData: Config = {
  profiles: {
    default: JSON.parse(JSON.stringify(defaultProfile))
  }
};
