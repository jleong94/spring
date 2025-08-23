@echo off
setlocal ENABLEDELAYEDEXPANSION

:: Always change to Keycloak base directory (parent of conf/bin)
cd /d "%~dp0.."

:menu
echo.
echo ======================================
echo   Keycloak Environment Selector
echo ======================================
echo [1] Development
echo [2] Production
echo [Q] Quit
echo.

set /p choice=Select environment [1/2/Q]: 

if /i "%choice%"=="1" (
    echo.
    echo Starting Keycloak in DEVELOPMENT mode...
    set KC_CONF=conf\keycloak-dev.conf
    bin\kc.bat start-dev
    goto :eof
)

if /i "%choice%"=="2" (
    echo.
    echo Starting Keycloak in PRODUCTION mode...
    set KC_DB_URL=
    set KC_DB_USERNAME=
    set KC_DB_PASSWORD=
    set KC_CONF=conf\keycloak-prod.conf
    bin\kc.bat start --optimized
    goto :eof
)

if /i "%choice%"=="Q" (
    echo Quitting...
    goto :eof
)

echo Invalid choice: "%choice%"
echo Please try again.
goto menu
