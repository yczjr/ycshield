ARG BASE_PREFIX=
# ---------- 构建 ----------
FROM ${BASE_PREFIX}maven:3.9-eclipse-temurin-17 AS build
ARG MAVEN_MIRROR=https://maven.aliyun.com/repository/public
WORKDIR /build
COPY pom.xml .
RUN mkdir -p /root/.m2 \
 && printf '<settings><mirrors><mirror><id>cn</id><mirrorOf>*</mirrorOf><url>%s</url></mirror></mirrors></settings>' "$MAVEN_MIRROR" > /root/.m2/settings.xml \
 && mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- 运行 ----------
FROM ${BASE_PREFIX}eclipse-temurin:17-jre-alpine
ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms256m -Xmx768m -Duser.timezone=Asia/Shanghai"
WORKDIR /app
COPY --from=build /build/target/blued-api.jar app.jar
EXPOSE 8081
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
