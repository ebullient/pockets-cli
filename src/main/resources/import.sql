
INSERT INTO Profile (id, slug, config)
VALUES
  (1, 'default', '{"description":"dummy description"}'),
  (2, 'test-pf2e', '{"preset":"pf2e"}'),
  (3, 'test-5e', '{"preset":"dnd5e"}');
ALTER SEQUENCE Profile_seq RESTART WITH 4;

INSERT INTO Pocket (id, profile_id, slug, name, emoji, pocketDetails)
VALUES
  (1, 1, 'pouch',     'Pouch',    '💰', '{"refId": "pouch"}'),
  (2, 2, 'backpack',  'Backpack', '🤪', '{"refId": "backpack"}'),
  (3, 3, 'coins',     'Coins',    '🤪', '{"refId": "pouch"}'),
  (4, 3, 'backpack',  'Backpack', '🤪', '{"refId": "backpack"}'),
  (5, 3, 'haversack', 'Haversack','🤪', '{"refId": "handy-haversack"}');
ALTER SEQUENCE Pocket_seq RESTART WITH 6;

INSERT INTO Item (id, profile_id, slug, name, itemDetails)
VALUES
  (1, 2, 'rations',       'Rations', '{"refId": "rations"}'),
  (2, 3, 'rations-1-day', 'Rations', '{"refId": "rations-1-day"}');
ALTER SEQUENCE Item_seq RESTART WITH 3;

INSERT INTO PocketItem (id, pocket_id, item_id, slug, quantity, cumulativeValue, cumulativeWeight, cumulativeBulk)
VALUES
  (1, 2, 1, 'rations',        2,  80,    0, 0),
  (2, 4, 2, 'rations-1-day', 10, 500, 20.0, 0);
ALTER SEQUENCE PocketItem_seq RESTART WITH 3;

INSERT INTO PocketCurrency (id, pocket_id, currency, quantity, baseValue, unitConversion)
VALUES
  (1, 2, 'gp', 10, 1000, 100),
  (2, 4, 'gp',  2, 200,   100);
ALTER SEQUENCE PocketCurrency_seq RESTART WITH 3;

INSERT INTO Journal (id, profile_id, datetime, memo, changes, uuid)
VALUES
  (1, 3, '000-230-321321', 'Create pockets', '[{"type":"CREATE","itemType":"POCKET","itemId":"coins","created":{"slug":"coins","name":"Coins","emoji":"🤪","pocketDetails":{"refId":"pouch"}}},{"type":"CREATE","itemType":"POCKET","itemId":"backpack","created":{"slug":"backpack","name":"Backpack","emoji":"🤪","pocketDetails":{"refId":"backpack"}}}]', '059beae7-32da-4d73-af61-0046bc8393fb'),
  (2, 3, '000-230-321322', 'Add rations to backpack',   '[{"type":"CREATE","itemType":"ITEM","itemId":"rations-1-day","created":{"slug":"rations-1-day","name":"Rations (1 day)","itemDetails":{"refId":"rations-1-day"}}},{"type":"ADD","itemType":"ITEM","pocketId":"backpack","itemId":"rations-1-day","quantity":10}]', '73cf4e2f-850e-43f8-b1fe-59d7dde8d104');
ALTER SEQUENCE Journal_seq RESTART WITH 3;
