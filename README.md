# Documentación del Proyecto: CGEProject

## 1. Visión General del Proyecto

`CGEProject` es una aplicación de escritorio desarrollada con Jetpack Compose para Desktop, diseñada para la gestión de clientes, medidores de consumo eléctrico, lecturas y la emisión de boletas de cobro. La arquitectura de la aplicación sigue un enfoque de separación de capas, dividiendo las responsabilidades en:

- **Dominio:** Contiene las entidades y reglas de negocio principales.
- **Persistencia:** Se encarga del almacenamiento y la recuperación de datos.
- **Servicios:** Orquesta la lógica de negocio y las operaciones complejas.
- **UI (Interfaz de Usuario):** Proporciona la interacción con el usuario.

## 2. Capa de Dominio (`dominio`)

Esta capa define las clases de datos y la lógica de negocio fundamental de la aplicación.

- **`EntidadBase`**: Clase base para todas las entidades, proporcionando un `id`, `createdAt` y `updatedAt`.
- **`Persona`**: Clase base que define propiedades comunes para seres humanos, como `rut`, `nombre` y `email`.
- **`Cliente`**: Hereda de `Persona`. Representa a un cliente del servicio eléctrico. Contiene su dirección de facturación, estado (activo/inactivo), el tipo de tarifa asociada (residencial/comercial) y listas de sus medidores y boletas.
- **`Medidor`**: Representa un medidor de consumo eléctrico. Tiene un código único, una dirección de suministro y está asociado a un cliente. Es una clase abierta, con especializaciones:
    - **`MedidorMonofasico`**: Un tipo de medidor con una potencia máxima específica.
    - **`MedidorTrifasico`**: Un tipo de medidor que, además de la potencia, tiene un factor de potencia.
- **`LecturaConsumo`**: Almacena el valor de consumo (kWh) leído de un medidor en un mes y año específicos.
- **`Boleta`**: Representa una boleta de cobro emitida a un cliente para un mes y año determinados. Contiene el total de kWh consumidos, el detalle del cálculo de la tarifa y el estado de la boleta (pendiente, pagada, etc.).
- **`Tarifa`**: Interfaz que define el contrato para los diferentes tipos de tarifas. Exige un método `nombre()` y un método `calcular(kwh)`.
    - **`TarifaResidencial`**: Implementación de `Tarifa` para clientes residenciales. Calcula el costo basado en tramos de consumo.
    - **`TarifaComercial`**: Implementación de `Tarifa` para clientes comerciales. Aplica un precio por kWh único y un recargo comercial.
- **`TarifaDetalle`**: Clase de datos que contiene el desglose de un cálculo de tarifa (subtotal, cargos, IVA, total).
- **`ExportablePDF`**: Interfaz que define un método `toPdfTable()`, obligando a las clases que la implementan a proporcionar una representación de sí mismas apta para ser convertida en una tabla de PDF.

## 3. Capa de Persistencia (`persistencia`)

Esta capa es responsable de abstraer el almacenamiento de datos. Utiliza un `StorageDriver` para leer y escribir datos en formato CSV.

- **`StorageDriver`**: Interfaz que define las operaciones básicas de almacenamiento (put, get, keys, remove).
- **`FileSystemStorageDriver`**: Implementación de `StorageDriver` que guarda los datos en archivos `.csv` dentro de una carpeta `data` en la raíz del proyecto.
- **Repositorios**: Interfaces que definen los contratos para acceder a los datos de cada entidad del dominio (`ClienteRepositorio`, `MedidorRepositorio`, `LecturaRepositorio`, `BoletaRepositorio`).
- **Implementaciones de Repositorios** (`ClienteRepoImpl`, `MedidorRepoImpl`, etc.): Clases que implementan las interfaces de los repositorios, utilizando `PersistenciaDatos` (una clase que a su vez usa el `StorageDriver`) para realizar las operaciones de lectura y escritura.

## 4. Capa de Servicios (`servicios`)

Esta capa contiene la lógica de negocio de alto nivel y coordina las operaciones entre el dominio y la persistencia.

- **`TarifaService`**: Servicio responsable de determinar qué tarifa (`TarifaResidencial` o `TarifaComercial`) se debe aplicar a un cliente según su tipo.
- **`BoletaService`**: Orquesta la lógica de negocio relacionada con las boletas. Sus responsabilidades clave son:
    - `calcularKwhClienteMes`: Calcula el consumo total de un cliente en un mes, basándose en las lecturas de sus medidores.
    - `emitirBoletaMensual`: Genera y guarda una nueva boleta para un cliente, calculando el consumo, aplicando la tarifa correspondiente y guardando el resultado.
    - `exportarPdfClienteMes`: Genera un archivo PDF para una boleta específica.
- **`PdfService`**: Servicio dedicado a la generación de archivos PDF. Utiliza la biblioteca iText para crear un documento PDF a partir de los datos de una o más boletas.

## 5. Capa de Interfaz de Usuario (`ui`)

Construida con Jetpack Compose, esta capa proporciona la interfaz gráfica con la que el usuario interactúa.

- **`App.kt`**: El punto de entrada principal de la aplicación. Contiene el navegador principal que permite al usuario cambiar entre las diferentes pantallas (Clientes, Medidores, Lecturas, Boletas). Aquí es donde se instancian y se inyectan las dependencias (servicios y repositorios) en las diferentes pantallas.
- **`PantallaClientes`**: Permite al usuario ver una lista de todos los clientes, buscarlos por nombre o RUT, y realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar).
- **`PantallaMedidores`**: Permite gestionar los medidores. El usuario puede buscar medidores por el RUT del cliente o por el código del medidor, así como crear y eliminar medidores.
- **`PantallaLecturas`**: Permite registrar nuevas lecturas de consumo para un medidor y ver el historial de lecturas de un medidor en un mes y año específicos.
- **`PantallaBoletas`**: Permite al usuario buscar las boletas de un cliente por su RUT. Muestra una lista de las boletas encontradas y, para cada una, permite ver el detalle y descargar un archivo PDF con la información de la boleta.