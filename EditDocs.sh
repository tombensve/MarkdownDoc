#!/usr/bin/env bash
if [ "${JAVA_HOME}" == "" ]; then
    export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_141.jdk/Contents/Home
fi

${JAVA_HOME}/bin/java -jar Editor/target/MarkdownDocEditor-*-App.jar Docs/src Docs/readme Library/docs Editor/docs MavenPlugin/docs CommandLine/docs
