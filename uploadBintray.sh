# the weired repetition is to create a workaround for the unreliable artifact upload of the bintray plugin -> usually we have missing random artifacts

./gradlew clean build generatePomFileForMavenJavaPublication publishMavenJavaPublicationToMavenLocal
./gradlew generatePomFileForMavenJavaPublication publishMavenJavaPublicationToMavenLocal bintrayUpload
