#!/usr/bin/env bash
set -euo pipefail

function configureTravis {
  mkdir -p ~/.local
  curl -sSL https://github.com/SonarSource/travis-utils/tarball/v48 | tar zx --strip-components 1 -C ~/.local
  source ~/.local/bin/install
}
configureTravis

# configure environment variables for Artifactory
export GIT_COMMIT=$TRAVIS_COMMIT
export BUILD_NUMBER=$TRAVIS_BUILD_NUMBER
if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
  export GIT_BRANCH=$TRAVIS_BRANCH
  unset PULL_REQUEST_BRANCH_TARGET
  unset PULL_REQUEST_NUMBER
else
  export GIT_BRANCH=$TRAVIS_PULL_REQUEST_BRANCH
  export PULL_REQUEST_BRANCH_TARGET=$TRAVIS_BRANCH
  export PULL_REQUEST_NUMBER=$TRAVIS_PULL_REQUEST
fi

gradle_cmd="./gradlew --no-daemon --console plain"
sonar_analysis="-DbuildNumber=$BUILD_NUMBER \
        -Dsonar.host.url=$SONAR_HOST_URL \
        -Dsonar.login=$SONAR_TOKEN \
        -Dsonar.analysis.buildNumber=$BUILD_NUMBER \
        -Dsonar.analysis.pipeline=$BUILD_NUMBER \
        -Dsonar.analysis.repository=$TRAVIS_REPO_SLUG "

# Used by Next
export INITIAL_VERSION=$(cat gradle.properties | grep version | awk -F= '{print $2}')

if [ "$TRAVIS_BRANCH" == "master" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
    echo 'Build and analyze master'
    ${gradle_cmd} build sonarqube artifactoryPublish -Prelease=true \
        ${sonar_analysis} \
        -Dsonar.analysis.sha1=$GIT_COMMIT \
        -Dsonar.projectVersion=$INITIAL_VERSION

elif [ "$TRAVIS_PULL_REQUEST" != "false" ] && [ -n "${GITHUB_TOKEN:-}" ]; then
    echo 'Build and analyze pull request'
    ${gradle_cmd} build sonarqube artifactoryPublish \
        ${sonar_analysis} \
        -Dsonar.analysis.prNumber=$TRAVIS_PULL_REQUEST \
        -Dsonar.analysis.sha1=$TRAVIS_PULL_REQUEST_SHA \
        -Dsonar.pullrequest.key=$TRAVIS_PULL_REQUEST \
        -Dsonar.pullrequest.branch=$TRAVIS_PULL_REQUEST_BRANCH \
        -Dsonar.pullrequest.base=$TRAVIS_BRANCH \
        -Dsonar.pullrequest.provider=github \
        -Dsonar.pullrequest.github.repository=$TRAVIS_REPO_SLUG
else
    echo 'Build feature branch or external pull request'
    ${gradle_cmd} -DbuildNumber=$BUILD_NUMBER build
fi

