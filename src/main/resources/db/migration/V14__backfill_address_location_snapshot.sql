UPDATE tb_address a
SET city_name = c.name,
    state_uf = s.sgl,
    country_code = COALESCE(co.sgl, 'BR'),
    ibge_code = c.ibge_code
FROM tb_city c
JOIN tb_state s ON s.id = c.state_id
LEFT JOIN tb_country co ON co.id = s.country_id
WHERE a.city_id = c.id
  AND a.city_name IS NULL;

UPDATE tb_address
SET number = 'S/N'
WHERE number IS NULL OR btrim(number) = '';

INSERT INTO tb_order_shipping_address (
    order_id,
    zip_code,
    street,
    number,
    complement,
    district,
    city_name,
    state_uf,
    country_code,
    ibge_code,
    original_address_id,
    snapshot_at
)
SELECT
    o.id,
    a.zip_code,
    a.street,
    COALESCE(NULLIF(btrim(a.number), ''), 'S/N'),
    a.complement,
    a.district,
    a.city_name,
    a.state_uf,
    COALESCE(a.country_code, 'BR'),
    a.ibge_code,
    a.id,
    COALESCE(o.order_date, CURRENT_TIMESTAMP)
FROM tb_order o
JOIN tb_address a ON a.id = o.delivery_adress_id
LEFT JOIN tb_order_shipping_address osa ON osa.order_id = o.id
WHERE osa.order_id IS NULL
  AND a.city_name IS NOT NULL
  AND a.state_uf IS NOT NULL;
