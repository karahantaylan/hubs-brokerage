-- Müşteri varlıkları (TRY ve hisse senetleri)
INSERT INTO asset (customer_id, asset_name, size, usable_size) VALUES
                                                                   ('cust1', 'TRY', 100000.0, 100000.0),  -- TRY bakiyesi
                                                                   ('cust1', 'AAPL', 100.0, 100.0),       -- Apple hissesi
                                                                   ('cust1', 'TSLA', 50.0, 50.0),         -- Tesla hissesi
                                                                   ('cust2', 'TRY', 50000.0, 50000.0),
                                                                   ('cust2', 'MSFT', 75.0, 75.0);

-- Örnek emirler
INSERT INTO orders (customer_id, asset_name, side, size, price, status, created_at) VALUES
                                                                                        ('cust1', 'AAPL', 'BUY', 10, 150.50, 'PENDING', CURRENT_TIMESTAMP),
                                                                                        ('cust1', 'TSLA', 'SELL', 5, 300.75, 'PENDING', CURRENT_TIMESTAMP),
                                                                                        ('cust2', 'MSFT', 'BUY', 15, 250.00, 'MATCHED', '2024-01-01 12:00:00');
