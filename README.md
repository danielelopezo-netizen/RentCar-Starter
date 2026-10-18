# RentCar — Sistema de Gestión de Alquiler de Vehículos (Parcial 2 · PGII)

> **Este archivo hace parte de la entrega.** Documenta de forma integral la arquitectura, reglas de negocio, los siete patrones de diseño implementados, la integración con el servicio de verificación y la API externa de pagos, y las instrucciones de ejecución y pruebas.

---

## 1. Integrantes

- **Daniel Esteven Lopez Ordoñez**
- **Juan Felipe Trujillo Urbina**

---

## 2. Descripción del Sistema y Tecnologías Utilizadas

**RentCar** es una aplicación de escritorio desarrollada en **Java 21** con **JavaFX (FXML + CSS)** y **Gradle** bajo una arquitectura **MVC (Modelo-Vista-Controlador)** orientada por principios **SOLID** y siete **patrones de diseño GoF**.

El sistema gestiona de principio a fin el flujo de alquiler de vehículos en memoria:
1. Consulta y selección de clientes (autorizados y no autorizados) y flota de vehículos.
2. Construcción validada de solicitudes de alquiler con rango de fechas, modalidad, servicios opcionales, paquetes compuestos y token de pago.
3. Cálculo detallado de tarifas en `BigDecimal` según la modalidad (`BASIC`, `STANDARD`, `PREMIUM`), servicios individuales y paquetes anidados, evitando cobros duplicados.
4. Control centralizado de disponibilidad de vehículos por intersección de intervalos temporales sobre reservaciones confirmadas.
5. Verificación previa de autorización del cliente antes de invocar la API REST externa de pagos (`external/payment-api.jar` mediante `libs/payment-client.jar`).
6. Registro y consulta en tiempo real de reservaciones en memoria (`PENDING`, `CONFIRMED`, `REJECTED`, `FAILED`).

### Tecnologías y Requisitos Previos
- **Java:** JDK 21 (configurado mediante Toolchain en Gradle).
- **Construcción:** Gradle 9.8 (incluido mediante Gradle Wrapper `gradlew` / `gradlew.bat`).
- **Interfaz Gráfica:** JavaFX 21.0.6 (`javafx.controls`, `javafx.fxml`) y hojas de estilo CSS (`styles.css`).
- **Pruebas Automatizadas:** JUnit Jupiter 5.11.4.
- **Almacenamiento:** Repositorios en memoria durante la ejecución (sin base de datos externa).

---

## 3. Requisitos Funcionales Implementados (RF-001 a RF-007)

| Código | Requisito Funcional | Estado | Implementación Principal |
|---|---|---|---|
| **RF-001** | Construcción y validación de solicitudes de alquiler | Completo | [`ReservationRequestBuilder`](src/main/java/edu/rentcar/builder/ReservationRequestBuilder.java), [`ReservationRequest`](src/main/java/edu/rentcar/model/ReservationRequest.java) |
| **RF-002** | Gestión del proceso de alquiler, reservaciones y disponibilidad | Completo | [`RentalFacade`](src/main/java/edu/rentcar/facade/RentalFacade.java), [`AvailabilityService`](src/main/java/edu/rentcar/service/AvailabilityService.java), [`ReservationRepository`](src/main/java/edu/rentcar/repository/ReservationRepository.java), [`Reservation`](src/main/java/edu/rentcar/model/Reservation.java) |
| **RF-003** | Modalidades de alquiler (`BASIC`, `STANDARD`, `PREMIUM`) | Completo | [`RentalModeFactory`](src/main/java/edu/rentcar/factory/RentalModeFactory.java), [`RentalModeFactoryProvider`](src/main/java/edu/rentcar/factory/RentalModeFactoryProvider.java) y familias de políticas |
| **RF-004** | Procesamiento de pagos mediante API externa | Completo | [`PaymentGateway`](src/main/java/edu/rentcar/adapter/PaymentGateway.java), [`PaymentAdapter`](src/main/java/edu/rentcar/adapter/PaymentAdapter.java) adaptando `PaymentApiClient` |
| **RF-005** | Servicios adicionales dinámicos sin explosión de clases | Completo | [`BillableRental`](src/main/java/edu/rentcar/decorator/BillableRental.java), [`RentalDecorator`](src/main/java/edu/rentcar/decorator/RentalDecorator.java), [`AdditionalServiceDecorator`](src/main/java/edu/rentcar/decorator/AdditionalServiceDecorator.java) y decoradores concretos |
| **RF-006** | Verificación previa al pago en capa de servicios | Completo | [`VerifiedPaymentProxy`](src/main/java/edu/rentcar/proxy/VerifiedPaymentProxy.java), [`CustomerVerificationService`](src/main/java/edu/rentcar/service/CustomerVerificationService.java) |
| **RF-007** | Paquetes de servicios simples y anidados | Completo | [`ServiceComponent`](src/main/java/edu/rentcar/composite/ServiceComponent.java), [`SingleService`](src/main/java/edu/rentcar/composite/SingleService.java), [`ServicePackage`](src/main/java/edu/rentcar/composite/ServicePackage.java), [`ServiceCatalog`](src/main/java/edu/rentcar/composite/ServiceCatalog.java) |

---

## 4. Estructura del Proyecto y Arquitectura MVC

```text
RentCar-Starter/
├── build.gradle                         # Configuración de Gradle, Java 21, JavaFX y JUnit 5
├── gradle.properties                    # Configuración de rutas de JDK 21 para Gradle Toolchain
├── README.md                            # Documentación principal del proyecto
├── docs/
│   ├── Documento-Tecnico-RentCar.md     # Documento técnico completo (UML, Pensamiento Computacional y Patrones)
│   ├── UML-RentCar.puml                 # Fuente PlantUML del diagrama de clases
│   └── instructor/README-docente.md     # Notas de referencia del docente (conservado intacto)
├── external/
│   ├── payment-api.jar                  # Servidor HTTP simulado de pagos suministrado por el docente
│   └── README-api-pagos.md              # Guía oficial de ejecución de la API de pagos (conservada intacta)
├── libs/
│   └── payment-client.jar               # Cliente HTTP PaymentApiClient suministrado por el docente
└── src/
    ├── main/
    │   ├── java/edu/rentcar/
    │   │   ├── app/                     # Inicio de aplicación JavaFX y configuración de servicios (ApplicationServices)
    │   │   ├── controller/              # Controladores JavaFX (Main, Dashboard, Customer, Vehicle, Reservation)
    │   │   ├── model/                   # Modelos de dominio, estados y objetos de valor (BigDecimal, LocalDate)
    │   │   ├── repository/              # Repositorios en memoria (Customer, Vehicle, Reservation)
    │   │   ├── service/                 # Servicios de dominio (Vehicle, Availability, Pricing, CustomerVerification)
    │   │   ├── builder/                 # Patrón Builder (ReservationRequestBuilder)
    │   │   ├── factory/                 # Patrón Abstract Factory (RentalModeFactory y políticas por modalidad)
    │   │   ├── facade/                  # Patrón Facade (RentalFacade)
    │   │   ├── adapter/                 # Patrón Adapter (PaymentGateway, PaymentAdapter)
    │   │   ├── decorator/               # Patrón Decorator (BillableRental, BaseVehicleRental, decoradores)
    │   │   ├── proxy/                   # Patrón Proxy (VerifiedPaymentProxy)
    │   │   └── composite/               # Patrón Composite (ServiceComponent, SingleService, ServicePackage)
    │   └── resources/edu/rentcar/view/  # Vistas FXML y estilos CSS (styles.css)
    └── test/java/edu/rentcar/           # Suite de 43 pruebas unitarias JUnit 5
```

### Responsabilidades por Capa
- **Presentación (`view/*.fxml` + `controller/*`):** Captura eventos del usuario, delega la construcción de solicitudes al `ReservationRequestBuilder`, invoca `RentalFacade` y actualiza las tablas y banners visuales. No contiene lógica de cálculo ni llamadas HTTP directas.
- **Coordinación y Servicios (`facade/*` + `service/*`):** Orquesta el flujo de alquiler (`RentalFacade`), evalúa solapamiento de fechas (`AvailabilityService`), calcula tarifas (`PricingService`) y verifica la autorización del cliente (`CustomerVerificationService`).
- **Dominio y Patrones (`model/*`, `builder/*`, `factory/*`, `decorator/*`, `composite/*`):** Representa las entidades inmutables, estados explícitos (`ReservationStatus`, `PaymentStatus`), políticas por modalidad y composición dinámica de servicios adicionales y paquetes.
- **Infraestructura en Memoria y Adaptadores (`repository/*`, `proxy/*`, `adapter/*`):** Gestiona la persistencia volátil en memoria durante la ejecución y encapsula el acceso protegido a la API externa de pagos.

---

## 5. Reglas de Negocio y Decisiones de Implementación

1. **Precisión Monetaria (`BigDecimal`):** Todos los cálculos de tarifas, recargos de modalidad, coberturas, asistencia, servicios y paquetes utilizan `BigDecimal` con escala de 2 decimales y redondeo `RoundingMode.HALF_UP`.
2. **Disponibilidad e Intersección de Intervalos:**
   - Un alquiler desde `startDate` hasta `endDate` ocupa el intervalo $[\text{startDate}, \text{endDate})$, requerimiento mínimo de 1 día (`endDate.isAfter(startDate)`).
   - Dos intervalos $[A_{\text{start}}, A_{\text{end}})$ y $[B_{\text{start}}, B_{\text{end}})$ se solapan si y solo si `startA.isBefore(endB) && startB.isBefore(endA)`. Los alquileres consecutivos donde `endA.equals(startB)` están permitidos.
   - Únicamente las reservaciones en estado `CONFIRMED` bloquean la disponibilidad del vehículo. Las reservaciones `REJECTED` o `FAILED` liberan inmediatamente el vehículo para consultas posteriores.
3. **Tarifas Configurables de Demostración por Modalidad (`Abstract Factory`):**
   - **`BASIC`:** 100% tarifa base del vehículo (0% recargo), cobertura civil básica (0 COP/día), soporte en horario laboral (0 COP/día). Sin servicios adicionales incluidos.
   - **`STANDARD`:** Tarifa base + **10%** recargo de modalidad, cobertura parcial CDW (**25,000 COP/día**), asistencia vial 24/7 (**10,000 COP/día**). **Incluye GPS (`SRV-GPS`) sin costo adicional**.
   - **`PREMIUM`:** Tarifa base + **20%** recargo VIP, cobertura total todo riesgo sin deducible (**45,000 COP/día**), asistencia VIP con vehículo de reemplazo (**20,000 COP/día**). **Incluye GPS (`SRV-GPS`) y Conductor Adicional (`SRV-ADD-DRIVER`) sin costo adicional**.
4. **Catálogo de Servicios y Paquetes (`ServiceCatalog`):**
   - Servicios individuales (`SingleService`): GPS (`25,000 COP/día`), Silla Infantil (`20,000 COP/día`), Conductor Adicional (`30,000 COP/día`), Wi-Fi Portátil (`18,000 COP/día`), Pase Electrónico de Peajes (`35,000 COP tarifa fija`).
   - Paquetes compuestos (`ServicePackage`):
     - `Family Comfort Package`: GPS + Silla Infantil.
     - `Executive Mobility Package`: Conductor Adicional + Wi-Fi Portátil + Pase de Peajes.
     - `All-Inclusive Touring Package` *(Paquete anidado)*: `Family Comfort Package` + Conductor Adicional + Wi-Fi Portátil + Pase de Peajes.
5. **Prevención de Doble Cobro:** Si un usuario selecciona un servicio individual o un paquete que contiene un servicio ya incluido por la modalidad seleccionada (por ejemplo, GPS en modalidad `STANDARD`) o ya incluido en otro paquete seleccionado, `AdditionalServiceDecorator` detecta el código en `getAppliedServiceCodes()` y no lo cobra dos veces.

---

## 6. Los Siete Patrones de Diseño Implementados

*(Para el análisis exhaustivo y el diagrama UML completo, consulta [`docs/Documento-Tecnico-RentCar.md`](docs/Documento-Tecnico-RentCar.md)).*

| Patrón | Tipo | Propósito en RentCar | Clases Participantes |
|---|---|---|---|
| **Builder** | Creacional | Construir y validar paso a paso solicitudes de alquiler (`ReservationRequest`) fuera de la UI. | `ReservationRequestBuilder`, `ReservationRequest` |
| **Abstract Factory** | Creacional | Crear familias coherentes de políticas de tarifa, cobertura y asistencia según la modalidad (`BASIC`, `STANDARD`, `PREMIUM`). | `RentalModeFactory`, `BasicRentalModeFactory`, `StandardRentalModeFactory`, `PremiumRentalModeFactory`, `RatePolicy`, `CoveragePolicy`, `AssistancePolicy`, `RentalModeFactoryProvider` |
| **Facade** | Estructural | Coordinar el flujo completo de alquiler (disponibilidad, cálculo de precio, reserva `PENDING`, pago y transición de estado). | `RentalFacade`, `AvailabilityService`, `PricingService`, `ReservationRepository`, `PaymentGateway` |
| **Adapter** | Estructural | Adaptar `PaymentApiClient` (`libs/payment-client.jar`) y sus modelos externos a la interfaz de dominio `PaymentGateway`. | `PaymentGateway`, `PaymentAdapter`, `PaymentApiClient`, `PaymentRequest`, `PaymentResult` |
| **Decorator** | Estructural | Añadir dinámicamente servicios opcionales y paquetes al alquiler acumulando costos y descripciones sin crear $2^n$ subclases. | `BillableRental`, `BaseVehicleRental`, `RentalDecorator`, `AdditionalServiceDecorator`, `GpsServiceDecorator`, `ChildSeatDecorator`, `AdditionalDriverDecorator` |
| **Proxy** | Estructural | Interceptar el acceso a `PaymentGateway` para exigir la verificación previa del cliente (`CustomerVerificationService`) antes de invocar la API externa. | `VerifiedPaymentProxy`, `PaymentGateway`, `PaymentAdapter`, `CustomerVerificationService` |
| **Composite** | Estructural | Representar servicios individuales y paquetes de servicios (simples y anidados) bajo una abstracción común con cálculo recursivo de costos. | `ServiceComponent`, `SingleService`, `ServicePackage`, `ServiceCatalog` |

### Ventajas y Limitaciones de la Arquitectura Implementada
- **Ventajas:** Alta cohesión y bajo acoplamiento; facilidad para incorporar nuevas modalidades o servicios sin modificar el flujo principal (Principio Abierto/Cerrado); aislamiento total del proveedor externo de pagos permitiendo pruebas unitarias deterministas y rápidas.
- **Limitaciones:** Los datos residen exclusivamente en memoria durante la ejecución de la aplicación (por restricción explícita del enunciado académico), por lo que se reinician al cerrar el programa.

---

## 7. Integración con el Servicio de Verificación y la API de Pagos

### 7.1. Verificación Previa (`CustomerVerificationService` + `VerifiedPaymentProxy`)
Antes de enviar cualquier solicitud HTTP, `VerifiedPaymentProxy` invoca `CustomerVerificationService.verifyCustomerAuthorization(customer)`:
- Clientes de ejemplo autorizados (`SampleData`):
  - `CUS-1001` — Sofia Martinez (`authorized = true`) $\rightarrow$ Permite continuar al pago.
  - `CUS-1002` — Daniel Rojas (`authorized = true`) $\rightarrow$ Permite continuar al pago.
- Cliente de ejemplo no autorizado:
  - `CUS-1003` — Valentina Torres (`authorized = false`) $\rightarrow$ **Bloqueado por `VerifiedPaymentProxy`**. La reservación pasa a `REJECTED` y **no se realiza ninguna petición HTTP** a `payment-api.jar`.

### 7.2. Ejecución y Conexión con la API Externa de Pagos (`external/payment-api.jar`)
La aplicación se conecta por defecto a `http://localhost:8080/api/payments` utilizando `PaymentApiClient` envuelto por `PaymentAdapter`.

Para probar la integración real con el servidor simulado:
1. Abre una terminal independiente en la carpeta `external/` del proyecto:
   ```powershell
   java -jar external/payment-api.jar
   ```
2. Deja esa ventana abierta escuchando en `http://localhost:8080`.
3. En la pantalla **Reservations** de la aplicación JavaFX puedes seleccionar el `Payment Token` de prueba (según el contrato de `external/README-api-pagos.md`):
   - `tok_approved` $\rightarrow$ La API responde HTTP `200` con `APPROVED` $\rightarrow$ La reservación pasa a **`CONFIRMED`**.
   - `tok_declined` $\rightarrow$ La API responde HTTP `200` con `NOT_APPROVED` $\rightarrow$ La reservación pasa a **`REJECTED`** y el vehículo sigue disponible.
   - `tok_api_error` $\rightarrow$ La API responde HTTP `503` (`PAYMENT_SERVICE_UNAVAILABLE`) $\rightarrow$ La reservación pasa a **`FAILED`** y el vehículo sigue disponible.
4. Si `payment-api.jar` no está en ejecución, `PaymentAdapter` captura el `IOException` de conexión de forma controlada, marca la reservación como **`FAILED`** (`Connection Error`) y muestra un mensaje claro al usuario sin bloquear la interfaz ni simular aprobaciones falsas.

---

## 8. Instrucciones de Configuración y Ejecución

### Requisitos
- **JDK 21** instalado.

### Ejecutar la Aplicación de Escritorio (JavaFX)
Desde la raíz del proyecto:

- **Windows PowerShell:**
  ```powershell
  .\gradlew.bat run
  ```
- **Git Bash / macOS / Linux:**
  ```bash
  ./gradlew run
  ```

### Ejecutar la Suite de Pruebas Unitarias (JUnit 5)
Las pruebas unitarias utilizan dobles de prueba (*test doubles* / *spies*) para aislar la API externa, por lo que **no requieren** que `payment-api.jar` esté ejecutándose:

- **Windows PowerShell:**
  ```powershell
  .\gradlew.bat test
  ```
- **Git Bash / macOS / Linux:**
  ```bash
  ./gradlew test
  ```

---

## 9. Resumen de Pruebas Unitarias Realizadas

Se implementaron y ejecutaron **8 suites de pruebas (43 pruebas unitarias en total)** con **100% de éxito (0 fallos, 0 errores)**:

| Suite de Prueba | Pruebas | Escenarios Cubiertos |
|---|---:|---|
| [`ReservationRequestBuilderTest`](src/test/java/edu/rentcar/builder/ReservationRequestBuilderTest.java) | 11 | Construcción válida, deduplicación de servicios, rechazo de cliente/vehículo nulo, tarifa inválida, fechas iguales o invertidas, modalidad nula y token vacío. |
| [`RentalModeFactoryTest`](src/test/java/edu/rentcar/factory/RentalModeFactoryTest.java) | 4 | Creación de familias `BASIC`, `STANDARD` y `PREMIUM`, cálculo exacto de tarifas y deducción automática de servicios incluidos en la modalidad. |
| [`RentalDecoratorTest`](src/test/java/edu/rentcar/decorator/RentalDecoratorTest.java) | 3 | Decorador individual (`GpsServiceDecorator`), combinación múltiple de decoradores y prevención de cobro duplicado. |
| [`ServicePackageTest`](src/test/java/edu/rentcar/composite/ServicePackageTest.java) | 5 | Paquete simple (`FamilyPackage`), paquete anidado (`AllInclusiveTouringPackage`), paquetes con descuento, prevención de ciclos recursivos y duplicados. |
| [`PaymentAdapterTest`](src/test/java/edu/rentcar/adapter/PaymentAdapterTest.java) | 5 | Adaptación de `APPROVED` (HTTP 200), `NOT_APPROVED` (HTTP 200), error técnico HTTP 503, solicitud inválida HTTP 400 y fallo de conexión (`IOException`). |
| [`VerifiedPaymentProxyTest`](src/test/java/edu/rentcar/proxy/VerifiedPaymentProxyTest.java) | 3 | Cliente autorizado invoca el gateway de pago; cliente no autorizado es rechazado con **0 invocaciones** al gateway; fallo en `CustomerVerificationService` bloquea el pago con **0 invocaciones**. |
| [`RentalFacadeTest`](src/test/java/edu/rentcar/facade/RentalFacadeTest.java) | 5 | Alquiler exitoso que confirma reserva y bloquea disponibilidad futura (permitiendo alquileres consecutivos); rechazo por solapamiento de fechas; pago rechazado (`NOT_APPROVED`) que no confirma reserva; cliente no autorizado; error de conexión que marca `FAILED`. |
| [`VehicleServiceTest`](src/test/java/edu/rentcar/service/VehicleServiceTest.java) | 7 | Pruebas base del módulo de registro y validación de vehículos conservadas y pasando al 100%. |

---

## 10. Evidencias y Documentación Adicional

- **Documento Técnico Completo y Análisis de Patrones:** [`docs/Documento-Tecnico-RentCar.md`](docs/Documento-Tecnico-RentCar.md)
- **Código Fuente del Diagrama UML (PlantUML):** [`docs/UML-RentCar.puml`](docs/UML-RentCar.puml)
- **Guía de la API Externa de Pagos:** [`external/README-api-pagos.md`](external/README-api-pagos.md)
