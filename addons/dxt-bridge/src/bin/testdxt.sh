#!/bin/bash
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License. See accompanying LICENSE file.
#

# resolve links - $0 may be a softlink
PRG="${0}"

while [ -h "${PRG}" ]; do
  ls=`ls -ld "${PRG}"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "${PRG}"`/"$link"
  fi
done

BASEDIR=`dirname ${PRG}`
BASEDIR=`cd ${BASEDIR}/..;pwd`

if [ -z "$METADATA_CONF" ]; then
  METADATA_CONF=${BASEDIR}/conf
fi
export METADATA_CONF

if [ -f "${METADATA_CONF}/atlas-env.sh" ]; then
  . "${METADATA_CONF}/atlas-env.sh"
fi

if test -z ${JAVA_HOME}
then
    JAVA_BIN=`which java`
    JAR_BIN=`which jar`
else
    JAVA_BIN=${JAVA_HOME}/bin/java
    JAR_BIN=${JAVA_HOME}/bin/jar
fi
export JAVA_BIN

if [ ! -e $JAVA_BIN ] || [ ! -e $JAR_BIN ]; then
  echo "$JAVA_BIN and/or $JAR_BIN not found on the system. Please make sure java and jar commands are available."
  exit 1
fi

METADATACPPATH="$METADATA_CONF"

for i in "${BASEDIR}/bridge/dxt/"*.jar; do
  METADATACPPATH="${METADATACPPATH}:$i"
done

for i in "${BASEDIR}/hook/dxt/"*.jar; do
  METADATACPPATH="${METADATACPPATH}:$i"
done

# log dir for applications
METADATA_LOG_DIR="${METADATA_LOG_DIR:-$BASEDIR/logs}"
export METADATA_LOG_DIR

JAVA_PROPERTIES="$METADATA_OPTS -Datlas.log.dir=$METADATA_LOG_DIR -Datlas.log.file=import-dxt.log"
shift

while [[ ${1} =~ ^\-D ]]; do
  JAVA_PROPERTIES="${JAVA_PROPERTIES} ${1}"
  shift
done
TIME=`date +%Y%m%d%H%M%s`

#  #Add dxt conf in classpath
#  if [ ! -z "$DXT_CONF_DIR" ]; then
#      DXT_CP=$DXT_CONF_DIR
#  elif [ ! -z "$DXT_HOME" ]; then
#      DXT_CP="$DXT_HOME/conf"
#  elif [ -e /etc/dxt/conf ]; then
#      DXT_CP="/etc/dxt/conf"
#  else
#      echo "Could not find a valid DXT configuration"
#      exit 1
#  fi
#  export DXT_CP
#  echo Using DXT configuration directory [$DXT_CP]
echo "Logs for import are in $METADATA_LOG_DIR/import-dxt.log"

#${JAVA_BIN} ${JAVA_PROPERTIES} -cp ${DXT_CP}:${METADATACPPATH} org.apache.atlas.dxt.model.DxtDataModelGenerator
${JAVA_BIN} ${JAVA_PROPERTIES} -cp ${DXT_CP}:${METADATACPPATH} org.apache.atlas.dxt.bridge.testDxt

RETVAL=$?
[ $RETVAL -eq 0 ] && echo DXT Data Model imported successfully!!!
[ $RETVAL -ne 0 ] && echo Failed to import DXT Data Model!!!
