#!/bin/bash

./gradlew clean spotlessCheck detekt checkstyle pmd bundleRelease lintRelease testReleaseUnitTest --no-daemon
cp -r app/build/outputs/bundle/release/app-release.aab ~/Google\ Drive/MTGCardsInfo/MTGSearch.aab