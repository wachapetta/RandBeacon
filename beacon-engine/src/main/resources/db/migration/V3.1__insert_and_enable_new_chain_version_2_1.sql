UPDATE chain set active = 0 where chain_index =1;


-- we have two options:
-- 1) inserting a chain update uri and pulse versions, without interruption
INSERT INTO chain(active,chain_index,cipher_suite,cipher_suite_description,period,version_uri, version_pulse)
SELECT 1,1,0,'SHA512 hashing and RSA signatures with PKCSv1.5',60000,'2.1','2.1' WHERE (select count(id) from pulse) = 0;

--OR

-- 2) inserting the second chain with news uri and pulse versions
INSERT INTO chain(active,chain_index,cipher_suite,cipher_suite_description,period,version_uri, version_pulse)
SELECT 0,2,0,'SHA512 hashing and RSA signatures with PKCSv1.5',60000,'2.1','2.1' WHERE (select count(id) from pulse) > 0;

--finally
--UPDATE chain set active = 1 where chain_index = 2;