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

for i in "${BASEDIR}/bridge/workflow/"*.jar; do
  METADATACPPATH="${METADATACPPATH}:$i"
done

for i in "${BASEDIR}/hook/workflow/"*.jar; do
  METADATACPPATH="${METADATACPPATH}:$i"
done

# log dir for applications
METADATA_LOG_DIR="${METADATA_LOG_DIR:-$BASEDIR/logs}"
export METADATA_LOG_DIR

JAVA_PROPERTIES="$METADATA_OPTS -Datlas.log.dir=$METADATA_LOG_DIR -Datlas.log.file=import-workflow.log"
shift

while [[ ${1} =~ ^\-D ]]; do
  JAVA_PROPERTIES="${JAVA_PROPERTIES} ${1}"
  shift
done
TIME=`date +%Y%m%d%H%M%s`

#  #Add workflow conf in classpath
#  if [ ! -z "$WORKFLOW_CONF_DIR" ]; then
#      WORKFLOW_CP=$WORKFLOW_CONF_DIR
#  elif [ ! -z "$WORKFLOW_HOME" ]; then
#      WORKFLOW_CP="$WORKFLOW_HOME/conf"
#  elif [ -e /etc/workflow/conf ]; then
#      WORKFLOW_CP="/etc/workflow/conf"
#  else
#      echo "Could not find a valid WORKFLOW configuration"
#      exit 1
#  fi
#  export WORKFLOW_CP
#  echo Using WORKFLOW configuration directory [$WORKFLOW_CP]
echo "Logs for import are in $METADATA_LOG_DIR/import-workflow.log"

#${JAVA_BIN} ${JAVA_PROPERTIES} -cp ${WORKFLOW_CP}:${METADATACPPATH} org.apache.atlas.workFlow.model.WorkFlowDataModelGenerator
${JAVA_BIN} ${JAVA_PROPERTIES} -cp ${WORKFLOW_CP}:${METADATACPPATH} org.apache.atlas.workFlow.bridge.testWorkflow

RETVAL=$?
[ $RETVAL -eq 0 ] && echo WORKFLOW Data Model imported successfully!!!
[ $RETVAL -ne 0 ] && echo Failed to import WORKFLOW Data Model!!!
