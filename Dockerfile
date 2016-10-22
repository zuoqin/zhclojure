FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/zhluminus.jar /zhluminus/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/zhluminus/app.jar"]
