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

## License

Copyright 2018-2018 SonarSource.

Licensed under the [GNU Lesser General Public License, Version 3.0](http://www.gnu.org/licenses/lgpl.txt)
