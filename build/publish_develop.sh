#!/usr/bin/env bash
if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "develop" ];
then

    if [ "${TRAVIS_SCALA_VERSION}" == "2.11.7" ] && [ "${TRAVIS_JDK_VERSION}" == "oraclejdk8" ];
    then

        echo "The current JDK version is ${TRAVIS_JDK_VERSION}"
        echo "The current Scala version is ${TRAVIS_SCALA_VERSION}"

        echo "Creating credentials file"
        if [ -e "$HOME/.bintray/.credentials" ]; then
            echo "Bintray redentials file already exists"
        else
            mkdir -p "$HOME/.bintray/"
            touch "$HOME/.bintray/.credentials"
            echo "realm = Bintray API Realm" >> "$HOME/.bintray/.credentials"
            echo "host = api.bintray.com" >> "$HOME/.bintray/.credentials"
            echo "user = $bintray_user" >> "$HOME/.bintray/.credentials"
            echo "password = $bintray_password" >> "$HOME/.bintray/.credentials"
        fi

        if [ -e "$HOME/.bintray/.credentials" ]; then
            echo "Bintray credentials file succesfully created"
        else
            echo "Bintray credentials still not found"
        fi


        echo "Bumping release version with a patch increment from $(sbt version)"
        sbt version-bump-patch exit

        set NEW_VERSION = "$(grep 'version :=')"
        echo "New version published is ${NEW_VERSION}"

        echo "Creating Git tag for version $$NEW_VERSION"

        echo "Pushing tag to GitHub."
        git push --tags "https://${github_token}@${GH_REF}" > /dev/null 2>&1

        echo "Publishing Bintray artifact"

        "Publishing $(sbt version) to bintray"
        sbt +publish

        git checkout master
        git merge develop

        git push --all  "https://${github_token}@${GH_REF}" develop:master > /dev/null 2>&1

    else
        echo "Only publishing version for Scala 2.11.7 and Oracle JDK 8 to prevent multiple artifacts"
    fi
else
    echo "This is either a pull request or the branch is not develop, deployment not necessary"
fi
