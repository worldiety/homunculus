
## Config

Create signing key:

    gpg --full-generate-key
    host keyserver.ubuntu.com
    gpg --keyserver hkp://<keyserver-ip>:80 --send-keys <last 8 letters from pub key>
    # create ring file:
    gpg --export-secret-keys -o ~/.gnupg/secring.kbx

create file local.properties with following variables:

    sdk.dir=<Android SDK-Folder>
    # take following from bitwarden:
    ossrhUsername=<ossrhUsername>
    ossrhPassword=<ossrhPassword>
    sonatypeStagingProfileId=<sonatypeStagingProfileId>
    signing.keyId=<last 8 letters from pub key>
    signing.password=<key password>
    signing.secretKeyRingFile=<ring file>

## Release

Run 

    uploadMavenCentral.sh

Visit maven central account: https://s01.oss.sonatype.org/#stagingRepositories

Login with (see Bitwarden) 
    
    <ossrhUsername>
    <ossrhPassword>

Check repository content, then *close*
Wait until ready, then *release*

