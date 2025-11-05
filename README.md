# CGE Project - Sistema de Gestión Eléctrica

Este es un proyecto de escritorio desarrollado con **Compose for Desktop** que simula un sistema de gestión para una compañía eléctrica. Permite administrar clientes, medidores, lecturas de consumo y generar boletas de facturación.

## ✨ Características Principales

- **Gestión de Clientes**: Permite crear, editar, eliminar y buscar clientes en el sistema.
- **Gestión de Medidores**: Admite el registro y la administración de medidores de tipo **monofásico** y **trifásico**, asociándolos a un cliente.
- **Registro de Lecturas**: Facilita la entrada de lecturas de consumo (kWh) para cada medidor, especificando el mes y el año.
- **Generación de Boletas**: Calcula y genera las boletas de facturación mensuales para cada cliente.
- **Persistencia de Datos**: Toda la información se guarda localmente en archivos de formato CSV, simulando una base de datos simple.

## 📂 Estructura del Proyecto

El proyecto sigue una arquitectura limpia y organizada en las siguientes capas principales:

- **`dominio`**: Contiene las clases del modelo de negocio (`Cliente`, `Medidor`, `Boleta`, `LecturaConsumo`, etc.), que representan las entidades centrales del sistema.
- **`persistencia`**: Se encarga del almacenamiento y la recuperación de datos.
    - `StorageDriver`: Una interfaz que abstrae el mecanismo de almacenamiento.
    - `FileSystemStorageDriver`: Una implementación que guarda los datos en archivos CSV en una carpeta `data/` en la raíz del proyecto.
    - `PersistenciaDatos`: Actúa como un DAO que maneja la lógica de lectura/escritura de los archivos CSV.
    - `*RepoImpl`: Repositorios que implementan la lógica de negocio para acceder a los datos.
- **`ui`**: Contiene los `Composables` de Jetpack Compose que construyen la interfaz de usuario. Cada pantalla (`PantallaClientes`, `PantallaMedidores`, etc.) está encapsulada en su propia clase.
- **`main.kt`**: El punto de entrada de la aplicación. Configura la ventana principal y la navegación entre las diferentes pantallas.

## 🚀 Cómo Ejecutar el Proyecto

1.  Abre el proyecto en IntelliJ IDEA o Android Studio.
2.  Ejecuta la función `main` que se encuentra en el archivo `composeApp/src/jvmMain/kotlin/org/example/cgeproject/main.kt`.
3.  La aplicación se iniciará y creará automáticamente una carpeta `data/` en la raíz del proyecto para almacenar los datos.

## 💾 Almacenamiento de Datos

La aplicación utiliza un sistema de persistencia basado en archivos **CSV**.

- Los datos se guardan en la carpeta `data/` en la raíz del proyecto.
- Cada entidad principal (clientes, medidores, lecturas, boletas) se almacena en su propio archivo `.csv`.
- Este enfoque permite que la aplicación sea completamente autocontenida y no requiera una base de datos externa.
