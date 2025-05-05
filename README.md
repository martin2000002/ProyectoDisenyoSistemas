# Manejador de Reuniones Corporativo

Este proyecto implementa un sistema de gestión de reuniones entre empleados de una compañía, desarrollado en Java, con arquitectura distribuida basada en sockets y ejecutándose en contenedores Docker.

## Patrones de Diseño Implementados

### Patrón Mediator
El Servidor Central implementa el patrón Mediator, actuando como intermediario para la comunicación entre los servidores de los empleados. Esto permite desacoplar a los empleados entre sí, ya que no necesitan conocerse directamente para comunicarse.

Clases relevantes:
- `Mediator` (interfaz)
- `CentralServerMediator` (implementación concreta)

### Patrón Observer
Los servidores de empleados implementan el patrón Observer, donde:
- El servidor de cada empleado actúa como Sujeto (Subject)
- El observador `MeetingUpdateObserver` reacciona a las notificaciones de actualización
- Cuando llega una notificación, el observador actualiza el archivo de reuniones

Clases relevantes:
- `Subject` (interfaz)
- `Observer` (interfaz)
- `MeetingUpdateObserver` (implementación concreta)

## Instalación y Ejecución

### Requisitos Previos
- Docker y Docker Compose
- Git para clonar el repositorio

### Pasos para la Ejecución

1. Clone el repositorio:
   ```bash
   git clone https://github.com/martin2000002/ProyectoDisenyoSistemas.git
   cd ProyectoDisenyoSistemas
   ```

2. Configure los empleados a su necesidad:
   ```
   Mire la sección de configuración de empleados a continuación
   ```

3. Ejecute los contenedores:
   ```bash
   docker-compose up
   ```

4. Utilice el cliente con el siguiente comando:
   ```bash
   docker exec -it proyecto-client-1 java -cp ./classes client.EmployeeClient
   ```

### Configuración de Empleados

El sistema viene preconfigurado con cinco empleados para pruebas:
- Alice_White (puerto 8081)
- Bob_Smith (puerto 8082)
- Carol_Simpson (puerto 8083)
- David_Black (puerto 8084)
- Eva_Brown (puerto 8085)

Para modificar esta configuración, edite el archivo `employees.properties` y luego regenere el archivo `docker-compose.yml` utilizando los scripts proporcionados:

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

## Pruebas del Sistema

Para probar el sistema completo:

1. Inicie los contenedores Docker como se describió anteriormente
2. Conéctese al cliente y seleccione un empleado
3. Realice las siguientes operaciones:
   - Crear una nueva reunión invitando a otros empleados
   - Modificar una reunión existente como organizador
   - Modificar una reunión como invitado (solo el tema)
   - Eliminar una reunión como organizador (opcional es una especificación extra)
4. Verifique que los archivos de reuniones en la carpeta `data/` se actualicen correctamente
5. Pruebe la resolución de conflictos modificando la misma reunión desde diferentes clientes

## Notas Adicionales

- El sistema utiliza el mecanismo "last-write-wins" basado en timestamps para resolver conflictos
- Las reuniones eliminadas se quitan completamente de los archivos de los invitados
- Cada servidor de empleado opera en su propio puerto, como se define en `employees.properties`