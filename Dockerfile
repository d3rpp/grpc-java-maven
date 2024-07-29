FROM alpine:latest AS builder

RUN apk add --no-cache protoc maven

WORKDIR /build

COPY ./ ./

RUN mvn package

FROM eclipse-temurin:8u422-b05-jre-alpine

WORKDIR /run
COPY --from=builder /build/target/RouteGuideServer.jar .

CMD [ "java", "-jar", "/run/RouteGuideServer.jar" ]