function incrementVersionCode {
    GRADLE_PROPERTIES=$HOME"/.gradle/gradle.properties"
    export GRADLE_PROPERTIES
    echo "Gradle Properties should exist at $GRADLE_PROPERTIES"

    if [ ! -f "$GRADLE_PROPERTIES" ]; then
        echo "Gradle Properties does not exist"

        echo "Creating Gradle Properties file..."
        mkdir ~/.gradle -p
        touch $GRADLE_PROPERTIES

        echo "Writing VERSION_CODE to gradle.properties..."
        echo "VERSION_CODE=$CIRCLE_BUILD_NUM" >> $GRADLE_PROPERTIES

    else
        echo "Gradle Properties does exist"
        echo "Writing VERSION_CODE to gradle.properties..."
        echo "VERSION_CODE=$CIRCLE_BUILD_NUM" >> $GRADLE_PROPERTIES
    fi
}

function incrementVersionName {
    ./gradlew incrementPatch
    VERSION=$(./gradlew -q getVersionName)
    echo "export VERSION_NAME=$VERSION" >> "$BASH_ENV"
    source "$BASH_ENV"
}