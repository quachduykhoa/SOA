-- ============================================================================
--  DỮ LIỆU MẪU CHO 4 BÀI THỰC HÀNH: TH1 (auth) - TH3 (product) - TH4 (order) - TH5 (report)
--  Chạy trong Supabase SQL Editor (role postgres).
--
--  !!! CẢNH BÁO: script XOÁ SẠCH dữ liệu hiện có của các bảng bên dưới
--      rồi nạp lại, để id bắt đầu từ 1 và số liệu khớp nhau hoàn toàn.
-- ============================================================================

-- Đảm bảo các schema tồn tại
CREATE SCHEMA IF NOT EXISTS auth_service;
CREATE SCHEMA IF NOT EXISTS products_service;
CREATE SCHEMA IF NOT EXISTS orders_service;
CREATE SCHEMA IF NOT EXISTS report_service;

-- Tạo sẵn bảng cho TH5 (phòng trường hợp Hibernate chưa kịp auto-create)
CREATE TABLE IF NOT EXISTS report_service.orders_reports (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL,
    total_revenue DECIMAL(10, 2),
    total_cost DECIMAL(10, 2),
    total_profit DECIMAL(10, 2)
);

CREATE TABLE IF NOT EXISTS report_service.product_reports (
    id SERIAL PRIMARY KEY,
    order_report_id INT REFERENCES report_service.orders_reports(id) ON DELETE CASCADE,
    product_id INT NOT NULL,
    total_sold INT,
    revenue DECIMAL(10, 2),
    cost DECIMAL(10, 2),
    profit DECIMAL(10, 2)
);

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
-- 2) TH3 - products_service.products   (id sẽ là 1..5)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE products_service.products RESTART IDENTITY CASCADE;

INSERT INTO products_service.products (name, description, price, quantity, created_at, updated_at) VALUES
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

-- ----------------------------------------------------------------------------
-- 4) TH5 - report_service.orders_reports + product_reports
--    - Khớp hoàn toàn với đơn hàng 1 và đơn hàng 2 ở trên
--    - Chi phí (cost) giả định là giá vốn (~60-70% giá bán)
--    - profit = revenue - cost
-- ----------------------------------------------------------------------------
TRUNCATE TABLE report_service.product_reports, report_service.orders_reports
  RESTART IDENTITY CASCADE;

-- Báo cáo cho Đơn hàng 1 (total_revenue = 570,000 khớp đơn 1)
INSERT INTO report_service.orders_reports (order_id, total_revenue, total_cost, total_profit) VALUES
  (1, 570000.00, 380000.00, 190000.00),
  (2, 1210000.00, 820000.00, 390000.00);

-- Chi tiết sản phẩm trong báo cáo:
-- Báo cáo 1 (đơn 1): Áo thun nam (id 1, bán 2 cái) & Mũ lưỡi trai (id 5, bán 3 cái)
-- Báo cáo 2 (đơn 2): Giày sneaker (id 3, bán 1 cái) & Balo laptop (id 4, bán 1 cái)
INSERT INTO report_service.product_reports 
  (order_report_id, product_id, total_sold, revenue, cost, profit) VALUES
  -- Thuộc order_report_id = 1
  (1, 1, 2, 300000.00, 200000.00, 100000.00),
  (1, 5, 3, 270000.00, 180000.00,  90000.00),
  -- Thuộc order_report_id = 2
  (2, 3, 1, 890000.00, 600000.00, 290000.00),
  (2, 4, 1, 320000.00, 220000.00, 100000.00);

COMMIT;

-- ============================================================================
--  KIỂM TRA SAU KHI CHẠY
-- ============================================================================

-- Thống kê số lượng dòng ở cả 4 service
SELECT '1. auth (users)' AS service_table, count(*) FROM auth_service.users
UNION ALL SELECT '2. products', count(*) FROM products_service.products
UNION ALL SELECT '3. orders', count(*) FROM orders_service.orders
UNION ALL SELECT '3. order_items', count(*) FROM orders_service.order_items
UNION ALL SELECT '4. orders_reports', count(*) FROM report_service.orders_reports
UNION ALL SELECT '4. product_reports', count(*) FROM report_service.product_reports;

-- 1) Đối chiếu total_amount của TH4 với tổng chi tiết order_items
SELECT o.id AS order_id,
       o.customer_name,
       o.total_amount,
       SUM(i.total_price)                    AS tinh_lai_tu_items,
       (o.total_amount = SUM(i.total_price)) AS khop_th4
FROM orders_service.orders o
JOIN orders_service.order_items i ON i.order_id = o.id
GROUP BY o.id, o.customer_name, o.total_amount
ORDER BY o.id;

-- 2) Đối chiếu total_revenue của TH5 với chi tiết product_reports và đơn hàng TH4
SELECT r.id AS report_id,
       r.order_id,
       o.total_amount                        AS tien_don_hang_th4,
       r.total_revenue                       AS doanh_thu_bao_cao,
       SUM(p.revenue)                        AS tong_doanh_thu_sp,
       (r.total_revenue = SUM(p.revenue) AND r.total_revenue = o.total_amount) AS khop_doanh_thu,
       r.total_cost                          AS tong_chi_phi,
       SUM(p.cost)                           AS tong_chi_phi_sp,
       (r.total_cost = SUM(p.cost))          AS khop_chi_phi,
       r.total_profit                        AS loi_nhuan
FROM report_service.orders_reports r
JOIN report_service.product_reports p ON p.order_report_id = r.id
JOIN orders_service.orders o ON o.id = r.order_id
GROUP BY r.id, r.order_id, o.total_amount, r.total_revenue, r.total_cost, r.total_profit
ORDER BY r.id;
