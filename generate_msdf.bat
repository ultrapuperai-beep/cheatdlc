@echo off
echo Downloading msdf-atlas-gen...

REM Скачайте msdf-atlas-gen с GitHub:
REM https://github.com/Chlumsky/msdf-atlas-gen/releases

REM После скачивания распакуйте и запустите:
msdf-atlas-gen.exe -font MuseoSansCyrl-900.ttf -type msdf -format png -imageout font.png -json font.json -size 42 -pxrange 4 -charset charset.txt

echo Done! Files created: font.png and font.json
pause
