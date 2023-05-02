type Theme = 'system' | 'light' | 'dark';

type ProfileConfigState = 'isNew' | 'hasData' | 'empty';

export interface PocketDefaults {
  presets: Record<string, Partial<Preset>>;
}

export interface PocketConfig {
  activeProfile: string;
  profiles: Record<string, ProfileConfig>;
}

export interface ProfileConfig {
  id: string;
  description?: string;
  preset: string;
  currency: Currency[];
  pocketRef: PocketRef[];
  // itemRef: Record<string, ItemRef>;
  state: ProfileConfigState;
}

export interface Preset {
  name: string;
  capacityType: string;
  currency: Currency[];
  additionalCurrency?: Record<string, Currency[]>;
  pocketRef: PocketRef[];
  additionalPockets?: Record<string, PocketRef[]>;
}

export interface ItemRef {
  id: string;
  name: string;

  value?: string;
  quantity?: number;

  bulk?: string;
  weight?: number;
  tradeable?: boolean;
}

export interface Currency extends ItemRef {
  notation: string;
  unitConversion: number;
}

export interface PocketRef extends ItemRef {
  emoji?: string;
  extradimensional?: boolean;
  compartments: Compartment[];
}

export interface Compartment {
  constraint?: string;
  max_bulk?: string;
  max_weight?: number;
  max_volume?: number;
}
