@echo off
title SERVIDOR API REST (SPRING BOOT) - SISTEMA DE VENTAS
echo ===================================================
echo   INICIANDO SERVICIO API REST SPRING BOOT (PUERTO 8080)
echo ===================================================
set "JAVA_HOME=C:\Program Files\Apache NetBeans\jdk"
cd /d "C:\Users\Luis\Desktop\Universidad\sistema-ventas-api-rest"
"C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" spring-boot:run
pause
