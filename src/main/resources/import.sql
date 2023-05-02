INSERT INTO pocket (id, pocketRef, name, slug, extradimensional, max_weight, max_volume, weight, notes)
VALUES
  (1, 'pouch',           'Coins',     'coins',    false, 6, 1/5, 1, null),
  (2, 'backpack',        'Backpack',  'backpack',  false, 30, 1, 5, null),
  (3, 'handy-haversack', 'Haversack', 'haversack', true, 120, 12, 5, null),
  (4, 'backpack',        'Backpack',  'backpack',  false, 30, 1, 5, null);
ALTER SEQUENCE pocket_seq RESTART WITH 5;

INSERT INTO item (id, pocket_id, name, slug, itemRef, quantity, cpValue, weight, tradable)
VALUES
  (1, 2, 'Rations', 'rations', 'rations', 10, 50, 2.0, true),
  (2, 1, 'Gold (gp)', 'gold-gp', 'gold-gp', 50, 100, 0.02, true),
  (3, 1, 'Silver (sp)', 'silver-sp', 'silver-sp', 1, 10, 0.02, true);
ALTER SEQUENCE item_seq RESTART WITH 5;
