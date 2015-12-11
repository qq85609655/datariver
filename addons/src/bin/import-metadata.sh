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

ALL_BRIDGES="dxt-bridge hive-bridge odps-bridge workflow-bridge"
ALL_BRIS="dxt hive odps workflow"

#################################################################################
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
for i in "${BASEDIR}/bridge/common/"*.jar; do
  METADATACPPATH="${METADATACPPATH}:$i"
done

for i in "${BASEDIR}/hook/common/"*.jar; do
  METADATACPPATH="${METADATACPPATH}:$i"
done

# log dir for applications
METADATA_LOG_DIR="${METADATA_LOG_DIR:-$BASEDIR/logs}"
export METADATA_LOG_DIR

###########################################################################################
BRIDGES="${@}"
BRIDGE_CP=""

if [ "Y${BRIDGES}" == "Y" ]; then
    echo "!!! Usage:" &&
    echo "${0} inputs," &&
    echo "the inputs should one/any of '${ALL_BRIS}' or '${ALL_BRIDGES}'," &&
    echo "specify 'all' for all the bridges." &&
    exit 0
elif [ "Y${BRIDGES}" == "Yall" ]; then
    BRIDGES=${ALL_BRIDGES}
fi

for bridge in ${BRIDGES}; do
    case ${bridge} in
    bridge-common | common)
        BRIDGE_DIR=common
        MAIN_CLASS=org.apache.atlas.common.bridge.CommonMetaStoreBridge
        ;;
    dxt-bridge | dxt)
        BRIDGE_DIR=dxt
        MAIN_CLASS=org.apache.atlas.dxt.bridge.DxtMetaStoreBridge
        ;;
    hive-bridge | hive)
        BRIDGE_DIR=hive
        MAIN_CLASS=org.apache.atlas.hive.bridge.HiveMetaStoreBridge

        if [ ! -z "$HIVE_CONF_DIR" ]; then
            BRIDGE_CP=$HIVE_CONF_DIR
        elif [ ! -z "$HIVE_HOME" ]; then
            BRIDGE_CP="$HIVE_HOME/conf"
        elif [ -e /etc/hive/conf ]; then
            BRIDGE_CP="/etc/hive/conf"
        else
            echo "Could not find a valid HIVE configuration"
            exit 1
        fi
        ;;
    odps-bridge | odps)
        BRIDGE_DIR=odps
        MAIN_CLASS=org.apache.atlas.odps.bridge.OdpsMetaStoreBridge
        ;;
    workflow-bridge | workflow)
        BRIDGE_DIR=workflow
        MAIN_CLASS=org.apache.atlas.workFlow.bridge.WorkFlowMetaStroreBridge
        ;;
    *)
        # [[ "${ALL_BRIDGES[@]/${bridge}/}" == "${ALL_BRIDGES[@]}" ]] &&
        echo "!!! The input type '${bridge}' is invalid, should be one of " &&
        echo "!!! '${ALL_BRIS}'," &&
        echo "!or '${ALL_BRIDGES}'." &&
        exit 1
    esac

    LOG_FILE=import-metadata-${BRIDGE_DIR}.log

    for i in "${BASEDIR}/bridge/${BRIDGE_DIR}/"*.jar; do
      METADATACPPATH="${METADATACPPATH}:$i"
    done

    for i in "${BASEDIR}/hook/${BRIDGE_DIR}/"*.jar; do
      METADATACPPATH="${METADATACPPATH}:$i"
    done

    JAVA_PROPERTIES="${METADATA_OPTS} -Datlas.log.dir=${METADATA_LOG_DIR} -Datlas.log.file=${LOG_FILE} -Dlog4j.configuration=atlas-log4j.xml -Datlas.home=${BASEDIR} -Datlas.conf=${METADATA_CONF}"

    echo "Using '${BRIDGE_DIR}' configuration directory '[${BRIDGE_CP}]'"
    echo ">>>> Logs for import meta data of '${BRIDGE_DIR}' are in '${METADATA_LOG_DIR}/${LOG_FILE}'"
    START_TIME=`date +%Y-%m-%d\ %H:%M:%S-%N`
    ${JAVA_BIN} ${JAVA_PROPERTIES} -cp ${BRIDGE_CP}:${METADATACPPATH} ${MAIN_CLASS}
    END_TIME=`date +%Y-%m-%d\ %H:%M:%S-%N`
    echo "${START_TIME}, start import meta data of '${BRIDGE_DIR}',"
    echo "${END_TIME}, end import meta data of '${BRIDGE_DIR}'."

    RETVAL=$?
    [ $RETVAL -eq 0 ] && echo "Meta Data of '${BRIDGE_DIR}' imported successfully!!!"
    [ $RETVAL -ne 0 ] && echo "Failed to import Meta Data of '${BRIDGE_DIR}'!!!"
    echo ""
done





