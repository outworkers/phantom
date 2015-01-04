#!/bin/sh

root=$(
	cd $(dirname $(readlink $0 || echo $0))/..
	pwd
)

sbtver=0.13.1
sbtjar=sbt-launch.jar
sbtsum=79e367c11fc2294f865c6ecc47b8886c

download() {
	echo "downloading ${sbtjar}" 1>&2
	curl -O "http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/${sbtver}/${sbtjar}"
}

sbtjar_md5() {
	openssl md5 < ${sbtjar} | cut -f2 -d'=' | awk '{print $1}'
}

if [ ! -f "${sbtjar}" ]; then
	download
fi

test -f "${sbtjar}" || exit 1

jarmd5=$(sbtjar_md5)
if [ "${jarmd5}" != "${sbtsum}" ]; then
	echo "Bad MD5 checksum on ${sbtjar}!" 1>&2
	echo "Moving current sbt-launch.jar to sbt-launch.jar.old!" 1>&2
	mv "${sbtjar}" "${sbtjar}.old"
	download

	jarmd5=$(sbtjar_md5)
	if [ "${jarmd5}" != "${sbtsum}" ]; then
		echo "Bad MD5 checksum *AGAIN*!" 1>&2
		exit 1
	fi
fi

java -ea -server $SBT_OPTS $JAVA_OPTS		\
	-XX:+AggressiveOpts             		\
	-XX:+OptimizeStringConcat			    \
	-XX:+UseConcMarkSweepGC               	\
	-XX:+CMSParallelRemarkEnabled   		\
	-XX:+CMSClassUnloadingEnabled   		\
	-XX:+CMSIncrementalMode         		\
	-Dio.netty.epollBugWorkaround=true		\
	-Xss2M                                  \
	-Xms128M					            \
	-Xmx1024M					            \
	-XX:MaxPermSize=1024M                   \
	-jar $sbtjar "$@"

