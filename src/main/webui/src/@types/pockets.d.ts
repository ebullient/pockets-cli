type Theme = 'system' | 'light' | 'dark';

export interface Config {
  profiles: Record<string, ConfigProfile>;
}

export interface ConfigProfile {
  name: string;
  preset: string;
  capacityType: string;
  currency: Currency[];
  pockets: Pocket[] | BulkPocket[] | WeightPocket[];
}

export interface Preset {
  name: string;
  capacityType: string;
  currency: Currency[];
  additionalCurrency?: Record<string, Currency[]>;
  pockets: Pocket[];
}

export interface Currency {
  name: string;
  notation: string;
  unitConversion: number;
}

export interface Pocket {
  name: string;
  value?: string;
  quantity?: number;
  rarity?: string;
  category?: string;
  emoji?: string;
  extradimensional?: boolean;
  compartments: Compartment[];
  bulk?: string;
  weight?: number;
}

export interface Compartment {
  constraint?: string;
  max_bulk?: string;
  max_weight?: number;
  max_volume?: number;
}
