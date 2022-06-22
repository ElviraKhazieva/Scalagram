@REM scalagram launcher script
@REM
@REM Environment:
@REM JAVA_HOME - location of a JDK home dir (optional if java on path)
@REM CFG_OPTS  - JVM options (optional)
@REM Configuration:
@REM SCALAGRAM_config.txt found in the SCALAGRAM_HOME.
@setlocal enabledelayedexpansion
@setlocal enableextensions

@echo off


if "%SCALAGRAM_HOME%"=="" (
  set "APP_HOME=%~dp0\\.."

  rem Also set the old env name for backwards compatibility
  set "SCALAGRAM_HOME=%~dp0\\.."
) else (
  set "APP_HOME=%SCALAGRAM_HOME%"
)

set "APP_LIB_DIR=%APP_HOME%\lib\"

rem Detect if we were double clicked, although theoretically A user could
rem manually run cmd /c
for %%x in (!cmdcmdline!) do if %%~x==/c set DOUBLECLICKED=1

rem FIRST we load the config file of extra options.
set "CFG_FILE=%APP_HOME%\SCALAGRAM_config.txt"
set CFG_OPTS=
call :parse_config "%CFG_FILE%" CFG_OPTS

rem We use the value of the JAVA_OPTS environment variable if defined, rather than the config.
set _JAVA_OPTS=%JAVA_OPTS%
if "!_JAVA_OPTS!"=="" set _JAVA_OPTS=!CFG_OPTS!

rem We keep in _JAVA_PARAMS all -J-prefixed and -D-prefixed arguments
rem "-J" is stripped, "-D" is left as is, and everything is appended to JAVA_OPTS
set _JAVA_PARAMS=
set _APP_ARGS=

set "APP_CLASSPATH=%APP_LIB_DIR%\scalagram.scalagram-0.1.0-SNAPSHOT.jar;%APP_LIB_DIR%\org.scala-lang.scala-library-2.13.8.jar;%APP_LIB_DIR%\org.typelevel.cats-core_2.13-2.7.0.jar;%APP_LIB_DIR%\org.http4s.http4s-blaze-server_2.13-0.23.12.jar;%APP_LIB_DIR%\org.http4s.http4s-circe_2.13-0.23.12.jar;%APP_LIB_DIR%\org.http4s.http4s-dsl_2.13-0.23.12.jar;%APP_LIB_DIR%\io.circe.circe-generic_2.13-0.14.2.jar;%APP_LIB_DIR%\io.circe.circe-literal_2.13-0.14.2.jar;%APP_LIB_DIR%\io.circe.circe-parser_2.13-0.14.2.jar;%APP_LIB_DIR%\io.circe.circe-config_2.13-0.8.0.jar;%APP_LIB_DIR%\ch.qos.logback.logback-classic-1.2.11.jar;%APP_LIB_DIR%\org.tpolecat.doobie-core_2.13-1.0.0-RC1.jar;%APP_LIB_DIR%\org.tpolecat.doobie-hikari_2.13-1.0.0-RC1.jar;%APP_LIB_DIR%\org.tpolecat.doobie-postgres_2.13-1.0.0-RC1.jar;%APP_LIB_DIR%\org.postgresql.postgresql-42.4.0.jar;%APP_LIB_DIR%\org.flywaydb.flyway-core-8.5.12.jar;%APP_LIB_DIR%\com.github.t3hnar.scala-bcrypt_2.13-4.3.0.jar;%APP_LIB_DIR%\tf.tofu.tofu-core-ce3_2.13-0.10.8.jar;%APP_LIB_DIR%\tf.tofu.tofu-logging_2.13-0.10.8.jar;%APP_LIB_DIR%\tf.tofu.tofu-logging-derivation_2.13-0.10.8.jar;%APP_LIB_DIR%\tf.tofu.tofu-kernel-ce3-interop_2.13-0.10.8.jar;%APP_LIB_DIR%\org.typelevel.cats-kernel_2.13-2.7.0.jar;%APP_LIB_DIR%\org.typelevel.simulacrum-scalafix-annotations_2.13-0.5.4.jar;%APP_LIB_DIR%\org.http4s.http4s-blaze-core_2.13-0.23.12.jar;%APP_LIB_DIR%\org.http4s.http4s-server_2.13-0.23.12.jar;%APP_LIB_DIR%\org.http4s.http4s-core_2.13-0.23.12.jar;%APP_LIB_DIR%\org.http4s.http4s-jawn_2.13-0.23.12.jar;%APP_LIB_DIR%\io.circe.circe-core_2.13-0.14.2.jar;%APP_LIB_DIR%\io.circe.circe-jawn_2.13-0.14.2.jar;%APP_LIB_DIR%\com.chuusai.shapeless_2.13-2.3.9.jar;%APP_LIB_DIR%\com.typesafe.config-1.4.0.jar;%APP_LIB_DIR%\ch.qos.logback.logback-core-1.2.11.jar;%APP_LIB_DIR%\org.slf4j.slf4j-api-1.7.36.jar;%APP_LIB_DIR%\org.tpolecat.doobie-free_2.13-1.0.0-RC1.jar;%APP_LIB_DIR%\org.scala-lang.modules.scala-collection-compat_2.13-2.7.0.jar;%APP_LIB_DIR%\org.tpolecat.typename_2.13-1.0.0.jar;%APP_LIB_DIR%\com.zaxxer.HikariCP-4.0.3.jar;%APP_LIB_DIR%\co.fs2.fs2-io_2.13-3.2.7.jar;%APP_LIB_DIR%\org.checkerframework.checker-qual-3.5.0.jar;%APP_LIB_DIR%\de.svenkubiak.jBCrypt-0.4.1.jar;%APP_LIB_DIR%\tf.tofu.tofu-kernel_2.13-0.10.8.jar;%APP_LIB_DIR%\tf.tofu.tofu-logging-structured_2.13-0.10.8.jar;%APP_LIB_DIR%\tf.tofu.tofu-logging-layout_2.13-0.10.8.jar;%APP_LIB_DIR%\tf.tofu.tofu-logging-shapeless_2.13-0.10.8.jar;%APP_LIB_DIR%\tf.tofu.tofu-logging-refined_2.13-0.10.8.jar;%APP_LIB_DIR%\tf.tofu.tofu-logging-log4cats_2.13-0.10.8.jar;%APP_LIB_DIR%\tf.tofu.tofu-optics-macro_2.13-0.10.8.jar;%APP_LIB_DIR%\tf.tofu.tofu-derivation_2.13-0.10.8.jar;%APP_LIB_DIR%\tf.tofu.derevo-core_2.13-0.13.0.jar;%APP_LIB_DIR%\com.propensive.magnolia_2.13-0.17.0.jar;%APP_LIB_DIR%\org.typelevel.cats-effect_2.13-3.3.9.jar;%APP_LIB_DIR%\org.http4s.blaze-http_2.13-0.23.12.jar;%APP_LIB_DIR%\org.typelevel.case-insensitive_2.13-1.2.0.jar;%APP_LIB_DIR%\org.typelevel.cats-effect-std_2.13-3.3.12.jar;%APP_LIB_DIR%\org.typelevel.cats-parse_2.13-0.3.7.jar;%APP_LIB_DIR%\org.http4s.http4s-crypto_2.13-0.2.3.jar;%APP_LIB_DIR%\co.fs2.fs2-core_2.13-3.2.7.jar;%APP_LIB_DIR%\com.comcast.ip4s-core_2.13-3.1.3.jar;%APP_LIB_DIR%\org.typelevel.literally_2.13-1.0.2.jar;%APP_LIB_DIR%\org.log4s.log4s_2.13-1.10.0.jar;%APP_LIB_DIR%\org.scodec.scodec-bits_2.13-1.1.31.jar;%APP_LIB_DIR%\org.typelevel.vault_2.13-3.1.0.jar;%APP_LIB_DIR%\org.typelevel.jawn-fs2_2.13-2.2.0.jar;%APP_LIB_DIR%\org.typelevel.jawn-parser_2.13-1.3.2.jar;%APP_LIB_DIR%\io.circe.circe-numbers_2.13-0.14.2.jar;%APP_LIB_DIR%\org.typelevel.cats-free_2.13-2.7.0.jar;%APP_LIB_DIR%\org.scala-lang.scala-reflect-2.13.8.jar;%APP_LIB_DIR%\tf.tofu.tofu-optics-core_2.13-0.10.8.jar;%APP_LIB_DIR%\tf.tofu.tofu-core-higher-kind_2.13-0.10.8.jar;%APP_LIB_DIR%\com.tethys-json.tethys-core_2.13-0.26.0.jar;%APP_LIB_DIR%\com.tethys-json.tethys-jackson_2.13-0.26.0.jar;%APP_LIB_DIR%\org.typelevel.alleycats-core_2.13-2.7.0.jar;%APP_LIB_DIR%\org.typelevel.cats-tagless-macros_2.13-0.14.0.jar;%APP_LIB_DIR%\eu.timepit.refined_2.13-0.9.28.jar;%APP_LIB_DIR%\org.typelevel.log4cats-core_2.13-1.5.1.jar;%APP_LIB_DIR%\com.propensive.mercator_2.13-0.2.1.jar;%APP_LIB_DIR%\org.typelevel.cats-effect-kernel_2.13-3.3.12.jar;%APP_LIB_DIR%\org.http4s.blaze-core_2.13-0.23.12.jar;%APP_LIB_DIR%\com.twitter.hpack-1.0.2.jar;%APP_LIB_DIR%\com.fasterxml.jackson.core.jackson-core-2.11.4.jar;%APP_LIB_DIR%\org.typelevel.cats-tagless-core_2.13-0.14.0.jar;%APP_LIB_DIR%\org.scala-lang.scala-compiler-2.13.8.jar;%APP_LIB_DIR%\org.scala-lang.modules.scala-xml_2.13-1.3.0.jar;%APP_LIB_DIR%\org.jline.jline-3.21.0.jar;%APP_LIB_DIR%\net.java.dev.jna.jna-5.9.0.jar"
set "APP_MAIN_CLASS=ru.itis.scalagram.Server"
set "SCRIPT_CONF_FILE=%APP_HOME%\conf\application.ini"

rem Bundled JRE has priority over standard environment variables
if defined BUNDLED_JVM (
  set "_JAVACMD=%BUNDLED_JVM%\bin\java.exe"
) else (
  if "%JAVACMD%" neq "" (
    set "_JAVACMD=%JAVACMD%"
  ) else (
    if "%JAVA_HOME%" neq "" (
      if exist "%JAVA_HOME%\bin\java.exe" set "_JAVACMD=%JAVA_HOME%\bin\java.exe"
    )
  )
)

if "%_JAVACMD%"=="" set _JAVACMD=java

rem Detect if this java is ok to use.
for /F %%j in ('"%_JAVACMD%" -version  2^>^&1') do (
  if %%~j==java set JAVAINSTALLED=1
  if %%~j==openjdk set JAVAINSTALLED=1
)

rem BAT has no logical or, so we do it OLD SCHOOL! Oppan Redmond Style
set JAVAOK=true
if not defined JAVAINSTALLED set JAVAOK=false

if "%JAVAOK%"=="false" (
  echo.
  echo A Java JDK is not installed or can't be found.
  if not "%JAVA_HOME%"=="" (
    echo JAVA_HOME = "%JAVA_HOME%"
  )
  echo.
  echo Please go to
  echo   http://www.oracle.com/technetwork/java/javase/downloads/index.html
  echo and download a valid Java JDK and install before running scalagram.
  echo.
  echo If you think this message is in error, please check
  echo your environment variables to see if "java.exe" and "javac.exe" are
  echo available via JAVA_HOME or PATH.
  echo.
  if defined DOUBLECLICKED pause
  exit /B 1
)

rem if configuration files exist, prepend their contents to the script arguments so it can be processed by this runner
call :parse_config "%SCRIPT_CONF_FILE%" SCRIPT_CONF_ARGS

call :process_args %SCRIPT_CONF_ARGS% %%*

set _JAVA_OPTS=!_JAVA_OPTS! !_JAVA_PARAMS!

if defined CUSTOM_MAIN_CLASS (
    set MAIN_CLASS=!CUSTOM_MAIN_CLASS!
) else (
    set MAIN_CLASS=!APP_MAIN_CLASS!
)

rem Call the application and pass all arguments unchanged.
"%_JAVACMD%" !_JAVA_OPTS! !SCALAGRAM_OPTS! -cp "%APP_CLASSPATH%" %MAIN_CLASS% !_APP_ARGS!

@endlocal

exit /B %ERRORLEVEL%


rem Loads a configuration file full of default command line options for this script.
rem First argument is the path to the config file.
rem Second argument is the name of the environment variable to write to.
:parse_config
  set _PARSE_FILE=%~1
  set _PARSE_OUT=
  if exist "%_PARSE_FILE%" (
    FOR /F "tokens=* eol=# usebackq delims=" %%i IN ("%_PARSE_FILE%") DO (
      set _PARSE_OUT=!_PARSE_OUT! %%i
    )
  )
  set %2=!_PARSE_OUT!
exit /B 0


:add_java
  set _JAVA_PARAMS=!_JAVA_PARAMS! %*
exit /B 0


:add_app
  set _APP_ARGS=!_APP_ARGS! %*
exit /B 0


rem Processes incoming arguments and places them in appropriate global variables
:process_args
  :param_loop
  call set _PARAM1=%%1
  set "_TEST_PARAM=%~1"

  if ["!_PARAM1!"]==[""] goto param_afterloop


  rem ignore arguments that do not start with '-'
  if "%_TEST_PARAM:~0,1%"=="-" goto param_java_check
  set _APP_ARGS=!_APP_ARGS! !_PARAM1!
  shift
  goto param_loop

  :param_java_check
  if "!_TEST_PARAM:~0,2!"=="-J" (
    rem strip -J prefix
    set _JAVA_PARAMS=!_JAVA_PARAMS! !_TEST_PARAM:~2!
    shift
    goto param_loop
  )

  if "!_TEST_PARAM:~0,2!"=="-D" (
    rem test if this was double-quoted property "-Dprop=42"
    for /F "delims== tokens=1,*" %%G in ("!_TEST_PARAM!") DO (
      if not ["%%H"] == [""] (
        set _JAVA_PARAMS=!_JAVA_PARAMS! !_PARAM1!
      ) else if [%2] neq [] (
        rem it was a normal property: -Dprop=42 or -Drop="42"
        call set _PARAM1=%%1=%%2
        set _JAVA_PARAMS=!_JAVA_PARAMS! !_PARAM1!
        shift
      )
    )
  ) else (
    if "!_TEST_PARAM!"=="-main" (
      call set CUSTOM_MAIN_CLASS=%%2
      shift
    ) else (
      set _APP_ARGS=!_APP_ARGS! !_PARAM1!
    )
  )
  shift
  goto param_loop
  :param_afterloop

exit /B 0
