USE appdb;
DROP PROCEDURE IF EXISTS getMerchantDetailByMerchant_Id;
DELIMITER $$
CREATE PROCEDURE getMerchantDetailByMerchant_Id(IN merchant_id_param VARCHAR(15))
BEGIN
    CREATE TEMPORARY TABLE temp_merchant AS
    SELECT * FROM merchant WHERE merchant_id = merchant_id_param;
    SELECT a.id, a.created_datetime, a.modified_datetime, a.dba_name, 
    a.merchant_name, a.merchant_id, a.status_id, b.status_name, c.permission_id,
    d.permission_name
    FROM temp_merchant a
    INNER JOIN user_status_lookup b ON a.status_id = b.status_id
    INNER JOIN user_permission c ON a.id = c.user_id
    INNER JOIN permission d ON c.permission_id = d.permission_id;
    DROP TEMPORARY TABLE IF EXISTS temp_merchant;
END$$

