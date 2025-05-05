@echo off
setlocal enabledelayedexpansion

:: Rutas relativas desde la carpeta generate_compose
set PROPERTIES_FILE=..\employees.properties
set OUTPUT_FILE=..\docker-compose.yml

:: Verificar que el archivo de propiedades existe
if not exist %PROPERTIES_FILE% (
    echo Error: No se puede encontrar el archivo %PROPERTIES_FILE%
    exit /b 1
)

:: Encabezado del archivo
echo services: > %OUTPUT_FILE%
echo   central-server: >> %OUTPUT_FILE%
echo     build: >> %OUTPUT_FILE%
echo       context: . >> %OUTPUT_FILE%
echo       dockerfile: DockerfileCentral >> %OUTPUT_FILE%
echo     ports: >> %OUTPUT_FILE%
echo       - "9090:9090" >> %OUTPUT_FILE%
echo     networks: >> %OUTPUT_FILE%
echo       - meeting-network >> %OUTPUT_FILE%
echo     volumes: >> %OUTPUT_FILE%
echo       - ./data:/app/data >> %OUTPUT_FILE%
echo. >> %OUTPUT_FILE%

:: Procesamiento del archivo de propiedades
for /f "tokens=1,2 delims==" %%a in (%PROPERTIES_FILE%) do (
    set "EMPLOYEE_NAME=%%a"
    set "EMPLOYEE_PORT=%%b"
    
    :: Convertir el nombre de empleado al formato de nombre de contenedor (minúsculas con guiones)
    set "CONTAINER_NAME=!EMPLOYEE_NAME:_=-!"
    for %%i in (a b c d e f g h i j k l m n o p q r s t u v w x y z) do (
        set "CONTAINER_NAME=!CONTAINER_NAME:%%i=%%i!"
    )
    
    :: Escribir la configuración del servidor de empleado
    echo   !CONTAINER_NAME!-server: >> %OUTPUT_FILE%
    echo     build: >> %OUTPUT_FILE%
    echo       context: . >> %OUTPUT_FILE%
    echo       dockerfile: Dockerfile >> %OUTPUT_FILE%
    echo       args: >> %OUTPUT_FILE%
    echo         - EMPLOYEE_NAME=!EMPLOYEE_NAME! >> %OUTPUT_FILE%
    echo         - EMPLOYEE_PORT=!EMPLOYEE_PORT! >> %OUTPUT_FILE%
    echo     ports: >> %OUTPUT_FILE%
    echo       - "!EMPLOYEE_PORT!:!EMPLOYEE_PORT!" >> %OUTPUT_FILE%
    echo     networks: >> %OUTPUT_FILE%
    echo       - meeting-network >> %OUTPUT_FILE%
    echo     depends_on: >> %OUTPUT_FILE%
    echo       - central-server >> %OUTPUT_FILE%
    echo     volumes: >> %OUTPUT_FILE%
    echo       - ./data:/app/data >> %OUTPUT_FILE%
    echo. >> %OUTPUT_FILE%
)

:: Cliente
echo   client: >> %OUTPUT_FILE%
echo     build: >> %OUTPUT_FILE%
echo       context: . >> %OUTPUT_FILE%
echo       dockerfile: DockerfileClient >> %OUTPUT_FILE%
echo     networks: >> %OUTPUT_FILE%
echo       - meeting-network >> %OUTPUT_FILE%
echo     depends_on: >> %OUTPUT_FILE%
echo       - central-server >> %OUTPUT_FILE%

:: Añadir las dependencias de todos los servidores de empleados
for /f "tokens=1 delims==" %%a in (%PROPERTIES_FILE%) do (
    set "EMPLOYEE_NAME=%%a"
    set "CONTAINER_NAME=!EMPLOYEE_NAME:_=-!"
    for %%i in (a b c d e f g h i j k l m n o p q r s t u v w x y z) do (
        set "CONTAINER_NAME=!CONTAINER_NAME:%%i=%%i!"
    )
    echo       - !CONTAINER_NAME!-server >> %OUTPUT_FILE%
)

:: Continuar con la configuración del cliente
echo     volumes: >> %OUTPUT_FILE%
echo       - ./data:/app/data >> %OUTPUT_FILE%
echo     extra_hosts: >> %OUTPUT_FILE%
echo       - "host.docker.internal:host-gateway" >> %OUTPUT_FILE%
echo. >> %OUTPUT_FILE%

:: Configuración de la red
echo networks: >> %OUTPUT_FILE%
echo   meeting-network: >> %OUTPUT_FILE%
echo     driver: bridge >> %OUTPUT_FILE%

echo Docker Compose file generado exitosamente en %OUTPUT_FILE%