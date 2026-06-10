# UADE Exam Manager

Aplicación web monolítica en **Java 17 + Spring Boot + Thymeleaf** para gestionar estudiantes, formar grupos y generar exámenes en PDF con formato UADE.

## Funcionalidades

- Gestión de estudiantes:
  - Alta, edición y eliminación
  - Importación por texto (copiar/pegar)
  - Importación por CSV (`Nombre Apellido,Legajo`)
- Formación de grupos:
  - Aleatoria con animación tipo ruleta (frontend JS/CSS)
  - Manual por asignación de estudiante a grupo
- Gestión de temas:
  - Alta y baja de temas
  - Asignación aleatoria configurable
  - Opción de permitir/no permitir repetición entre grupos
- Configuración de examen:
  - Materia, docentes, fecha, cantidad de hojas
  - Subida de cabecera personalizada
  - Vista previa de cabecera
- Generación de PDFs:
  - Un PDF por grupo
  - Incluye integrantes, temas, metadata del examen y cláusulas de honestidad académica

## Stack técnico

- Java 17+
- Spring Boot 3
- Spring MVC + Thymeleaf + Bootstrap 5
- Spring Data JPA (patrón Repository)
- H2 file-based (persistente)
- OpenPDF para generación de PDF

## Ejecución local

### Requisitos

- Java 17 o superior
- Maven 3.9+

### Pasos

```bash
cd /home/ubuntu/uade_exam_manager
mvn clean spring-boot:run
```

Abrir en navegador:
- App: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console`

### Datos de H2

- JDBC URL: `jdbc:h2:file:./data/uade_exam_db;AUTO_SERVER=TRUE`
- User: `sa`
- Password: *(vacío)*

## Estructura

- `controller/`: controladores MVC
- `service/`: reglas de negocio
- `repository/`: acceso a datos JPA
- `entity/`: entidades de dominio
- `templates/`: vistas Thymeleaf
- `static/`: CSS/JS/imagenes

## Notas de diseño

- Arquitectura monolítica MVC con separación por capas para respetar SOLID.
- Validaciones en backend y restricciones en frontend.
- Manejo centralizado de errores con `@ControllerAdvice`.
- Base H2 persistente por archivo para conservar datos entre reinicios.
