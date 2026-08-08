-- =====================================================================
-- Script de datos de prueba: Author, Category, Book (+ tablas puente)
-- =====================================================================
-- IMPORTANTE: este script está construido a partir de las entidades JPA
-- reales (Author.java, Category.java, Book.java), NO desde schema.sql.
-- El proyecto usa spring.jpa.hibernate.ddl-auto=update y
-- spring.sql.init.mode=never, así que es Hibernate quien crea las tablas
-- reales en tu base de datos a partir de las anotaciones @Entity/@Column,
-- y schema.sql (que tiene columnas distintas) nunca se ejecuta.
--
-- Orden de inserción: categories -> authors -> books -> tablas puente,
-- para respetar las FKs (parent_id en categories, book_authors,
-- book_categories).
-- =====================================================================

-- ---------------------------------------------------------------------
-- CATEGORIES (12 filas: 6 raíz + 6 subcategorías con parent_id)
-- ---------------------------------------------------------------------
INSERT INTO categories (id, name, description, parent_id, created_at, updated_at) VALUES
(1, 'Ficción', 'Obras narrativas de carácter imaginativo', NULL, NOW(), NOW()),
(2, 'Ciencia Ficción', 'Ficción basada en avances científicos y tecnológicos especulativos', 1, NOW(), NOW()),
(3, 'Fantasía', 'Ficción ambientada en mundos imaginarios con elementos mágicos', 1, NOW(), NOW()),
(4, 'No Ficción', 'Obras basadas en hechos y eventos reales', NULL, NOW(), NOW()),
(5, 'Biografía', 'Relatos de la vida de una persona real', 4, NOW(), NOW()),
(6, 'Historia', 'Obras centradas en el estudio de hechos históricos', 4, NOW(), NOW()),
(7, 'Ciencia', 'Divulgación y estudio de las ciencias naturales y exactas', NULL, NOW(), NOW()),
(8, 'Tecnología', 'Obras sobre informática, ingeniería e innovación', 7, NOW(), NOW()),
(9, 'Literatura Infantil', 'Obras dirigidas a lectores niños', NULL, NOW(), NOW()),
(10, 'Poesía', 'Obras en verso y composiciones líricas', NULL, NOW(), NOW()),
(11, 'Misterio y Suspenso', 'Narrativa centrada en la intriga y la resolución de enigmas', 1, NOW(), NOW()),
(12, 'Filosofía', 'Obras de reflexión y pensamiento filosófico', 4, NOW(), NOW());

-- ---------------------------------------------------------------------
-- AUTHORS (12 filas)
-- ---------------------------------------------------------------------
INSERT INTO authors (id, name, biography, birth_year, death_year, nationality, created_at, updated_at) VALUES
(1, 'Gabriel García Márquez', 'Escritor y periodista colombiano, premio Nobel de Literatura en 1982, máximo exponente del realismo mágico.', 1927, 2014, 'Colombiana', NOW(), NOW()),
(2, 'Isabel Allende', 'Escritora chilena conocida por obras que mezclan realismo mágico con temas históricos y sociales.', 1942, NULL, 'Chilena', NOW(), NOW()),
(3, 'George Orwell', 'Novelista y ensayista británico, autor de obras distópicas influyentes sobre totalitarismo y vigilancia.', 1903, 1950, 'Británica', NOW(), NOW()),
(4, 'J.K. Rowling', 'Autora británica, creadora de la saga de fantasía Harry Potter.', 1965, NULL, 'Británica', NOW(), NOW()),
(5, 'J.R.R. Tolkien', 'Filólogo y escritor británico, padre de la fantasía moderna con El Señor de los Anillos.', 1892, 1973, 'Británica', NOW(), NOW()),
(6, 'Jorge Luis Borges', 'Escritor argentino, referente universal de la literatura fantástica y el ensayo.', 1899, 1986, 'Argentina', NOW(), NOW()),
(7, 'Agatha Christie', 'Novelista británica, una de las autoras más vendidas de la historia en el género policial.', 1890, 1976, 'Británica', NOW(), NOW()),
(8, 'Stephen Hawking', 'Físico teórico y divulgador científico británico, conocido por sus estudios sobre agujeros negros.', 1942, 2018, 'Británica', NOW(), NOW()),
(9, 'Yuval Noah Harari', 'Historiador y escritor israelí, autor de obras de divulgación sobre historia y futuro de la humanidad.', 1976, NULL, 'Israelí', NOW(), NOW()),
(10, 'Mario Vargas Llosa', 'Escritor peruano-español, premio Nobel de Literatura en 2010.', 1936, NULL, 'Peruana', NOW(), NOW()),
(11, 'Roald Dahl', 'Escritor británico de origen noruego, célebre por sus cuentos infantiles.', 1916, 1990, 'Británica', NOW(), NOW()),
(12, 'Pablo Neruda', 'Poeta chileno, premio Nobel de Literatura en 1971, uno de los más grandes poetas en español.', 1904, 1973, 'Chilena', NOW(), NOW());

-- ---------------------------------------------------------------------
-- BOOKS (15 filas)
-- ---------------------------------------------------------------------
INSERT INTO books (id, title, isbn, publication_year, edition, publisher, language, physical_location, total_copies, available_copies, description, cover_image_url, created_at, updated_at) VALUES
(1, 'Cien años de soledad', '978-0307474728', 1967, '1ra edición', 'Editorial Sudamericana', 'Español', 'Estante A1-01', 5, 5, 'La historia de la familia Buendía a lo largo de siete generaciones en el pueblo ficticio de Macondo.', 'https://covers.example.com/cien-anos-soledad.jpg', NOW(), NOW()),
(2, 'La casa de los espíritus', '978-8401341830', 1982, '1ra edición', 'Plaza & Janés', 'Español', 'Estante A1-02', 4, 4, 'Saga familiar chilena narrada a través de varias generaciones y hechos sobrenaturales.', 'https://covers.example.com/casa-espiritus.jpg', NOW(), NOW()),
(3, '1984', '978-0451524935', 1949, '1ra edición', 'Secker & Warburg', 'Español', 'Estante B2-05', 6, 6, 'Novela distópica sobre un régimen totalitario que vigila y controla cada aspecto de la vida de sus ciudadanos.', 'https://covers.example.com/1984.jpg', NOW(), NOW()),
(4, 'Rebelión en la granja', '978-0451526342', 1945, '1ra edición', 'Secker & Warburg', 'Español', 'Estante B2-06', 4, 4, 'Fábula satírica sobre una granja cuyos animales se rebelan contra los humanos.', 'https://covers.example.com/rebelion-granja.jpg', NOW(), NOW()),
(5, 'Harry Potter y la piedra filosofal', '978-8478884452', 1997, '1ra edición', 'Bloomsbury', 'Español', 'Estante C3-01', 8, 8, 'Primer libro de la saga sobre el joven mago Harry Potter y su ingreso a Hogwarts.', 'https://covers.example.com/hp-piedra-filosofal.jpg', NOW(), NOW()),
(6, 'El señor de los anillos: La comunidad del anillo', '978-8445073905', 1954, '1ra edición', 'Allen & Unwin', 'Español', 'Estante C3-10', 5, 5, 'Primer volumen de la trilogía sobre la lucha por destruir el Anillo Único.', 'https://covers.example.com/lotr-comunidad.jpg', NOW(), NOW()),
(7, 'El Hobbit', '978-8445073912', 1937, '1ra edición', 'Allen & Unwin', 'Español', 'Estante C3-11', 6, 6, 'Las aventuras de Bilbo Bolsón en su viaje hacia la Montaña Solitaria.', 'https://covers.example.com/el-hobbit.jpg', NOW(), NOW()),
(8, 'Ficciones', '978-8420633111', 1944, '1ra edición', 'Sur', 'Español', 'Estante A2-04', 3, 3, 'Colección de cuentos fantásticos y filosóficos, obra cumbre de Borges.', 'https://covers.example.com/ficciones.jpg', NOW(), NOW()),
(9, 'Asesinato en el Orient Express', '978-8408175774', 1934, '1ra edición', 'Collins Crime Club', 'Español', 'Estante D4-02', 4, 4, 'El detective Hércules Poirot investiga un crimen a bordo de un tren.', 'https://covers.example.com/orient-express.jpg', NOW(), NOW()),
(10, 'Breve historia del tiempo', '978-8484505948', 1988, '1ra edición', 'Bantam Books', 'Español', 'Estante E5-01', 3, 3, 'Divulgación científica sobre cosmología, agujeros negros y el origen del universo.', 'https://covers.example.com/breve-historia-tiempo.jpg', NOW(), NOW()),
(11, 'Sapiens: De animales a dioses', '978-8499926223', 2011, '1ra edición', 'Debate', 'Español', 'Estante E5-10', 5, 5, 'Un recorrido por la historia de la humanidad desde la evolución hasta la actualidad.', 'https://covers.example.com/sapiens.jpg', NOW(), NOW()),
(12, 'La ciudad y los perros', '978-8420471839', 1963, '1ra edición', 'Seix Barral', 'Español', 'Estante A1-08', 3, 3, 'Novela ambientada en un colegio militar de Lima que retrata la violencia y la corrupción juvenil.', 'https://covers.example.com/ciudad-perros.jpg', NOW(), NOW()),
(13, 'Charlie y la fábrica de chocolate', '978-8420471051', 1964, '1ra edición', 'Alfred A. Knopf', 'Español', 'Estante F6-03', 6, 6, 'La aventura de Charlie Bucket dentro de la fantástica fábrica de Willy Wonka.', 'https://covers.example.com/charlie-fabrica.jpg', NOW(), NOW()),
(14, 'Veinte poemas de amor y una canción desesperada', '978-8437604739', 1924, '1ra edición', 'Editorial Nascimento', 'Español', 'Estante G7-01', 4, 4, 'Uno de los poemarios más leídos en lengua española, sobre el amor y la melancolía.', 'https://covers.example.com/veinte-poemas.jpg', NOW(), NOW()),
(15, 'Muerte en el Nilo', '978-8408175781', 1937, '1ra edición', 'Collins Crime Club', 'Español', 'Estante D4-03', 4, 4, 'Nuevo caso de Hércules Poirot durante un crucero por el río Nilo.', 'https://covers.example.com/muerte-nilo.jpg', NOW(), NOW());

-- ---------------------------------------------------------------------
-- BOOK_AUTHORS (relación muchos-a-muchos)
-- ---------------------------------------------------------------------
INSERT INTO book_authors (book_id, author_id) VALUES
(1, 1),   -- Cien años de soledad -> García Márquez
(2, 2),   -- La casa de los espíritus -> Isabel Allende
(3, 3),   -- 1984 -> Orwell
(4, 3),   -- Rebelión en la granja -> Orwell
(5, 4),   -- Harry Potter -> Rowling
(6, 5),   -- LOTR -> Tolkien
(7, 5),   -- El Hobbit -> Tolkien
(8, 6),   -- Ficciones -> Borges
(9, 7),   -- Orient Express -> Agatha Christie
(10, 8),  -- Breve historia del tiempo -> Hawking
(11, 9),  -- Sapiens -> Harari
(12, 10), -- La ciudad y los perros -> Vargas Llosa
(13, 11), -- Charlie y la fábrica de chocolate -> Roald Dahl
(14, 12), -- Veinte poemas -> Neruda
(15, 7);  -- Muerte en el Nilo -> Agatha Christie

-- ---------------------------------------------------------------------
-- BOOK_CATEGORIES (relación muchos-a-muchos)
-- ---------------------------------------------------------------------
INSERT INTO book_categories (book_id, category_id) VALUES
(1, 1),   -- Cien años de soledad -> Ficción
(2, 1),   -- La casa de los espíritus -> Ficción
(2, 3),   -- La casa de los espíritus -> Fantasía
(3, 2),   -- 1984 -> Ciencia Ficción
(4, 1),   -- Rebelión en la granja -> Ficción
(5, 3),   -- Harry Potter -> Fantasía
(6, 3),   -- LOTR -> Fantasía
(7, 3),   -- El Hobbit -> Fantasía
(7, 9),   -- El Hobbit -> Literatura Infantil
(8, 1),   -- Ficciones -> Ficción
(9, 11),  -- Orient Express -> Misterio y Suspenso
(10, 7),  -- Breve historia del tiempo -> Ciencia
(11, 6),  -- Sapiens -> Historia
(11, 7),  -- Sapiens -> Ciencia
(12, 1),  -- La ciudad y los perros -> Ficción
(13, 9),  -- Charlie y la fábrica de chocolate -> Literatura Infantil
(14, 10), -- Veinte poemas -> Poesía
(15, 11); -- Muerte en el Nilo -> Misterio y Suspenso
