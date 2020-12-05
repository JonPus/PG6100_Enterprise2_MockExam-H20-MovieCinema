FROM adoptopenjdk/openjdk11:alpine

COPY target/movies.jar .

CMD java -Dspring.profiles.active=FakeData  -jar movies.jar