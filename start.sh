docker rm `docker stop daily-job`
docker rmi daily-job:v1.0.0
docker build -t daily-job:v1.0.0 .
docker run \
--name daily-job -d \
-p 8888:8888 \
--restart=always \
-e JVM_XMS=256m \
-e JVM_XMX=256m \
daily-job:v1.0.0