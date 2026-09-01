# OpenVXML

VoiceXML IVR development platform built as Eclipse/OSGi plugins. Originally the "Eclipse Voice Tools Project" (VTP) by OpenMethods, now maintained by VHT/Medallia. Provides a visual IDE for designing IVR call flows (desktop layer) and a server-side runtime engine that executes them (framework layer).

## Build

Requires JDK 8 (Amazon Corretto 8 in CI) and Maven 3.5.x. No Maven wrapper included.

```bash
mvn clean verify
```

Build output (p2 update site): `com.vht.openvxml.releng/com.vht.openvxml.update/target/repository/`

Uses Eclipse Tycho 1.3.0 with the pomless extension (`.mvn/extensions.xml`), so most plugins don't need individual pom.xml files.

Key dependencies: Eclipse Kepler SR2, OSGi/Equinox, Apache HttpClient 4.5.13, Jackson 2.13.x, Mozilla Rhino, Apache Derby, Xerces.

## Test

No automated test suite. The one test class (`org.eclipse.vtp.framework.util/src/test/java/.../XMLUtilitiesXXETest.java`) is not run by `mvn verify` because Tycho only compiles `src/main/java`. Must be run from an IDE with JUnit 4.13.2 and Hamcrest 1.3 on the classpath.

## Architecture

Three layers, each following the same pattern: `*.features` (Eclipse feature definitions) and `*.plugins` (OSGi bundles). Parent POM chain: plugin -> `com.vht.openvxml.configuration` (in `com.vht.openvxml.releng`) -> root.

**Desktop** (`com.vht.openvxml.desktop`) — Eclipse RCP plugins providing the visual IVR design IDE: workflow editors with visual themes (Attraction, Mantis), model layers for business objects/databases/web services, media/voice prompt editors, project management and WAR export.

**Framework** (`com.vht.openvxml.framework`) — Server-side runtime engine deployed as a WAR via the Eclipse Equinox Servlet Bridge. Core subsystems: process engine and session management (`framework.engine`), extension points for actions/configurations/observers/services (`framework.core`), voice interaction handling (`framework.interactions.*`), JavaScript scripting via Mozilla Rhino, database and web service connectors.

**Platforms** (`com.vht.openvxml.platforms`) — Telephony platform integrations: Genesys GVP, Cisco CVP, Dialogic, Prophecy, AVP, VTOP, VXMLB, and IDriver (native Windows telephony via JNA).

Standard Eclipse plugin structure per bundle:
- `META-INF/MANIFEST.MF` — OSGi bundle manifest
- `plugin.xml` — Eclipse extension point declarations
- `build.properties` — source folders and build includes
- `src/main/java/` — Java source

Package namespaces: `org.eclipse.vtp.framework.*` (runtime), `org.eclipse.vtp.desktop.*` (IDE), `org.eclipse.vtp.modules.*` (modular UI), `com.openmethods.openvxml.*` (platform integrations).

XML parsing is centralized through `XMLUtilities` in `org.eclipse.vtp.framework.util` with XXE-prevention features enabled. Use `XMLUtilities` rather than creating parser instances directly.

## CI/CD

Jenkins pipeline in `jenkins/ci_build/JenkinsFile`. Runs `mvn clean verify` on Ubuntu with Corretto JDK 8, then packages the p2 repository and publishes to a build server.

## Relevant Projects

- `VXML-IVR` — builds the final deployable product and consumes this repo's p2 update site.

## Conventions

Code ownership: `@VHT/prem-voice-platform`. PRs use `.github/PULL_REQUEST_TEMPLATE.md` with sections: Card(s), Related Pull Requests, Changes Made, Test Procedure (Cucumber format).
