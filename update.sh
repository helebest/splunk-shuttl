#!/bin/bash
DIR="$( cd "$( dirname "$0" )" && pwd )"
rsync -vr ${DIR}/package/* ${DIR}/build-cache/splunk/etc/apps/shep/
${DIR}/build-cache/splunk/bin/splunk restart -f 