#!/usr/bin/env bash
# clean everything and start from scratch
./gradlew clean

# build the first jar, so that the code generator can compile
./gradlew :hcf-android-core:build

# build the second jar, so that the code generator can compile
./gradlew :hcf-android-component:build

# generate code
./gradlew :hcf-codegen:run

# upload to nexus
#./gradlew uploadArchives --warning-mode all
./gradlew publish
# the weired repetition is to create a workaround for the unreliable artifact upload of the bintray plugin -> usually we have missing random artifacts
#./gradlew build generatePomFileForMavenJavaPublication publishMavenJavaPublicationToMavenLocal
#./gradlew generatePomFileForMavenJavaPublication publishMavenJavaPublicationToMavenLocal bintrayUpload
