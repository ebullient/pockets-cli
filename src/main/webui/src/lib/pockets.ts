import type { Compartment, PocketRef, Preset } from "../@types/pockets";

export function pocketDescription(pocket: PocketRef, activePresetData: Preset): string {
  let result = pocket.name;
  if (pocket.extradimensional) {
    result += "*";
  }
  if (activePresetData.capacityType == "weight" && pocket.weight) {
    const unit = pocket.weight == 1 ? "lb" : "lbs";
    result += `, ${pocket.weight} ${unit}`;
  } else if (pocket.bulk) {
    result += `, ${pocket.bulk} Bulk`;
  }
  if (pocket.value) {
    result += `<br /> ${pocket.value}`;
    if (pocket.quantity) {
      result += ` for ${pocket.quantity}`;
    }
  }

  if (pocket.compartments) {
    if (
      pocket.compartments.length == 1 &&
      pocket.compartments[0].constraint
    ) {
      result += `<br />${pocket.compartments[0].constraint}`;
    } else if (pocket.compartments.length > 1) {
      result += `<br />This pocket has ${pocket.compartments.length} compartments`;
      if (pocket.compartments.find((c) => c.constraint)) {
        let i = 1;
        pocket.compartments
          .filter((c) => c.constraint)
          .forEach((c) => `<br \>${i++}: ${c}`);
      }
    }
  }
  return result;
}

export function pocketCapacity(pocket: PocketRef): string {
  if (!pocket.compartments) {
    return "";
  }
  let parts = [];
  pocket.compartments.forEach(c => {
    parts.push(pocketCompartmentCapacity(c));
  })

  return parts.length > 0
    ? parts.join("<br />")
    : "";
}

export function pocketCompartmentCapacity(compartment: Compartment) {
  let parts = [];
  if (compartment.max_weight) {
      const unit = compartment.max_weight == 1 ? "lb" : "lbs";
      parts.push(`${compartment.max_weight} ${unit}`);
  }
  if (compartment.max_volume) {
    const unit = compartment.max_volume == 1 ? "foot" : "feet";
    parts.push(`${compartment.max_volume} cubic ${unit}`);
  }
  if (compartment.max_bulk) {
    parts.push(`${compartment.max_bulk} Bulk`);
  }
  return parts.join(" or ");
}

export function sortedPockets(list: PocketRef[]): PocketRef[] {
  return list.sort((a, b) => a.name.localeCompare(b.name));
}
