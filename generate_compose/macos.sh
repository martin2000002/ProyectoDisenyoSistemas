#!/bin/bash

# Rutas relativas desde la carpeta generate_compose
PROPERTIES_FILE="../employees.properties"
OUTPUT_FILE="../docker-compose.yml"
DATA_DIR="../data"
DOCKER_HUB_USERNAME="martin2000002"

# Verificar que el archivo de propiedades existe
if [ ! -f "$PROPERTIES_FILE" ]; then
    echo "Error: No se puede encontrar el archivo $PROPERTIES_FILE"
    exit 1
fi

# Encabezado del archivo
cat > $OUTPUT_FILE << EOF
services:
  central-server:
    image: ${DOCKER_HUB_USERNAME}/meeting-central-server:latest
    ports:
      - "9090:9090"
    networks:
      - meeting-network
    volumes:
      - ./data:/app/data
      - ./employees.properties:/app/employees.properties

EOF

# Array para almacenar los nombres de los contenedores
declare -a container_names

# Procesamiento del archivo de propiedades
while IFS='=' read -r employee_name employee_port || [[ -n "$employee_name" ]]; do
    if [[ -z "$employee_name" || "$employee_name" == \#* ]]; then
        continue  # Saltar líneas vacías o comentarios
    fi
    
    # Convertir el nombre de empleado al formato de nombre de contenedor (minúsculas con guiones)
    container_name=$(echo "$employee_name" | tr '_' '-' | tr '[:upper:]' '[:lower:]')
    container_names+=("$container_name")
    
    # Escribir la configuración del servidor de empleado
    cat >> $OUTPUT_FILE << EOF
  ${container_name}-server:
    image: ${DOCKER_HUB_USERNAME}/meeting-employee-server:latest
    environment:
      - EMPLOYEE_NAME=${employee_name}
      - EMPLOYEE_PORT=${employee_port}
    ports:
      - "${employee_port}:${employee_port}"
    networks:
      - meeting-network
    depends_on:
      - central-server
    volumes:
      - ./data:/app/data
      - ./employees.properties:/app/employees.properties

EOF
done < "$PROPERTIES_FILE"

# Cliente
cat >> $OUTPUT_FILE << EOF
  client:
    image: ${DOCKER_HUB_USERNAME}/meeting-client:latest
    networks:
      - meeting-network
    depends_on:
      - central-server
EOF

# Añadir las dependencias de todos los servidores de empleados
for container_name in "${container_names[@]}"; do
    echo "      - ${container_name}-server" >> $OUTPUT_FILE
done

# Continuar con la configuración del cliente
cat >> $OUTPUT_FILE << EOF
    volumes:
      - ./data:/app/data
      - ./employees.properties:/app/employees.properties
    extra_hosts:
      - "host.docker.internal:host-gateway"

networks:
  meeting-network:
    driver: bridge
EOF

echo "Docker Compose file generado exitosamente en $OUTPUT_FILE"

# Eliminar todos los archivos en el directorio data
echo "Eliminando archivos de la carpeta data..."
if [ -d "$DATA_DIR" ]; then
    # Crear el directorio si no existe
    mkdir -p "$DATA_DIR"
    # Eliminar todo su contenido
    rm -f "$DATA_DIR"/*
    echo "Archivos eliminados correctamente de $DATA_DIR"
else
    # Crear el directorio si no existe
    mkdir -p "$DATA_DIR"
    echo "El directorio $DATA_DIR se ha creado (estaba vacío)"
fi