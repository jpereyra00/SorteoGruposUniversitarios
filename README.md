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

## Estructura (arquitectura hexagonal)

`exam-core` está organizado en capas siguiendo el patrón ports & adapters:

- `domain/model/`: modelos de negocio puros (POJOs, sin Spring ni JPA)
- `domain/port/in/`: puertos de entrada (use cases)
- `domain/port/out/`: puertos de salida (repositorios, generador de documentos, storage)
- `domain/service/`: lógica de negocio pura (`GroupingDomainService`, `TopicAssignmentDomainService`, `GradingDomainService`)
- `application/usecase/`: implementaciones de los use cases (`*ApplicationService`), orquestan los puertos
- `application/view/`: read-models devueltos a los adaptadores de entrada
- `infrastructure/adapter/in/rest/`: controladores REST
- `infrastructure/adapter/out/persistence/`: adaptadores JPA + mappers entidad ↔ dominio
- `infrastructure/adapter/out/document/`: generación de PDF con OpenPDF
- `infrastructure/adapter/out/storage/`: almacenamiento de imagen de cabecera
- `infrastructure/config/`: registro de beans (servicios de dominio)
- `entity/` + `repository/`: entidades `@Entity` y `JpaRepository` (detalle de persistencia, envuelto por los adaptadores)
- `static/`: CSS/JS/imágenes

## Notas de diseño

- Arquitectura hexagonal (ports & adapters): el dominio es puro y no depende de Spring, JPA ni HTTP.
- Los controladores invocan use cases (puertos de entrada), nunca servicios de infraestructura directamente.
- Validaciones de negocio en los servicios de dominio y use cases; restricciones también en frontend.
- Manejo centralizado de errores con `@ControllerAdvice`.
- Base H2 persistente por archivo para conservar datos entre reinicios.
- Tests de dominio puros (sin Spring ni BD) para `GroupingDomainService` y `GradingDomainService`.
