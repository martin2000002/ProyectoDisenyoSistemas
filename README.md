# Manejador de Reuniones Corporativo

Este proyecto implementa un sistema de gestión de reuniones entre empleados de una compañía, desarrollado en Java, con arquitectura distribuida basada en sockets y ejecutándose en contenedores Docker.

## Índice
- [Descripción General](#descripción-general)
- [Patrones de Diseño Implementados](#patrones-de-diseño-implementados)
  - [Patrón Mediator](#patrón-mediator)
  - [Patrón Observer](#patrón-observer)
- [Instalación y Ejecución](#instalación-y-ejecución)
  - [Requisitos Previos](#requisitos-previos)
  - [Pasos para la Ejecución](#pasos-para-la-ejecución)
  - [Configuración de Empleados](#configuración-de-empleados)
- [Pruebas del Sistema](#pruebas-del-sistema)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Solución de Problemas](#solución-de-problemas)

## Descripción General

El sistema permite gestionar reuniones corporativas donde los empleados pueden:
- Crear nuevas reuniones e invitar a otros empleados
- Modificar reuniones existentes (como organizador o como invitado)
- Eliminar reuniones (como organizador)

Cada empleado tiene su propio servidor que se ejecuta continuamente y recibe actualizaciones sobre reuniones a través del servidor central.

## Patrones de Diseño Implementados

### Patrón Mediator

El Servidor Central implementa el patrón Mediator, actuando como intermediario para la comunicación entre los servidores de los empleados. Esto permite desacoplar a los empleados entre sí, ya que no necesitan conocerse directamente para comunicarse.

**Clases relevantes:**
- `Mediator` (interfaz)
- `CentralServerMediator` (implementación concreta)

### Patrón Observer

Los servidores de empleados implementan el patrón Observer, donde:
- El servidor de cada empleado actúa como Sujeto (Subject)
- El observador `MeetingUpdateObserver` reacciona a las notificaciones de actualización
- Cuando llega una notificación, el observador actualiza el archivo de reuniones

**Clases relevantes:**
- `Subject` (interfaz)
- `Observer` (interfaz)
- `MeetingUpdateObserver` (implementación concreta)

## Instalación y Ejecución

### Requisitos Previos
- Docker y Docker Compose
- Git para clonar el repositorio

### Pasos para la Ejecución

1. **Clone el repositorio:**
   ```bash
   git clone https://github.com/martin2000002/ProyectoDisenyoSistemas.git
   cd ProyectoDisenyoSistemas
   ```

2. **Configure los empleados** (opcional):
   - El sistema viene preconfigurado con cinco empleados
   - Si desea modificar esta configuración, consulte la sección [Configuración de Empleados](#configuración-de-empleados)

3. **Ejecute los contenedores:**
   ```bash
   docker-compose up
   ```

4. **Utilice la aplicación cliente:**
   ```bash
   docker exec -it proyectodisenyosistemas-client-1 java -cp ./classes client.EmployeeClient
   ```

5. **Monitoreo de logs** (opcional):
   - Para ver los logs del servidor central:
     ```bash
     docker logs -f proyectodisenyosistemas-central-server-1
     ```
   - Para ver los logs del servidor de un empleado:
     ```bash
     docker logs -f proyectodisenyosistemas-[nombre-apellido]-server-1
     ```
     Ejemplo:
     ```bash
     docker logs -f proyectodisenyosistemas-alice-white-server-1
     ```

> **Nota:** Si cambia el nombre de la carpeta del proyecto, deberá reemplazar `proyectodisenyosistemas` por el nuevo nombre en todos los comandos.

### Configuración de Empleados

El sistema viene preconfigurado con cinco empleados para pruebas:
- Alice_White (puerto 8081)
- Bob_Smith (puerto 8082)
- Carol_Simpson (puerto 8083)
- David_Black (puerto 8084)
- Eva_Brown (puerto 8085)

Para modificar esta configuración:

1. **Edite el archivo `employees.properties`** con el formato `Nombre_Apellido=Puerto`
2. **Regenere el archivo `docker-compose.yml`** utilizando los scripts proporcionados:

   - En Windows:
     ```bash
     cd generate_compose
     windows.bat
     ```

   - En macOS/Linux:
     ```bash
     cd generate_compose
     chmod +x macos.sh
     ./macos.sh
     ```

3. **Reinicie los contenedores** para aplicar los cambios:
   ```bash
   docker-compose down
   docker-compose up
   ```

## Pruebas del Sistema

Para verificar el funcionamiento completo del sistema:

1. **Inicie los contenedores Docker** como se describió anteriormente
2. **Ejecute un cliente** para un empleado:
   ```bash
   docker exec -it proyectodisenyosistemas-client-1 java -cp ./classes client.EmployeeClient
   ```

3. **Realice las siguientes operaciones:**
   - Crear una nueva reunión invitando a otros empleados
   - Modificar una reunión existente como organizador
   - Modificar una reunión como invitado (solo se permite cambiar el tema)
   - Eliminar una reunión como organizador

4. **Verifique que los archivos de reuniones** en la carpeta `data/` se actualicen correctamente
   - Cada empleado tendrá su propio archivo: `data/Nombre_Apellido_meetings.txt`

5. **Pruebe la resolución de conflictos:**
   - Ejecute varios clientes simultáneamente
   - Modifique la misma reunión desde diferentes clientes
   - Compruebe que se aplica la regla "last-write-wins"

## Estructura del Proyecto

- `src/` - Código fuente del proyecto
  - `client/` - Cliente de empleado
  - `server/` - Implementación de servidores
  - `model/` - Modelo de dominio (Meeting)
  - `mediator/` - Implementación del patrón Mediator
  - `observer/` - Implementación del patrón Observer
  - `util/` - Utilidades para manejo de propiedades
- `data/` - Archivos de reuniones generados por el sistema
- `generate_compose/` - Scripts para generar el archivo docker-compose.yml
- `Dockerfile*` - Archivos para construir las imágenes Docker

## Solución de Problemas

- **Error al ejecutar el cliente:**
  - Asegúrese de que los contenedores están en ejecución
  - Verifique que el nombre del contenedor sea correcto:
    ```bash
    docker ps
    ```

- **No se crean/actualizan los archivos de reuniones:**
  - Verifique los permisos de la carpeta `data/`
  - Revise los logs del servidor central y del servidor del empleado

- **Puerto ya en uso:**
  - Cambie los puertos en `employees.properties` y regenere el `docker-compose.yml` usando los scripts de la carpeta `generate_compose/`.