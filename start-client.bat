@echo off
SETLOCAL EnableDelayedExpansion

echo ===================================
echo = Gestor de Reuniones Corporativo =
echo ===================================
echo.
echo Seleccione un empleado:
echo 1. Alice White
echo 2. Bob Smith
echo 3. Carol Simpson
echo 4. David Black
echo 5. Eva Brown
echo.
SET /P opcion=Ingrese el número de opción: 

IF "%opcion%"=="1" (
    SET empleado=Alice_White
) ELSE IF "%opcion%"=="2" (
    SET empleado=Bob_Smith
) ELSE IF "%opcion%"=="3" (
    SET empleado=Carol_Simpson
) ELSE IF "%opcion%"=="4" (
    SET empleado=David_Black
) ELSE IF "%opcion%"=="5" (
    SET empleado=Eva_Brown
) ELSE (
    echo Opción inválida
    pause
    exit /b
)

echo.
echo Iniciando cliente para %empleado%...
echo.

docker exec -it proyecto-client-1 java -cp ./classes client.EmployeeClient %empleado%

ENDLOCAL