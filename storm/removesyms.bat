@echo off
setlocal enabledelayedexpansion

:: Get the current directory
set "currentDir=%~dp0"

:: Loop through all subdirectories in the current directory
for /D %%D in ("%currentDir%*") do (
    pushd "%%D"
    :: Remove symlinks in the current subdirectory
    for /f "delims=" %%A in ('dir /AL /B') do (
        echo Removing symlink: %%A in directory: %%D
        del "%%A"
    )
    popd
)

echo Done removing symlinks from subdirectories.
pause
