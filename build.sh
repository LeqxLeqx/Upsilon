#!/bin/bash

if [[ !(-e bin) ]] ; then
  mkdir bin
fi

javac -Xlint -d bin `find src -name '*.java'`
if [[ $? != 0 ]] ; then
  echo javac failed
  exit 1
fi

echo 'Manifest-Version: 1.0' > bin/Manifest.txt

cd bin
jar cfm upsilon.jar Manifest.txt `find . -name '*.class'`
if [[ $? != 0 ]] ; then
  echo jar failed
  exit 2
fi

echo 'public class VersionGetter {' > VersionGetter.java
echo '  public static void main(String[] args) {' >> VersionGetter.java
echo '    System.out.print(upsilon.Upsilon.getVersion());' >> \
  VersionGetter.java
echo '  }' >> VersionGetter.java
echo '}' >> VersionGetter.java

javac -cp .:upsilon.jar VersionGetter.java
version=`java -cp .:upsilon.jar VersionGetter`

cd ..

mv bin/upsilon.jar ./upsilon-$version.jar







