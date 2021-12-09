DROP SEQUENCE IF EXISTS hibernate_sequence;
CREATE SEQUENCE hibernate_sequence START 1;

insert into Pocket (id, type, name, magic, max_capacity, max_volume, weight) values (nextval('hibernate_sequence'), 1, 'Backpack', false, 30, 1, 5);
insert into Pocket (id, type, name, magic, max_capacity, max_volume, weight) values (nextval('hibernate_sequence'), 0, 'Coins', false, 6, 1/5, 1);
insert into Pocket (id, type, name, magic, max_capacity, max_volume, weight) values (nextval('hibernate_sequence'), 2, 'Haversack', true, 120, 12, 5);
