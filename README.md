# SonarGo

[![Build Status](https://travis-ci.org/SonarSource/sonar-go.svg?branch=master)](https://travis-ci.org/SonarSource/sonar-go)
[![Quality gate](https://next.sonarqube.com/sonarqube/api/project_badges/measure?project=org.sonarsource.go%3Asonar-go&metric=alert_status)](https://next.sonarqube.com/sonarqube/dashboard?id=org.sonarsource.go%3Asonar-go)
[![Coverage](https://next.sonarqube.com/sonarqube/api/project_badges/measure?project=org.sonarsource.go%3Asonar-go&metric=coverage)](https://next.sonarqube.com/sonarqube/component_measures?id=org.sonarsource.go%3Asonar-go&metric=coverage) 

SonarGo is a SonarQube code analyzer for the Go language. 

## Features
 
 - [Rules](https://rules.sonarsource.com/go) for detecting bugs, vulnerabilities and code smells.
 - Metrics ([Cognitive complexity](https://www.sonarsource.com/resources/white-papers/cognitive-complexity.html), NCLOC, ...)
 - Display of code coverage
 - Duplication detection 
  

_Note: The SonarGo plugin is not compatible with the GoLang community plugin. If you are using it, you need to uninstall it to test SonarGo._

## Building

Run:

    ./gradlew build

Run build with ruling:

    git submodule update --init  # first time
    ruling=true ./gradlew build --info --no-daemon

Run plugin integration tests:

    ./gradlew integrationTest --info

## Repository Structure

This SonarQube plugin uses an intermediate representation format to provide issues and metrics on a Go project.
The intermediate format is a json file representing an UAST (Universal Abstract Syntax Tree).
This is an example: [uast.json](https://github.com/SonarSource/sonar-go/blob/master/common-rule-engine/src/test/resources/reference.java.uast.json)

Two modules are responsible for the source code conversion into a json UAST:
- **uast-generator-go** Use the native Go parser and convert the AST tree into an UAST
- **uast-generator-java** Use the sonar-java parser to produce UAST, just here to ensure that the UAST can support several programming languages.

A non-language dependent rule engine
- **common-rule-engine** Rule engine executed on the UAST

The SonarQube Go analyzer plugin 
- **sonar-go-plugin** Aggregate uast-generator-go and common-rule-engine into a plugin

#### License headers

Generate license headers for non-Go files with the command:

    ./gradlew licenseFormat

The license plugins doesn't work for Go files. At the moment we create/edit them manually.
When doing so, make sure to add a blank line after the license header, otherwise `go doc`
will treat it as documentation string. To verify that no unintended documentation was added,
run `go doc` inside uast-generator-go.

## License

Copyright 2018-2018 SonarSource.

Licensed under the [GNU Lesser General Public License, Version 3.0](http://www.gnu.org/licenses/lgpl.txt)
