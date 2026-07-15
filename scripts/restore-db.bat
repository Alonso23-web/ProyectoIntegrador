@echo off
setlocal enabledelayedexpansion

echo ==============================================
echo   Nueva Ases Express - Restaurar Base de Datos
echo ==============================================
echo.

REM Verificar que se recibio un parametro
if "%~1"=="" (
    echo [ERROR] Debes especificar la ruta al archivo .sql de backup.
    echo.
    echo Uso: %~nx0 ^<ruta_al_backup.sql^>
    echo.
    echo Ejemplo: %~nx0 "C:\Backups\NuevaAsesExpress\backup_nueva_ases_local_20260714_153000.sql"
    echo.
    pause
    exit /b 1
)

set "BACKUP_FILE=%~1"

REM Verificar que el archivo existe
if not exist "%BACKUP_FILE%" (
    echo [ERROR] No se encontro el archivo de backup:
    echo "%BACKUP_FILE%"
    echo.
    pause
    exit /b 1
)

REM Verificar que es un archivo .sql
if /i not "%~x1"==".sql" (
    echo [WARN] El archivo especificado no tiene extension .sql.
    echo       Asegurate de que sea un archivo de backup valido.
    echo.
)

echo [INFO] Archivo de backup: %BACKUP_FILE%

REM Obtener la ruta absoluta al directorio del proyecto
set "SCRIPT_DIR=%~dp0"
set "PROJECT_DIR=%SCRIPT_DIR%.."
set "PROPS_FILE=%PROJECT_DIR%\src\main\resources\application-local.properties"

REM Verificar que el archivo de propiedades existe
if not exist "%PROPS_FILE%" (
    echo [ERROR] No se encontro el archivo de propiedades:
    echo "%PROPS_FILE%"
    echo Asegurate de ejecutar este script desde la carpeta scripts/ del proyecto.
    echo.
    pause
    exit /b 1
)

echo [INFO] Leyendo credenciales desde application-local.properties...

REM Leer URL, usuario y password desde el properties
REM Asignacion directa sin delayed expansion para evitar corromper caracteres especiales
for /f "usebackq tokens=1,* delims==" %%a in ("%PROPS_FILE%") do (
    if "%%a"=="spring.datasource.url" set "DB_URL=%%b"
    if "%%a"=="spring.datasource.username" set "DB_USER=%%b"
    if "%%a"=="spring.datasource.password" set "DB_PASS=%%b"
)

REM Verificar que se leyeron los valores
if not defined DB_URL (
    echo [ERROR] No se pudo leer spring.datasource.url desde el archivo de propiedades.
    pause
    exit /b 1
)
if not defined DB_USER (
    echo [ERROR] No se pudo leer spring.datasource.username desde el archivo de propiedades.
    pause
    exit /b 1
)

echo [INFO] URL encontrada: %DB_URL%

REM ==============================================
REM Parseo robusto de URL JDBC
REM Formato: jdbc:mysql://HOST:PUERTO/NOMBREBD?params...
REM Soporta localhost e IPs (ej: 127.0.0.1)
REM ==============================================
set "TEMP_URL=%DB_URL:jdbc:mysql://=%"

REM Extraer HOST:PORT (todo antes del primer /)
for /f "tokens=1 delims=/" %%a in ("%TEMP_URL%") do set "HOSTPORT=%%a"

REM Extraer puerto: reemplazar : por espacios, el ultimo token es el puerto
set "HOSTPORT_SPACED=%HOSTPORT::= %"
for %%i in (%HOSTPORT_SPACED%) do set "DB_PORT=%%i"

REM Extraer host: eliminar :PUERTO del HOSTPORT
set "DB_HOST=!HOSTPORT::%DB_PORT%=!"

REM Extraer nombre de BD: todo entre el primer / y el primer ?
set "AFTER_SLASH=%TEMP_URL:*/=%"
for /f "tokens=1 delims=?" %%a in ("%AFTER_SLASH%") do set "DB_NAME=%%a"

echo [INFO] Host: %DB_HOST%
echo [INFO] Puerto: %DB_PORT%
echo [INFO] Base de datos: %DB_NAME%

echo.
echo [WARN] !!! ATENCION: VAS A SOBREESCRIBIR LA BASE DE DATOS !!!
echo [WARN] Base de datos objetivo: %DB_NAME%@%DB_HOST%:%DB_PORT%
echo [WARN] Archivo a restaurar: %BACKUP_FILE%
echo.
set /p "CONFIRM=Escribe CONFIRMAR para continuar o presiona Ctrl+C para cancelar: "
if /i not "!CONFIRM!"=="CONFIRMAR" (
    echo.
    echo [INFO] Operacion cancelada por el usuario.
    pause
    exit /b 0
)

echo.
echo [INFO] Verificando el tamano del archivo de backup...
for %%A in ("%BACKUP_FILE%") do set "FILE_SIZE=%%~zA"
echo [INFO] Tamano del backup: %FILE_SIZE% bytes
echo.

echo [INFO] Iniciando restauracion de la base de datos...
echo.

REM Ejecutar mysql para restaurar el backup
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% < "%BACKUP_FILE%"

if errorlevel 1 (
    echo [ERROR] Fallo la restauracion de la base de datos.
    echo.
    echo Posibles causas:
    echo   - MySQL no esta instalado o mysql no esta en el PATH
    echo   - Credenciales incorrectas
    echo   - El servicio de MySQL no esta corriendo
    echo   - El archivo de backup esta corrupto
    echo.
    pause
    exit /b 1
)

echo.
echo [SUCCESS] Restauracion completada exitosamente.
echo [SUCCESS] Base de datos '%DB_NAME%' restaurada desde:
echo [SUCCESS] %BACKUP_FILE%
echo.

echo ==============================================
echo   Proceso de restauracion finalizado.
echo ==============================================
echo.
pause
exit /b 0
