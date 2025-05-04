@echo off
echo Construyendo imagenes Docker...
docker-compose build

echo Iniciando contenedores...
docker-compose up -d

echo Contenedores iniciados. Puedes ver los logs con 'docker-compose logs'
echo Para acceder al cliente de Alice, ejecuta:
echo docker-compose exec alice-server java -cp ./classes client.EmployeeClient Alice_White