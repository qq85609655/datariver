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

for i in "${BASEDIR}/bridge/odps/"*.jar; do
  METADATACPPATH="${METADATACPPATH}:$i"
done

for i in "${BASEDIR}/hook/odps/"*.jar; do
  METADATACPPATH="${METADATACPPATH}:$i"
done

# log dir for applications
METADATA_LOG_DIR="${METADATA_LOG_DIR:-$BASEDIR/logs}"
export METADATA_LOG_DIR

JAVA_PROPERTIES="$METADATA_OPTS -Datlas.log.dir=$METADATA_LOG_DIR -Datlas.log.file=import-odps.log"
shift

while [[ ${1} =~ ^\-D ]]; do
  JAVA_PROPERTIES="${JAVA_PROPERTIES} ${1}"
  shift
done
TIME=`date +%Y%m%d%H%M%s`

#  #Add odps conf in classpath
#  if [ ! -z "$ODPS_CONF_DIR" ]; then
#      ODPS_CP=$ODPS_CONF_DIR
#  elif [ ! -z "$ODPS_HOME" ]; then
#      ODPS_CP="$ODPS_HOME/conf"
#  elif [ -e /etc/odps/conf ]; then
#      ODPS_CP="/etc/odps/conf"
#  else
#      echo "Could not find a valid ODPS configuration"
#      exit 1
#  fi
#  export ODPS_CP
#  echo Using ODPS configuration directory [$ODPS_CP]
echo "Logs for import are in $METADATA_LOG_DIR/import-odps.log"

${JAVA_BIN} ${JAVA_PROPERTIES} -cp ${ODPS_CP}:${METADATACPPATH} org.apache.atlas.odps.bridge.testOdps

RETVAL=$?
[ $RETVAL -eq 0 ] && echo odps Data Model imported successfully!!!
[ $RETVAL -ne 0 ] && echo Failed to import odps Data Model!!!
echo "test import result:"
curl -s http://localhost:21000/api/atlas/types/odps_object_type | sed 's#\\"#"#g' | sed 's#\\n#\r\n#g' 1>>odpsresult.txt
curl -s http://localhost:21000/api/atlas/types/odps_resource_type | sed 's#\\"#"#g' | sed 's#\\n#\r\n#g' 1>>odpsresult.txt
curl -s http://localhost:21000/api/atlas/types/odps_resource | sed 's#\\"#"#g' | sed 's#\\n#\r\n#g' 1>>odpsresult.txt
curl -s http://localhost:21000/api/atlas/types/odps_task | sed 's#\\"#"#g' | sed 's#\\n#\r\n#g' 1>>odpsresult.txt
curl -s http://localhost:21000/api/atlas/types/odps_column | sed 's#\\"#"#g' | sed 's#\\n#\r\n#g' 1>>odpsresult.txt
curl -s http://localhost:21000/api/atlas/types/odps_partition | sed 's#\\"#"#g' | sed 's#\\n#\r\n#g' 1>>odpsresult.txt
curl -s http://localhost:21000/api/atlas/types/odps_instance | sed 's#\\"#"#g' | sed 's#\\n#\r\n#g' 1>>odpsresult.txt
curl -s http://localhost:21000/api/atlas/types/odps_table | sed 's#\\"#"#g' | sed 's#\\n#\r\n#g' 1>>odpsresult.txt
curl -s http://localhost:21000/api/atlas/types/odps_project | sed 's#\\"#"#g' | sed 's#\\n#\r\n#g' 1>>odpsresult.txt
curl -s http://localhost:21000/api/atlas/types/odps_accinfo | sed 's#\\"#"#g' | sed 's#\\n#\r\n#g' 1>>odpsresult.txt
echo "check results in odpsresult.txt using 'more odpsresult.txt'"