@echo off
setlocal enabledelayedexpansion

echo ==============================================
echo   Nueva Ases Express - Backup de Base de Datos
echo ==============================================
echo.

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

REM Crear carpeta de backups si no existe (incluyendo directorios padres)
set "BACKUP_DIR=C:\Backups\NuevaAsesExpress"
if not exist "C:\Backups" (
    mkdir "C:\Backups"
    if !errorlevel! neq 0 (
        echo [ERROR] No se pudo crear el directorio C:\Backups.
        pause
        exit /b 1
    )
)
if not exist "%BACKUP_DIR%" (
    mkdir "%BACKUP_DIR%"
    if !errorlevel! neq 0 (
        echo [ERROR] No se pudo crear el directorio de backups: "%BACKUP_DIR%"
        pause
        exit /b 1
    )
    echo [INFO] Directorio de backups creado: "%BACKUP_DIR%"
)

REM Generar nombre de archivo con timestamp
for /f "tokens=2-4 delims=/ " %%a in ('echo %date%') do (
    set "DD=%%a"
    set "MM=%%b"
    set "YYYY=%%c"
)
REM Ajustar formato segun configuracion regional del sistema
if "%YYYY%"=="" (
    for /f "tokens=1-3 delims=/ " %%a in ('echo %date%') do (
        set "YYYY=%%c"
        set "MM=%%a"
        set "DD=%%b"
    )
)
REM Si aun asi falla, usar formato MM/DD/YYYY
if "%YYYY%"=="" (
    for /f "tokens=1-3 delims=/ " %%a in ('echo %date%') do (
        set "MM=%%a"
        set "DD=%%b"
        set "YYYY=%%c"
    )
)
set "HH=%time:~0,2%"
set "MIN=%time:~3,2%"
set "SEC=%time:~6,2%"
REM Quitar espacios en hora de un digito (ej: " 9" -> "09")
if "%HH:~0,1%"==" " set "HH=0%HH:~1%"
set "TIMESTAMP=%YYYY%%MM%%DD%_%HH%%MIN%%SEC%"
set "BACKUP_FILE=%BACKUP_DIR%\backup_nueva_ases_local_%TIMESTAMP%.sql"

echo.
echo [INFO] Ejecutando mysqldump...
echo [INFO] Archivo de salida: %BACKUP_FILE%
echo.

REM Ejecutar mysqldump
mysqldump -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% ^
    --single-transaction --routines --triggers --databases %DB_NAME% ^
    > "%BACKUP_FILE%"

if errorlevel 1 (
    echo [ERROR] Fallo la ejecucion de mysqldump.
    echo.
    echo Posibles causas:
    echo   - MySQL no esta instalado o mysqldump no esta en el PATH
    echo   - Credenciales incorrectas
    echo   - El servicio de MySQL no esta corriendo
    echo.
    pause
    exit /b 1
)

REM Verificar que el archivo se creo y no esta vacio
if not exist "%BACKUP_FILE%" (
    echo [ERROR] No se pudo crear el archivo de backup.
    pause
    exit /b 1
)

for %%A in ("%BACKUP_FILE%") do set "FILE_SIZE=%%~zA"
if %FILE_SIZE% equ 0 (
    echo [ERROR] El archivo de backup esta vacio. La base de datos podria estar vacia o inaccesible.
    del "%BACKUP_FILE%"
    pause
    exit /b 1
)

echo.
echo [SUCCESS] Backup completado exitosamente.
echo [SUCCESS] Archivo: %BACKUP_FILE%
echo [SUCCESS] Tamano: %FILE_SIZE% bytes
echo.

REM ==============================================
REM Limpieza: conservar solo los ultimos 6 backups
REM ==============================================
echo [INFO] Verificando backups antiguos (maximo 6)...

set "COUNT=0"
for %%F in ("%BACKUP_DIR%\backup_nueva_ases_local_*.sql") do set /a COUNT+=1
echo [INFO] Backups actuales: %COUNT%

if %COUNT% gtr 6 (
    echo [INFO] Hay %COUNT% backups. Conservando solo los 6 mas recientes...

    set /a "DELETED=0"

    REM Listar archivos por fecha descendente (mas recientes primero), saltar los 6 primeros
    for /f "skip=6 delims=" %%F in ('dir /b /o-d "%BACKUP_DIR%\backup_nueva_ases_local_*.sql" 2^>nul') do (
        del "%BACKUP_DIR%\%%F"
        if !errorlevel! equ 0 (
            set /a DELETED+=1
            echo [INFO] Eliminado backup antiguo: %%F
        ) else (
            echo [WARN] No se pudo eliminar: %%F
        )
    )
    echo [INFO] Eliminados !DELETED! backup(s) antiguo(s).
)

echo.
echo ==============================================
echo   Proceso de backup finalizado.
echo ==============================================
echo.
pause
exit /b 0
