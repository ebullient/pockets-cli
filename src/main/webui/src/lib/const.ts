import { empty } from "svelte/internal";
import type { ProfileConfig, Preset } from "../@types/pockets";

export const defaultPresets: Record<string, Preset> = {
  dnd5e: {
    name: "D&D 5e",
    capacityType: "weight",
    currency: [
      {
        "id": "cp",
        "notation": "cp",
        "name": "Copper (cp)",
        "unitConversion": 1
      }
    ],
    pocketRef: [
      {
        "id": "backpack",
        "name": "Backpack",
        "weight": 5.0,
        "value": "200cp",
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
  pf2e: {
    name: "Pathfinder 2e",
    capacityType: "bulk",
    currency: [
      {
        "id": "cp",
        "notation": "cp",
        "name": "Copper (cp)",
        "unitConversion": 1
      }
    ],
    pocketRef: [
      {
        "id": "backpack",
        "name": "Backpack",
        "bulk": "1",
        "value": "1sp",
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

export const defaultPreset: Preset = {
  "name": "",
  "capacityType": "",
  "currency": [],
  "pocketRef": []
};


export const defaultProfile: ProfileConfig = {
  id: "default",
  preset: "",
  currency: [],
  pocketRef: [],
  state: 'empty'
};

export const defaultProfiles = {
  default: defaultProfile
};

export const defaultConfig = {
  activeProfile: "default",
  profiles: defaultProfiles
};
