# 멀티 스테이지 빌드 방법 사용

# 첫번째 스테이지
FROM openjdk:11 as stage1
WORKDIR /app

# . == /app밑에 gradlew 파일로
COPY gradlew .
#/app/gralde 폴더로 저장
COPY gradle gradle
COPY src src
COPY build.gradle .
COPY settings.gradle .

#docker container 안에서 bootJar 명령어 .jar파일 생성
RUN chmod 777 gradlew
RUN ./gradlew bootJar

#두번째 스테이지
#jar 파일 만들어지면 나머지 copy들을 필요없어짐. 두번째 스테이지에 jar 파일만 받기. 컨테이너 이미지 사이즈 감소할 수 있음
FROM openjdk:11
WORKDIR /app
#stage1에 있는 jar를 stage2의 app.jar라는 이름으로 copy
COPY --from=stage1 /app/build/libs/*.jar app.jar

#CMD or ENTRIYPOINT를 통해 컨테이너 실행. 엔트리포인트 많이 사용함
#app.jar 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

#docker 컨테이너 내에서 밖의 전체 hot를 지칭하는 도메인 : host.docker.internal
#위에서 localhost 바꾸면 빌드부터 다시해야함. 실행하는 시점에서 바꿔주기
#docker run -d -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3306/board ordersystem:latest
# docker 컨테이너 실행시에 볼륨을 설정할 때는 -v옵션 사용  -v host경로:docker log 경로
#docker run -d -p 8081:8080 -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3306/board -v C:\Users\Playdata\Desktop\tmp_logs:/app/logs spring_test:v1


#docker run -d -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3306/order_system shinseunghyun/ordersystem:latest
