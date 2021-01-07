FROM clojure:openjdk-8-lein
WORKDIR /app

COPY project.clj .
RUN lein deps

COPY . .

RUN lein uberjar

ENTRYPOINT java -jar target/uberjar/stallmannotes-0.1.0-SNAPSHOT-standalone.jar 