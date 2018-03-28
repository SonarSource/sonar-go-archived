# sonar-go

## Building

Run:

    ./gradlew build

Run with ruling:

    git submodule update --init  # first time
    ruling=true ./gradlew build --no-daemon

## License headers

Generate license headers for non-Go files with the command:

    ./gradlew licenseFormat

The license plugins doesn't work for Go files. At the moment we create/edit them manually.
When doing so, make sure to add a blank line after the license header, otherwise `go doc`
will treat it as documentation string. To verify that no unintended documentation was added,
run `go doc` inside uast-generator-go.

## License

Copyright 2018-2018 SonarSource.

Licensed under the [GNU Lesser General Public License, Version 3.0](http://www.gnu.org/licenses/lgpl.txt)
