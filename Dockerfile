# Dockerfile para Servidores de Empleados
FROM --platform=$BUILDPLATFORM openjdk:24
WORKDIR /app
COPY . .
RUN mkdir -p classes && \
    javac -d ./classes ./src/util/*.java ./src/model/*.java ./src/observer/*.java ./src/mediator/*.java ./src/server/EmployeeServer.java
ARG EMPLOYEE_NAME
ARG EMPLOYEE_PORT
ENV EMPLOYEE_NAME=${EMPLOYEE_NAME}
ENV EMPLOYEE_PORT=${EMPLOYEE_PORT}
EXPOSE ${EMPLOYEE_PORT}
RUN mkdir -p /app/data
CMD ["sh", "-c", "java -cp ./classes server.EmployeeServer ${EMPLOYEE_NAME} ${EMPLOYEE_PORT}"]