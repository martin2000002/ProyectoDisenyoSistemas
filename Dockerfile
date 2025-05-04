# Dockerfile para Servidores de Empleados
FROM openjdk:24
WORKDIR /app
COPY ./src ./src
RUN mkdir -p classes && javac -d ./classes ./src/observer/*.java ./src/server/EmployeeServer.java ./src/model/*.java
ARG EMPLOYEE_NAME
ARG EMPLOYEE_PORT
ENV EMPLOYEE_NAME=${EMPLOYEE_NAME}
ENV EMPLOYEE_PORT=${EMPLOYEE_PORT}
EXPOSE ${EMPLOYEE_PORT}
CMD ["sh", "-c", "java -cp ./classes server.EmployeeServer ${EMPLOYEE_NAME} ${EMPLOYEE_PORT}"]