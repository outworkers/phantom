java -Xmx1024M -Xss2M -XX:MaxPermSize=1024M -XX:+CMSClassUnloadingEnabled -jar \
 `dirname $0`/sbt-launch.jar "$@"
