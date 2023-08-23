#!/usr/bin/env bash
# clean everything and start from scratch
./gradlew clean

# build the first jar, so that the code generator can compile
./gradlew :hcf-android-core:build

# build the second jar, so that the code generator can compile
./gradlew :hcf-android-component:build

# generate code
./gradlew :hcf-codegen:run