-- Insertar categorías de productos
INSERT INTO category (name) VALUES ('Electronics');
INSERT INTO category (name) VALUES ('Clothing');
INSERT INTO category (name) VALUES ('Books');

INSERT INTO role (name) VALUES ('ADMIN'), ('CUSTOMER'), ('SELLER');

-- Insertar productos
INSERT INTO product (name, description, price, stock, category_id)
VALUES
('Laptop', 'High performance laptop', 1200.00, 10, 1),
('Smartphone', 'Latest model smartphone', 800.00, 20, 1),
('T-shirt', 'Comfortable cotton t-shirt', 25.00, 100, 2),
('Jeans', 'Stylish denim jeans', 50.00, 50, 2),
('Java Programming Book', 'Learn Java with this comprehensive guide', 30.00, 200, 3),
('Python Programming Book', 'Master Python programming with this book', 35.00, 150, 3);

UPDATE product SET enabled = 0 WHERE id = 6;

-- Insertar usuarios
INSERT INTO customer (username, password, email, enabled, created_at)
VALUES
('john_doe', '$2a$10$OJ/Sf.WtAJqGyBgpn3kix.bRT7OXmEFF4LBSMjb.KSHne.1aFVq4W', 'john@example.com', TRUE, NOW()),
('jane_smith', '$2a$10$K/geCZgSsPGpXlTLg/N5AOgj3YIVSDkk5nX9aJCCla.bdciJCHf4u', 'jane@example.com', TRUE, NOW()),
('admin', '$2a$10$tWDdJRPcuJ1lAzcC5TKlH.PCAMkoqv4tc1QmUnY//ganc7AfBckHG', 'admin@example.com', TRUE, NOW()),
('seller', '$2a$10$f/h6miuZwMG4BtBWzdrxYedcXW1jKqRIR42FRSy8j.guKlX2bRkEG', 'seller@example.com', TRUE, NOW()),
('customerDisabled', '$2a$10$f/h6miuZwMG4BtBWzdrxYedcXW1jKqRIR42FRSy8j.guKlX2bRkEG', 'customerDisabled@example.com', FALSE, NOW());
-- password123
 -- password456
 -- adminpassword
 -- sellerpassword

-- Asignar roles a usuarios
INSERT INTO customer_roles (customer_id, role_id) VALUES
(1, 2), -- john_doe CUSTMER
(2, 2), -- jane_smith CUSTOMER
(3, 1), -- admin ADMIN
(3, 2), -- admin CUSTOMER
(4, 2), -- seller CUSTOMER
(4, 3); -- seller SELLER

-- Insertar carrito de compras para los usuarios
INSERT INTO cart (customer_id)
VALUES
(1),  -- El carrito de John Doe
(2);  -- El carrito de Jane Smith

INSERT INTO cart_product (cart_id, product_id, quantity)
VALUES
(1, 1, 5);  -- El carrito de John Doe

-- Insertar pedidos
INSERT INTO order_ (customer_id, total_price, order_status)
VALUES
(1, 850.00, 'PENDING'),  -- Pedido de John Doe
(2, 80.00, 'COMPLETED');  -- Pedido de Jane Smith

-- Insertar detalles de pedidos
INSERT INTO order_detail (order_id, product_id, quantity, price)
VALUES
(1, 2, 1, 800.00),  -- Smartphone para John Doe
(1, 3, 2, 25.00),   -- 2 T-shirts para John Doe
(2, 4, 1, 50.00),   -- 1 Jeans para Jane Smith
(2, 5, 1, 30.00);   -- 1 Java Programming Book para Jane Smith
