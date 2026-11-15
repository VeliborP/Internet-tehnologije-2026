-- ===================================================================
-- Web Shop - seed data (run after schema.sql)
-- Usage:  mysql -u root -p --default-character-set=utf8mb4 webshop < database/data.sql
--
-- NOTE: passwords below are PLAIN TEXT and work only with
--       app.security.password-encoding=plain (development / test mode).
--       The same data is created automatically on first start when
--       app.seed.enabled=true and the database is empty.
-- ===================================================================

USE webshop;
SET NAMES utf8mb4;

-- Initial accounts: admin / admin123 (ADMIN), kupac / kupac123 (CUSTOMER)
INSERT INTO users (first_name, last_name, username, password, role, enabled) VALUES
    ('Admin', 'Administrator', 'admin', 'admin123', 'ADMIN', b'1'),
    ('Petar', 'Petrović', 'kupac', 'kupac123', 'CUSTOMER', b'1');

INSERT INTO categories (code, name, description, active) VALUES
    ('ELE', 'Elektronika', 'Slušalice, pametni satovi i računarska oprema.', b'1'),
    ('KNJ', 'Knjige', 'Klasici domaće književnosti i priručnici.', b'1'),
    ('KUC', 'Kuća i bašta', 'Sve što čini dom prijatnijim.', b'1'),
    ('SPO', 'Sport', 'Oprema za trening i rekreaciju.', b'1');

INSERT INTO products (code, name, description, image, price, stock_quantity, category_id, active, featured) VALUES
    ('ELE-001', 'Bežične slušalice Aurora', 'Bluetooth slušalice sa aktivnim poništavanjem buke i baterijom do 30 sati.', '/images/categories/electronics.svg', 7990.00, 25, (SELECT id FROM categories WHERE code = 'ELE'), b'1', b'1'),
    ('ELE-002', 'Pametni sat Puls 2', 'Praćenje pulsa, sna i treninga, vodootporan do 50 metara.', '/images/categories/electronics.svg', 14990.00, 12, (SELECT id FROM categories WHERE code = 'ELE'), b'1', b'1'),
    ('ELE-003', 'Mehanička tastatura Škljoc', 'Kompaktna mehanička tastatura sa pozadinskim osvetljenjem i srpskim rasporedom.', '/images/categories/electronics.svg', 9490.00, 8, (SELECT id FROM categories WHERE code = 'ELE'), b'1', b'0'),
    ('KNJ-001', 'Na Drini ćuprija', 'Roman Ive Andrića, tvrdi povez.', '/images/categories/books.svg', 1199.00, 40, (SELECT id FROM categories WHERE code = 'KNJ'), b'1', b'1'),
    ('KNJ-002', 'Derviš i smrt', 'Roman Meše Selimovića, džepno izdanje.', '/images/categories/books.svg', 1099.00, 30, (SELECT id FROM categories WHERE code = 'KNJ'), b'1', b'0'),
    ('KNJ-003', 'Rečnik srpskog jezika', 'Jednotomni rečnik sa više od 85.000 odrednica.', '/images/categories/books.svg', 4500.00, 5, (SELECT id FROM categories WHERE code = 'KNJ'), b'1', b'0'),
    ('KUC-001', 'Bakarna džezva', 'Ručno kovana džezva za četiri šoljice kafe.', '/images/categories/home.svg', 1890.00, 50, (SELECT id FROM categories WHERE code = 'KUC'), b'1', b'1'),
    ('KUC-002', 'Set šolja Šumadija', 'Šest keramičkih šolja sa tradicionalnim motivima.', '/images/categories/home.svg', 2450.00, 18, (SELECT id FROM categories WHERE code = 'KUC'), b'1', b'0'),
    ('KUC-003', 'Baštenske makaze', 'Makaze za orezivanje sa čeličnim sečivom i ergonomskom drškom.', '/images/categories/home.svg', 1350.00, 0, (SELECT id FROM categories WHERE code = 'KUC'), b'1', b'0'),
    ('SPO-001', 'Fudbalska lopta Đerdap', 'Lopta veličine 5 za travnate i veštačke terene.', '/images/categories/sport.svg', 3290.00, 22, (SELECT id FROM categories WHERE code = 'SPO'), b'1', b'0'),
    ('SPO-002', 'Prostirka za jogu', 'Neklizajuća prostirka debljine 6 mm sa trakom za nošenje.', '/images/categories/sport.svg', 2190.00, 35, (SELECT id FROM categories WHERE code = 'SPO'), b'1', b'1'),
    ('SPO-003', 'Čelična flašica 0,75 l', 'Termo flašica koja održava temperaturu pića do 12 sati.', '/images/categories/sport.svg', 1490.00, 60, (SELECT id FROM categories WHERE code = 'SPO'), b'1', b'0');

-- Every user has a cart.
INSERT INTO carts (user_id, created_at, updated_at)
SELECT id, NOW(6), NOW(6) FROM users;
