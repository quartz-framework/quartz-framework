#!/bin/bash

PLUGIN_VERSION=$(./mvnw -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive exec:exec)

IFS='.' read -r MAJOR MINOR PATCH <<< "${PLUGIN_VERSION%-SNAPSHOT}"

if [[ "$1" == "--rollback" ]]; then
    if [[ "$2" == "--major" ]]; then
        MAJOR=$((MAJOR - 1))
        if [[ $MAJOR -lt 0 ]]; then MAJOR=0; fi
        MINOR=0
        PATCH=0
    elif [[ "$2" == "--minor" ]]; then
        MINOR=$((MINOR - 1))
        if [[ $MINOR -lt 0 ]]; then MINOR=0; fi
        PATCH=0
    else
        PATCH=$((PATCH - 1))
        if [[ $PATCH -lt 0 ]]; then PATCH=0; fi
    fi
else
    if [[ "$1" == "--major" ]]; then
        MAJOR=$((MAJOR + 1))
        MINOR=0
        PATCH=0
    elif [[ "$1" == "--minor" ]]; then
        MINOR=$((MINOR + 1))
        PATCH=0
    else
        PATCH=$((PATCH + 1))
    fi
fi

if [[ "$3" == "--snapshot" ]] || [[ "$2" == "--snapshot" ]] || [[ "$1" == "--snapshot" ]]; then
    NEW_VERSION="${MAJOR}.${MINOR}.${PATCH}-SNAPSHOT"
else
    NEW_VERSION="${MAJOR}.${MINOR}.${PATCH}"
fi

./mvnw versions:set -DnewVersion="${NEW_VERSION}"

echo "Version updated to ${NEW_VERSION}"