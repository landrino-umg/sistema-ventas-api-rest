@echo off
title SERVIDOR API REST - SISTEMA DE VENTAS
echo ===================================================
echo   INICIANDO SERVICIO API REST (PROGRAMACION II)
echo ===================================================
set "JAVA_HOME=C:\Program Files\Apache NetBeans\jdk"
cd /d "C:\Users\Luis\Desktop\Universidad\sistema-ventas-api-rest"
"C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" exec:java
pause
