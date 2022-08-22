pushd ..

mvn -DskipTests=true -Dcheckstyle.skip=true clean install

popd

mvn -DskipTests=true -Dcheckstyle.skip=true clean install dockerfile:build -P release