DROP SEQUENCE IF EXISTS hibernate_sequence;
CREATE SEQUENCE hibernate_sequence START 1;

insert into pocket (id, type_id, name, slug, magic, max_weight, max_volume, weight)
values
  (nextval('hibernate_sequence'), 'pouch',     'Coins',     'coins', false, 6, 1/5, 1),
  (nextval('hibernate_sequence'), 'backpack',  'Backpack',  'backpack', false, 30, 1, 5),
  (nextval('hibernate_sequence'), 'handy-haversack', 'Haversack', 'haversack', true, 120, 12, 5),
  (nextval('hibernate_sequence'), 'backpack',  'Backpack',  'backpack', false, 30, 1, 5);

insert into pocket_item (id, pocket_id, name, slug, quantity, value, weight)
values (nextval('hibernate_sequence'), 2, 'Rations', 'rations', 10, null, null);

