#!/bin/sh
#
# Since the "versions-maven-plugin" does not fix the parent poms versions thus damaging the whole maven tree I have
# now tagged all relevant version tags with <!--VER--> before the version number. This is then used by this script
# to change the version number of all tagged versions in the pom.xml files in the project.
#

version=1.2.7
oldVersion=1.2.6

for pom in `find . -name 'pom.xml' -print`
do
    echo "Updating ${pom} ..."
    cat ${pom} | sed 's%><!--VER-->.*<%><!--VER-->'${version}'<%g' > ${pom}.new
    mv ${pom} ${pom}.old
    mv ${pom}.new ${pom}
    rm ${pom}.old
done

echo "Updating README.md ..."
cat README.md | sed "s/${oldVersion}/${version}/g" > README.new
mv README.md README.old
mv README.new README.md
rm README.old
