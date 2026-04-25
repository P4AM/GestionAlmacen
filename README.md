# ERP Almacén - Sistema de Gestión de Inventario y Ventas

Este proyecto ha sido desarrollado como solución integral para la gestión de almacenes, enfocándose en la eficiencia operativa, la integridad de los datos y una experiencia de usuario optimizada. El sistema permite el control total sobre el inventario, la relación con proveedores y la ejecución de ventas en tiempo real.

## 📋 Funcionalidades del Sistema

El software se divide en módulos estratégicos diseñados para cubrir las necesidades reales de una organización:

### 1. Panel de Control y Analítica
Visualización de métricas clave mediante un Dashboard dinámico que incluye:
*   **Valorización de Inventario:** Cálculo en tiempo real del valor total de los activos.
*   **Gestión de Stock Crítico:** Sistema de alertas para productos con existencias bajas.
*   **Distribución por Categorías:** Análisis visual de la composición del inventario.

### 2. Gestión de Inventario y Almacenamiento Digital
Control detallado de productos con un enfoque en la optimización de recursos:
*   **Optimización de Imágenes (Pica.js):** Los archivos multimedia son redimensionados y comprimidos en el cliente antes de la subida, reduciendo el consumo de ancho de banda y almacenamiento.
*   **Persistencia en la Nube:** Integración con buckets de almacenamiento para garantizar la disponibilidad global de los recursos visuales.
*   **Filtros de Búsqueda de Alto Rendimiento:** Implementación de búsquedas reactivas que permiten localizar ítems sin necesidad de recargas de página.

### 3. Punto de Venta (POS)
Módulo transaccional diseñado para la agilidad en el proceso de venta:
*   Buscador predictivo de productos.
*   Gestión dinámica de carrito de compras con persistencia temporal.
*   Actualización atómica de stock tras cada transacción.

### 4. Sistema de Reportes Profesionales
Generación de documentos para la toma de decisiones:
*   **Exportación a Excel:** Reportes detallados para análisis de datos masivos.
*   **Reportes PDF:** Documentos de inventario crítico listos para impresión y auditoría.

## 🛠️ Stack Tecnológico y Arquitectura

Para garantizar la escalabilidad y el rendimiento del sistema, se han seleccionado las siguientes tecnologías:

*   **Backend:** Java 21 con el framework **Spring Boot**. Se ha implementado un **Sistema de Caché Quirúrgica** para minimizar las consultas a la base de datos y reducir la latencia de respuesta.
*   **Base de Datos y Seguridad:** Uso de **Supabase (PostgreSQL)**. La integridad y seguridad de la información se gestionan mediante políticas de Row Level Security (RLS).
*   **Frontend Reactivo:** Implementación de **HTMX** para comunicaciones asíncronas servidor-cliente y **Alpine.js** para la lógica de estados en el navegador. Esta combinación permite una experiencia similar a una Single Page Application (SPA) pero con una carga inicial mucho más ligera.
*   **Interfaz Visual:** Diseño basado en **Vanilla CSS** con principios de ergonomía visual (Modo Oscuro) y efectos de transparencia moderna (Glassmorphism).

## 🚀 Guía de Operación

1.  **Inicialización:** El sistema requiere la configuración previa de categorías y proveedores para mantener la integridad referencial.
2.  **Registro de Activos:** Al ingresar productos, el sistema optimiza automáticamente las imágenes adjuntas.
3.  **Flujo de Venta:** Las transacciones se realizan desde el módulo POS, el cual descuenta automáticamente las existencias del inventario global.
4.  **Auditoría:** Los reportes pueden generarse en cualquier momento desde las secciones correspondientes para validar la consistencia de los datos.

---
*Proyecto desarrollado por el Grupo Temporal - Ingeniería de Software.*
