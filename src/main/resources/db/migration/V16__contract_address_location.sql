DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM tb_address
        WHERE city_name IS NULL
           OR btrim(city_name) = ''
           OR state_uf IS NULL
           OR btrim(state_uf) = ''
           OR country_code IS NULL
           OR btrim(country_code) = ''
           OR number IS NULL
           OR btrim(number) = ''
    ) THEN
        RAISE EXCEPTION 'Cannot contract address location: tb_address has rows without number, city_name, state_uf or country_code';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM tb_order o
        WHERE o.delivery_adress_id IS NOT NULL
          AND NOT EXISTS (
              SELECT 1
              FROM tb_order_shipping_address s
              WHERE s.order_id = o.id
          )
    ) THEN
        RAISE EXCEPTION 'Cannot contract address location: tb_order has delivery addresses without shipping snapshots';
    END IF;
END $$;

ALTER TABLE tb_order DROP CONSTRAINT IF EXISTS fk_order_address;
ALTER TABLE tb_order DROP COLUMN IF EXISTS delivery_adress_id;

ALTER TABLE tb_address DROP CONSTRAINT IF EXISTS fk_address_city;
ALTER TABLE tb_address DROP COLUMN IF EXISTS city_id;

ALTER TABLE tb_address ALTER COLUMN number SET NOT NULL;
ALTER TABLE tb_address ALTER COLUMN city_name SET NOT NULL;
ALTER TABLE tb_address ALTER COLUMN state_uf SET NOT NULL;
ALTER TABLE tb_address ALTER COLUMN country_code SET NOT NULL;
ALTER TABLE tb_address ALTER COLUMN country_code SET DEFAULT 'BR';

DROP TABLE IF EXISTS tb_city;
DROP TABLE IF EXISTS tb_state;
DROP TABLE IF EXISTS tb_country;
