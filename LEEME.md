# PULSE

App Android para el sprint de 90 días: rutinas animadas para la máquina todo en uno, calendario de cumplimiento, mediciones de peso y cintura, y recordatorios.

## Cómo obtener el APK (sin instalar nada en tu computadora)

1. Crea una cuenta gratuita en github.com si no tienes.
2. Arriba a la derecha toca **+** y luego **New repository**. Ponle de nombre `pulse`, márcalo como **Private** y toca **Create repository**.
3. En la página del repositorio toca **uploading an existing file**.
4. Descomprime `PULSE-proyecto.zip`, abre la carpeta `pulse` y arrastra **todo lo que hay adentro** a la página (no la carpeta `pulse` en sí). Toca **Commit changes**.
   - En la página principal del repositorio deberías ver `app`, `build.gradle.kts` y `settings.gradle.kts` directamente.
   - El navegador casi siempre se salta la carpeta `.github`. Por eso viene una copia visible llamada `COPIAR-build.yml`. Toca **Add file → Create new file**, escribe como nombre `.github/workflows/build.yml`, pega el contenido de `COPIAR-build.yml` y toca **Commit changes**.
5. Ve a la pestaña **Actions**. Verás "Construir APK" corriendo (tarda unos 5 a 8 minutos). Si no arrancó, entra a "Construir APK" y toca **Run workflow**.
6. Cuando tenga un check verde, entra a la ejecución y abajo, en **Artifacts**, descarga **PULSE-apk**. Es un .zip con `PULSE.apk` adentro.
7. Pasa `PULSE.apk` a tu teléfono, ábrelo y permite "Instalar apps de origen desconocido" cuando lo pida.

## Al abrir la app

- Acepta las notificaciones.
- En Ajustes, toca "Enviar notificación de prueba" para confirmar que llegan.
- Si tu teléfono tiene ahorro de batería agresivo (Samsung, Xiaomi, Huawei), quita PULSE de la optimización de batería.

## Actualizaciones

El proyecto trae su propia firma (`app/pulse.keystore`), así que las versiones nuevas se instalan encima sin perder tus datos. No borres ese archivo.

Íconos: Lucide (licencia ISC). Tipografías: Space Grotesk e Inter (SIL Open Font License).
