# 🚗 FuelTracker

Aplicación Android para el registro, análisis y visualización del consumo de combustible de un vehículo.

El objetivo del proyecto es ofrecer una herramienta clara para entender el gasto real en combustible, consumo medio y evolución del vehículo a lo largo del tiempo.

---

## ⚙️ Tecnologías

- Kotlin
- Jetpack Compose
- Room (Base de datos local)
- MVVM
- StateFlow / Coroutines

---

## ✨ Funcionalidades

### 📊 Gestión de repostajes
- Añadir, editar y eliminar repostajes
- Historial completo de registros
- Validación de datos (km, litros, precio, fecha)

### 📈 Estadísticas avanzadas
- Consumo medio (L/100km)
- Kilómetros mensuales estimados
- Gasto mensual estimado
- Totales globales:
  - Kilómetros recorridos
  - Litros consumidos
  - Dinero invertido
  - Precio medio del litro
  - Días de uso del vehículo

### 🔍 Análisis por intervalos
- Cálculo automático entre repostajes
- Métricas por tramo:
  - Consumo real
  - Km por mes
  - Gasto por mes
- Visualización detallada de evolución

### 🎨 UI/UX
- Interfaz moderna con Jetpack Compose
- Tarjetas dinámicas con información contextual
- Sistema de colores según eficiencia de consumo
- Filtrado de datos por año

---

## 🧠 Arquitectura

El proyecto sigue arquitectura **MVVM**:

- **ViewModel** → lógica de negocio y estado
- **Repository** → acceso a datos
- **Room Database** → persistencia local
- **Compose UI** → capa de presentación reactiva

---

## 📦 Estado del proyecto

Versión actual: **v0.0.4**

Proyecto en desarrollo activo con mejoras continuas en:
- UI/UX
- Precisión de métricas
- Refactorización de arquitectura

---

## 🚀 Próximas mejoras

- Gráficas de consumo (charts)
- Exportación de datos (CSV / JSON)
- Backup de base de datos
- Soporte multi-vehículo