@echo off

REM Set the folder name to symlink
set "folderName=storm"

REM Set source as the current directory where the batch file is located
set "source=%~dp0"

REM Calculate the absolute destination based on the known project structure
set "absoluteDestination=C:\Users\Stormi\IdeaProjects\plugins"

echo Source: %source%
echo Absolute Destination: %absoluteDestination%

REM Check if the source folder exists
if not exist "%source%" (
    echo The source folder does not exist: %source%
    pause
    exit /b
)

REM Check if the destination folder exists
if not exist "%absoluteDestination%" (
    echo The destination folder does not exist: %absoluteDestination%
    pause
    exit /b
)

REM Remove existing symbolic link if it exists
if exist "%absoluteDestination%\%folderName%" (
    echo Removing existing link: %absoluteDestination%\%folderName%
    rmdir /S /Q "%absoluteDestination%\%folderName%"
)

REM Create symbolic link for the folder
echo Creating link for directory: %folderName%
mklink /J "%absoluteDestination%\%folderName%" "%source%"

echo Symbolic link created successfully!
pause
