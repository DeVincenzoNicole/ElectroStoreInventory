Objetivo:
Diseñar y prototipar una mejora a un sistema de gestion de inventario existente que opera en un entorno distribuido.
El objetivo es optimizar la consistencia del inventario, reducir la latencia en la actualizacion de stock y disminuir los problemas de seguridad y observabilidad.

Contexto:
Tu empresa mantiene un sistema de gestion de inventario para una cadena de tiendas minoristas. Actualmente, cada tienda tiene una base de datos local que se sincroniza periodicamente (cada 15 minutos) con una base de datos central.
Los clientes enfrentan inconsistencias y latencia en las actualizaciones, lo que ha generado problemas de experiencia de usuario y pérdida de ventas debido a discrepancias en el stock.
El sistema actual tiene un backend monolítico y el frontend es una aplicacion web heredada.

Requisitos:

🔹 Diseño Técnico:
	•	Proponer una arquitectura distribuida que aborde los problemas de consistencia y latencia del sistema actual.
	•	Diseñar la API para las operaciones clave de inventario.
	•	Justificar tus decisiones técnicas y de diseño de API, explicando por qué son las más adecuadas para este escenario distribuido.

🔹 Backend:
	•	Implementar un prototipo simplificado de los servicios backend propuestos. Usa el lenguaje de programacion de tu eleccion.
	•	Simular persistencia de datos usando archivos JSON/CSV locales o una base de datos en memoria (ejemplo: SQLite, H2 Database) para representar el inventario. No se requiere una base de datos real.
	•	Implementar mecanismos básicos de tolerancia a fallos que consideres necesarios.
	•	Incluir logica para manejar actualizaciones de stock en un entorno concurrente, priorizando consistencia sobre disponibilidad (o viceversa), justificando tu eleccion.