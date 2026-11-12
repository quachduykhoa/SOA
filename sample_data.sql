-- ============================================================================
--  DỮ LIỆU MẪU CHO 3 BÀI THỰC HÀNH: TH1 (auth) - TH3 (product) - TH4 (order)
--  Chạy trong Supabase SQL Editor (role postgres).
--
--  !!! CẢNH BÁO: script XOÁ SẠCH dữ liệu hiện có của 3 bảng bên dưới
--      rồi nạp lại, để id bắt đầu từ 1 và tổng tiền khớp nhau.
-- ============================================================================

BEGIN;

-- ----------------------------------------------------------------------------
-- 1) TH1 - auth_service.users
--    Cột thật trên DB: id_user, user_name, password, token
--    password lưu MD5 (TH1 so sánh nguyên văn, KHÔNG băm hộ client)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE auth_service.users RESTART IDENTITY CASCADE;

INSERT INTO auth_service.users (user_name, password) VALUES
  ('admin',      'e10adc3949ba59abbe56e057f20f883e'),  -- 123456
  ('nguyenvana', '5f4dcc3b5aa765d61d8327deb882cf99'),  -- password
  ('tranthib',   'e99a18c428cb38d5f260853678922e03');  -- abc123

-- ----------------------------------------------------------------------------
-- 2) TH3 - products.products   (id sẽ là 1..5)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE products.products RESTART IDENTITY CASCADE;

INSERT INTO products.products (name, description, price, quantity, created_at, updated_at) VALUES
  ('Áo thun nam',     'Cotton 100%, size M-L-XL',        150000.00,  50, now() - interval '10 days', now() - interval '10 days'),
  ('Quần jean nữ',    'Jean slim fit, size 26-32',       450000.00,  30, now() - interval '9 days',  now() - interval '9 days'),
  ('Giày sneaker',    'Da lộn, phối màu trắng xanh',     890000.00,  20, now() - interval '8 days',  now() - interval '8 days'),
  ('Balo laptop',     'Chống nước, ngăn 15.6 inch',      320000.00,  40, now() - interval '7 days',  now() - interval '7 days'),
  ('Mũ lưỡi trai',    'Vải kaki, logo thêu',              90000.00, 100, now() - interval '6 days',  now() - interval '6 days');

-- ----------------------------------------------------------------------------
-- 3) TH4 - orders_service.orders  (id 1..3) + order_items
--    total_amount = tổng total_price của các item (đã tính sẵn cho khớp)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE orders_service.order_items, orders_service.orders
  RESTART IDENTITY CASCADE;

INSERT INTO orders_service.orders (customer_name, customer_email, total_amount, status, created_at, updated_at) VALUES
  ('Nguyễn Văn An',  'an@example.com',      570000.00, 'completed', now() - interval '5 days', now() - interval '4 days'),
  ('Trần Thị Bình',  'binh@example.com',   1210000.00, 'pending',   now() - interval '2 days', now() - interval '2 days'),
  ('Lê Hoàng Cường', 'cuong@example.com',   900000.00, 'cancelled', now() - interval '1 day',  now() - interval '1 day');

INSERT INTO orders_service.order_items
  (order_id, product_id, product_name, quantity, unit_price, total_price) VALUES
  (1, 1, 'Áo thun nam',   2, 150000.00, 300000.00),
  (1, 5, 'Mũ lưỡi trai',  3,  90000.00, 270000.00),
  (2, 3, 'Giày sneaker',  1, 890000.00, 890000.00),
  (2, 4, 'Balo laptop',   1, 320000.00, 320000.00),
  (3, 2, 'Quần jean nữ',  2, 450000.00, 900000.00);

COMMIT;

-- ============================================================================
--  KIỂM TRA SAU KHI CHẠY
-- ============================================================================

-- 3 user, 5 sản phẩm, 3 đơn, 5 chi tiết
SELECT 'users' AS bang, count(*) FROM auth_service.users
UNION ALL SELECT 'products', count(*) FROM products.products
UNION ALL SELECT 'orders', count(*) FROM orders_service.orders
UNION ALL SELECT 'order_items', count(*) FROM orders_service.order_items;

-- Đối chiếu total_amount với tổng chi tiết: cột khop phải là true hết
SELECT o.id,
       o.customer_name,
       o.total_amount,
       SUM(i.total_price)                    AS tinh_lai_tu_items,
       (o.total_amount = SUM(i.total_price)) AS khop
FROM orders_service.orders o
JOIN orders_service.order_items i ON i.order_id = o.id
GROUP BY o.id, o.customer_name, o.total_amount
ORDER BY o.id;
