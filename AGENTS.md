# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

OpenVXML is a VoiceXML IVR (Interactive Voice Response) development platform built as Eclipse/OSGi plugins. Originally the "Eclipse Voice Tools Project" (VTP) by OpenMethods, now maintained by VHT/Medallia. It provides a visual IDE for designing IVR call flows (desktop layer) and a server-side runtime engine that executes them (framework layer).

## Build

Requires JDK 8 (Amazon Corretto 8 in CI) and Maven 3.5.x. No Maven wrapper is included.

```bash
mvn clean verify
```

This is the only build command. There are no profiles or separate test goals. The build uses Eclipse Tycho 1.3.0 with the pomless extension (`.mvn/extensions.xml`), so most plugins don't need individual pom.xml files — Tycho infers module types from `MANIFEST.MF` and `feature.xml`.

Build output (p2 update site): `com.vht.openvxml.releng/com.vht.openvxml.update/target/repository/`

## Testing

There is effectively no automated test suite. The one test class (`org.eclipse.vtp.framework.util/src/test/java/.../XMLUtilitiesXXETest.java`) is **not run by `mvn verify`** because Tycho only compiles `src/main/java`. It must be run from an IDE with the bundled JUnit 4.13.2 and Hamcrest 1.3 jars on the classpath.

## Architecture — Three Layers

**Desktop** (`com.vht.openvxml.desktop`) — Eclipse RCP plugins providing the visual IVR design IDE: workflow editors with visual themes (Attraction, Mantis), model layers for business objects/databases/web services, media/voice prompt editors, project management and WAR export.

**Framework** (`com.vht.openvxml.framework`) — Server-side runtime engine deployed as a WAR via the Eclipse Equinox Servlet Bridge. Core subsystems: process engine and session management (`framework.engine`), extension points for actions/configurations/observers/services (`framework.core`), voice interaction handling (`framework.interactions.*`), JavaScript scripting via Mozilla Rhino, database and web service connectors.

**Platforms** (`com.vht.openvxml.platforms`) — Telephony platform integrations: Genesys GVP, Cisco CVP, Dialogic, Prophecy, AVP, VTOP, VXMLB, and IDriver (native Windows telephony via JNA).

## Module Layout

Each layer follows the same pattern: `*.features` (Eclipse feature definitions) and `*.plugins` (OSGi bundles). The parent POM chain is: plugin → `com.vht.openvxml.configuration` (in `com.vht.openvxml.releng`) → root.

Standard Eclipse plugin structure per bundle:
- `META-INF/MANIFEST.MF` — OSGi bundle manifest (dependencies, exports, execution environment)
- `plugin.xml` — Eclipse extension point declarations
- `build.properties` — source folders and build includes
- `src/main/java/` — Java source

## Package Naming

- `org.eclipse.vtp.framework.*` — runtime framework (historical VTP origin)
- `org.eclipse.vtp.desktop.*` — desktop IDE
- `org.eclipse.vtp.modules.*` — modular UI components
- `com.openmethods.openvxml.*` — platform integrations, desktop model extensions, idriver

## Key Dependencies

Eclipse Kepler SR2 (with Java 8 patches), OSGi/Equinox, Apache HttpClient 4.5.13, Jackson 2.13.x (in Genesys/VXMLB platforms), Mozilla Rhino, Apache Derby, Xerces. No Spring or modern web frameworks.

## Security: XXE Hardening

XML parsing is centralized through `XMLUtilities` in `org.eclipse.vtp.framework.util`. All runtime XML parsers are routed through this class with XXE-prevention features enabled. When adding new XML parsing, use `XMLUtilities` rather than creating parser instances directly.

## PR Conventions

PRs use the template in `.github/PULL_REQUEST_TEMPLATE.md` with sections: Card(s), Related Pull Requests, Changes Made, Test Procedure (Cucumber format). Code ownership: `@VHT/prem-voice-platform`.

## CI

Jenkins pipeline in `jenkins/ci_build/JenkinsFile`. Runs `mvn clean verify` on Ubuntu with Corretto JDK 8, then packages the p2 repository and publishes it to a build server.

## Related Repository

VXML-IVR (`github.com/VHT/VXML-IVR`) builds the final deployable product and consumes this repo's p2 update site as a dependency.
