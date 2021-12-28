DROP SEQUENCE IF EXISTS hibernate_sequence;
CREATE SEQUENCE hibernate_sequence START 1;

insert into pocket (id, type, name, slug, magic, max_capacity, max_volume, weight) values (nextval('hibernate_sequence'), 0, 'Coins', 'coins', false, 6, 1/5, 1);
insert into pocket (id, type, name, slug, magic, max_capacity, max_volume, weight) values (nextval('hibernate_sequence'), 1, 'Backpack', 'backpack', false, 30, 1, 5);
insert into pocket (id, type, name, slug, magic, max_capacity, max_volume, weight) values (nextval('hibernate_sequence'), 2, 'Haversack', 'haversack', true, 120, 12, 5);
insert into pocket (id, type, name, slug, magic, max_capacity, max_volume, weight) values (nextval('hibernate_sequence'), 1, 'Backpack', 'backpack', false, 30, 1, 5);

insert into pocket_item (id, pocket_id, description, quantity, value, weight) values (nextval('hibernate_sequence'), 2, 'Rations', 10, null, null);