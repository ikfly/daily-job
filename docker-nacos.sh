docker pull nacos/nacos-server &&
docker  run \
--name nacos -d \
-p 8848:8848 \
-p 9848:9848 \
-p 9849:9849 \
--restart=always \
-e JVM_XMS=256m \
-e JVM_XMX=256m \
-e MODE=standalone \
-e PREFER_HOST_MODE=hostname \
-v /opt/nacos/logs:/home/nacos/logs \
nacos/nacos-server
