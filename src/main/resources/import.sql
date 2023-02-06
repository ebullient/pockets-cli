DROP SEQUENCE IF EXISTS hibernate_sequence;
CREATE SEQUENCE hibernate_sequence START WITH 1 INCREMENT BY 1;

insert into Profile (id, slug, config)
values
  (nextval('hibernate_sequence'), 'default', '{"description":"dummy description"}'),
  (nextval('hibernate_sequence'), 'test-pf2e', '{"preset":"pf2e"}'),
  (nextval('hibernate_sequence'), 'test-5e', '{"preset":"dnd5e"}');

insert into Pocket (id, profile_id, slug, name, emoji, pocketDetails)
values
  (nextval('hibernate_sequence'), 1, 'pouch',     'Pouch',    '💰', '{"refId": "pouch"}'),
  (nextval('hibernate_sequence'), 2, 'backpack',  'Backpack', '🤪', '{"refId": "backpack"}'),
  (nextval('hibernate_sequence'), 3, 'coins',     'Coins',    '🤪', '{"refId": "pouch"}'),
  (nextval('hibernate_sequence'), 3, 'backpack',  'Backpack', '🤪', '{"refId": "backpack"}'),
  (nextval('hibernate_sequence'), 3, 'haversack', 'Haversack','🤪', '{"refId": "handy-haversack"}');

insert into Item (id, profile_id, slug, name, itemDetails)
values
  (nextval('hibernate_sequence'), 2, 'rations',       'Rations', '{"refId": "rations"}'),
  (nextval('hibernate_sequence'), 3, 'rations-1-day', 'Rations', '{"refId": "rations-1-day"}');

insert into PocketItem (id, pocket_id, item_id, slug, quantity, cumulativeValue, cumulativeWeight, cumulativeBulk)
values
  (nextval('hibernate_sequence'), 5, 9, 'rations',        2,  80,    0, 0),
  (nextval('hibernate_sequence'), 7, 10, 'rations-1-day', 10, 500, 20.0, 0);

insert into PocketCurrency (id, pocket_id, currency, quantity, baseValue, unitConversion)
values
  (nextval('hibernate_sequence'), 5, 'gp', 10, 1000, 100),
  (nextval('hibernate_sequence'), 7, 'gp',  2, 200,   100);

insert into Journal (id, profile_id, datetime, memo, changes, uuid)
values
  (nextval('hibernate_sequence'), 3, '000-230-321321', 'Create pockets', '[{"type":"CREATE","itemType":"POCKET","itemId":"coins","created":{"slug":"coins","name":"Coins","emoji":"🤪","pocketDetails":{"refId":"pouch"}}},{"type":"CREATE","itemType":"POCKET","itemId":"backpack","created":{"slug":"backpack","name":"Backpack","emoji":"🤪","pocketDetails":{"refId":"backpack"}}}]', '059beae7-32da-4d73-af61-0046bc8393fb'),
  (nextval('hibernate_sequence'), 3, '000-230-321322', 'Add rations to backpack',   '[{"type":"CREATE","itemType":"ITEM","itemId":"rations-1-day","created":{"slug":"rations-1-day","name":"Rations (1 day)","itemDetails":{"refId":"rations-1-day"}}},{"type":"ADD","itemType":"ITEM","pocketId":"backpack","itemId":"rations-1-day","quantity":10}]', '73cf4e2f-850e-43f8-b1fe-59d7dde8d104');

