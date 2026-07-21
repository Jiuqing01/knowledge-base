UPDATE files SET storage_name = 'ed7e93c9-bde7-4fd0-9396-083bb4d16428.zip', storage_path = 'uploads/1/ed7e93c9-bde7-4fd0-9396-083bb4d16428.zip' WHERE id = 8;
UPDATE files SET storage_name = '72ccded6-61ab-40e5-9a61-dd8399b33090.pdf', storage_path = 'uploads/1/72ccded6-61ab-40e5-9a61-dd8399b33090.pdf' WHERE id = 5;
UPDATE files SET storage_name = '0fabf801-978c-491f-b2f9-61c49ec4ae8b.pdf', storage_path = 'uploads/1/0fabf801-978c-491f-b2f9-61c49ec4ae8b.pdf' WHERE id = 6;
UPDATE files SET storage_name = '3fc0ab53-c167-4278-be4d-02ec33af58b9.pdf', storage_path = 'uploads/1/3fc0ab53-c167-4278-be4d-02ec33af58b9.pdf' WHERE id = 2;
UPDATE files SET storage_name = 'd2307b88-9667-4c89-8838-3e893c1fc44b.pdf', storage_path = 'uploads/1/d2307b88-9667-4c89-8838-3e893c1fc44b.pdf' WHERE id = 4;
UPDATE files SET storage_name = '29a8476a-a835-406c-b8d4-29d89c8588f0.pdf', storage_path = 'uploads/1/29a8476a-a835-406c-b8d4-29d89c8588f0.pdf' WHERE id = 1;
UPDATE files SET storage_name = '2b62db9f-b3df-495e-aee0-64783abcb8dd.pdf', storage_path = 'uploads/1/2b62db9f-b3df-495e-aee0-64783abcb8dd.pdf' WHERE id = 3;
UPDATE files SET storage_name = '5da08c7f-970b-4269-b6ad-303d387aa2c5.pdf', storage_path = 'uploads/1/5da08c7f-970b-4269-b6ad-303d387aa2c5.pdf' WHERE id = 9;
UPDATE files SET storage_name = 'f849bc29-d6c9-41ce-86f7-4116bc431210.pdf', storage_path = 'uploads/1/f849bc29-d6c9-41ce-86f7-4116bc431210.pdf' WHERE id = 13;
UPDATE files SET storage_name = '13e419f4-8b33-41ff-b2d3-16e08595642f.docx', storage_path = 'uploads/1/13e419f4-8b33-41ff-b2d3-16e08595642f.docx' WHERE id = 7;
UPDATE files SET storage_name = '7d3dacea-3cd8-4557-8186-2bb3a2199cf8.docx', storage_path = 'uploads/1/7d3dacea-3cd8-4557-8186-2bb3a2199cf8.docx' WHERE id = 10;
UPDATE files SET storage_name = '8e923719-0882-43d3-a8c7-0651056fa8a9.docx', storage_path = 'uploads/1/8e923719-0882-43d3-a8c7-0651056fa8a9.docx' WHERE id = 11;
UPDATE files SET storage_name = 'c293e2ba-8b88-421a-9035-eff01d957677.pdf', storage_path = 'uploads/1/c293e2ba-8b88-421a-9035-eff01d957677.pdf' WHERE id = 12;
UPDATE files SET storage_name = 'ab4a0cf0-ffa6-439c-9f07-482a31bd1a59.docx', storage_path = 'uploads/1/ab4a0cf0-ffa6-439c-9f07-482a31bd1a59.docx' WHERE id = 14;
UPDATE files SET storage_name = 'fb81846e-1b9b-4287-a462-a959166fabc7.docx', storage_path = 'uploads/1/fb81846e-1b9b-4287-a462-a959166fabc7.docx' WHERE id = 15;

SELECT id, original_name, storage_name, storage_path FROM files WHERE owner_id = 1;