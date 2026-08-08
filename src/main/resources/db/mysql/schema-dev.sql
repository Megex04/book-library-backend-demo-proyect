
-- Insertar roles predefinidos
INSERT IGNORE INTO roles (name, description) VALUES
('ROLE_ADMIN', 'Administrador del sistema con acceso completo'),
('ROLE_LIBRARIAN', 'Bibliotecario con acceso a gestión de libros y préstamos'),
('ROLE_MEMBER', 'Miembro registrado con acceso a préstamos y reservas'),
('ROLE_VISITOR', 'Visitante con acceso limitado solo a consulta');