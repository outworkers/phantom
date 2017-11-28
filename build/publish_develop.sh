#!/usr/bin/env bash

echo "Pull request: ${TRAVIS_PULL_REQUEST}; Branch: ${TRAVIS_BRANCH}"
TARGET_SCALA_VERSION="2.12.4"

if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "develop" ];
then
    if [ "${TRAVIS_SCALA_VERSION}" == "${TARGET_SCALA_VERSION}" ] && [ "${TRAVIS_JDK_VERSION}" == "oraclejdk8" ];
    then

        echo "Setting git user email to ci@outworkers.com"
        git config user.email "ci@outworkers.com"

        echo "Setting git user name to Travis CI"
        git config user.name "Travis CI"

        echo "The current JDK version is ${TRAVIS_JDK_VERSION}"
        echo "The current Scala version is ${TRAVIS_SCALA_VERSION}"

        echo "Creating credentials file"
        if [ -e "$HOME/.bintray/.credentials" ]; then
            echo "Bintray credentials file already exists"
        else
            mkdir -p "$HOME/.bintray/"
            touch "$HOME/.bintray/.credentials"
            echo "realm = Bintray API Realm" >> "$HOME/.bintray/.credentials"
            echo "host = api.bintray.com" >> "$HOME/.bintray/.credentials"
            echo "user = $bintray_user" >> "$HOME/.bintray/.credentials"
            echo "password = $bintray_password" >> "$HOME/.bintray/.credentials"
        fi

        if [ -e "$HOME/.bintray/.credentials" ]; then
            echo "Bintray credentials file successfully created"
        else
            echo "Bintray credentials still not found"
        fi

        if [ -e "$HOME/.ivy2/.credentials" ]; then
            echo "Maven credentials file already exists"
        else
        mkdir -p "$HOME/.ivy2/"
            touch "$HOME/.ivy2/.credentials"
            echo "realm = Sonatype Nexus Repository Manager" >> "$HOME/.ivy2/.credentials"
            echo "host = oss.sonatype.org" >> "$HOME/.ivy2/.credentials"
            echo "user = $maven_user" >> "$HOME/.ivy2/.credentials"
            echo "password = $maven_password" >> "$HOME/.ivy2/.credentials"
        fi

        if [ -e "$HOME/.ivy2/.credentials" ]; then
            echo "Maven credentials file successfully created"
        else
            echo "Maven credentials still not found"
        fi

        if [ -e "$HOME/.bintray/.credentials" ]; then
            echo "Bintray credentials file successfully created"
        else
            echo "Bintray credentials still not found"
        fi

        COMMIT_MSG=$(git log -1 --pretty=%B 2>&1)
        COMMIT_SKIP_MESSAGE="[version skip]"

        echo "Last commit message $COMMIT_MSG"
        echo "Commit skip message $COMMIT_SKIP_MESSAGE"

        if [[ $COMMIT_MSG == *"$COMMIT_SKIP_MESSAGE"* ]]
        then
            echo "Skipping version bump and simply tagging"
            sbt git-tag
        else
            sbt version-bump-patch git-tag

            echo "Pushing tag to GitHub."
            git push --tags "https://${github_token}@${GH_REF}"
        fi

        sbt "project readme" tut

        echo "Publishing version bump information and tut docs to GitHub"
        git add .
        git commit -m "TravisCI: Bumping version to match CI definition and pushing compiled documentation [ci skip]"
        git checkout -b version_branch
        git checkout -B $TRAVIS_BRANCH version_branch
        git push "https://${github_token}@${GH_REF}" $TRAVIS_BRANCH

        if [ "$TRAVIS_BRANCH" == "develop" ];
        then
            if [[ $COMMIT_MSG == *"$COMMIT_SKIP_MESSAGE"* ]];
            then
                echo "Publishing new version to Maven Central"
                echo "Creating GPG deploy key"
                openssl aes-256-cbc -K $encrypted_759d2b7e5bb0_key -iv $encrypted_759d2b7e5bb0_iv -in build/deploy.asc.enc -out build/deploy.asc -d

                echo "importing GPG key to local GBP repo"
                gpg --fast-import build/deploy.asc

                echo "Setting MAVEN_PUBLISH mode to true"
                export MAVEN_PUBLISH="true"
                export pgp_passphrase=${maven_password}
                sbt "such publishSigned"
                sbt sonatypeReleaseAll
                exit $?
            else
                echo "Skipping publication of a new Maven Artefact, the version was not bumped."
            fi
        else
            echo "Not deploying to Maven Central, branch is not develop, current branch is ${TRAVIS_BRANCH}"
        fi

        if [[ $COMMIT_MSG == *"$COMMIT_SKIP_MESSAGE"* ]];
        then
            echo "Not publishing to Bintray"
        else
            echo "Publishing new version to bintray"
            sbt "such publish"
        fi

    else
        echo "Only publishing version for Scala $TARGET_SCALA_VERSION and Oracle JDK 8 to prevent multiple artifacts"
    fi
else
    echo "This is either a pull request or the branch is not develop, deployment not necessary"
fi
