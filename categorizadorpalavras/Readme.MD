# Exercício Categorizador de  Palavras
Nesse exercício vamos criar um projeto com Spring Boot em Kafka Streams
1. Ambiente
Iniciar Kafka
```
docker-compose up
```
Criar tópicos
Tópico de `entrada`
```
docker-compose exec kafka  \
kafka-topics --create --topic entrada --partitions 1 --replication-factor 1 --if-not-exists --zookeeper zookeeper:2181
```
Tópicos de `resultados`
```
docker-compose exec kafka  \
kafka-topics --create --topic pequenas --partitions 1 --replication-factor 1 --if-not-exists --zookeeper zookeeper:2181
 
docker-compose exec kafka  \
kafka-topics --create --topic medias --partitions 1 --replication-factor 1 --if-not-exists --zookeeper zookeeper:2181
 
docker-compose exec kafka  \
kafka-topics --create --topic grandes --partitions 1 --replication-factor 1 --if-not-exists --zookeeper zookeeper:2181
```

2. Criar Projeto

Usando o [Spring Initializr](https://start.spring.io/) vamos criar um novo projeto em Java com as dependências “Cloud Stream” e “Spring for Apache Kafka Streams".

* [Link da Preguiça](https://start.spring.io/#!type=maven-project&language=java&platformVersion=2.3.3.RELEASE&packaging=jar&jvmVersion=11&groupId=com.facef.kafka&artifactId=categorizadorpalavras&name=categorizadorpalavras&description=Demo%20project%20for%20Spring%20Boot&packageName=com.facef.kafka.categorizadorpalavras&dependencies=cloud-stream,kafka-streams) - Tudo pronto :)

Importe o Projeto no Eclipse
* Extraia o diretório do projeto em uma pasta de trabalho
* Importe pelo menu "File > Import > Maven >  Existing Maven Projects" e selecione o diretório onde o projeto foi extraído.

3. Criando o Processor

Com base no exercício anterior, agora vamos utilizar da função `branch`, onde podemos passar uma lista de `Predicate` que iram fazer as validações e os direcionamentos para cada tópico de saída, as palavras pequenas no tópico `pequenas` e assim por diante. 

```
@SpringBootApplication
public class CategorizadorpalavrasApplication {
 
   public static void main(String[] args) {
       SpringApplication.run(CategorizadorpalavrasApplication.class, args);
   }
  
  
   @Bean
   @SuppressWarnings("unchecked")
   public Function<KStream<Object, String>, KStream<Object, WordCount>[]> process() {
 
       Predicate<Object, WordCount> isSmall = (k, v) -> v.getKey().length() < 1;
       Predicate<Object, WordCount> isMedium = (k, v) -> v.getKey().length() < 1;
       Predicate<Object, WordCount> isLarge = (k, v) -> v.getKey().length() < 1;
 
       return input -> input.flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
               .map((key, value) -> new KeyValue<>(value, value))
               .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
               .windowedBy(TimeWindows.of(Duration.ofSeconds(5)))
               .count(Materialized.as("WordCounts-1"))
               .toStream()
               .map((key, value) -> new KeyValue<>(null, new WordCount(key.key(), value, new Date(key.window().start()), new Date(key.window().end()))))
               .branch(isSmall, isMedium, isLarge);
   }
 
}
 
```

4. Confira as configurações de para conexão com o kafka.

No aquivo `application.properties` configure as propriedades para o broker.
```
## Endereço e porta do kafka
spring.kafka.bootstrapServers=localhost:9092
## Configuração do tópico de entrada.
##  process-in-0, é a referência auto gerada pelo spring ao processo de consumo process que criamos
spring.cloud.stream.bindings.process-in-0.destination=entrada
## Primeira saída vinculada ao primeiro Predicate da lista
spring.cloud.stream.bindings.process-out-0.destination=pequenas
spring.cloud.stream.bindings.process-out-1.destination=medias
spring.cloud.stream.bindings.process-out-2.destination=grandes
 
```

5. Iniciando o projeto

Para iniciar o projeto basta rodar o comando.
```
./mvnw spring-boot:run
```

6. Gerando alguns eventos
 
Para gerar eventos podemos usar o script do próprio kafka para emitir alguns eventos no tópico `entrada`
```
docker-compose exec kafka kafka-console-producer --bootstrap-server localhost:9092 --topic entrada
oi
tudo
bem ou não
...
```
 
8. Conferido os resultados
 
Confira se as palavras com até 3 caracteres estão sendo direcionadas para o pequenas, as com até 6 no médias e as com mais de 6 no grandes, caso necessário façam os ajustes no código.
```
docker-compose exec kafka  \
kafka-console-consumer --bootstrap-server localhost:9092 --topic pequenas --from-beginning
```
 
```
docker-compose exec kafka  \
kafka-console-consumer --bootstrap-server localhost:9092 --topic medias --from-beginning
```
```
docker-compose exec kafka  \
kafka-console-consumer --bootstrap-server localhost:9092 --topic grandes --from-beginning
```
 
 
 
 

