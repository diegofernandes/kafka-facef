# Exercício Conta Palavras
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
 
Tópico de `resultados`
```
docker-compose exec kafka  \
kafka-topics --create --topic resultado --partitions 1 --replication-factor 1 --if-not-exists --zookeeper zookeeper:2181
```
 
2. Criar Projeto
 
Usando o [Spring Initializr](https://start.spring.io/) vamos criar um novo projeto em Java com as dependências “Cloud Stream” e “Spring for Apache Kafka Streams".
 
* [Link da Preguiça](https://start.spring.io/#!type=maven-project&language=java&platformVersion=2.3.3.RELEASE&packaging=jar&jvmVersion=11&groupId=com.facef.kafka&artifactId=contapalavras&name=contapalavras&description=Demo%20project%20for%20Spring%20Boot&packageName=com.facef.kafka.contapalavras&dependencies=cloud-stream,kafka-streams) - Tudo pronto :)
 
Importe o Projeto no Eclipse
* Extraia o diretório do projeto em uma pasta de trabalho
* Importe pelo menu "File > Import > Maven >  Existing Maven Projects" e selecione o diretório onde o projeto foi extraído.
 
 
3. Criando o primeiro Processor
 
Na Classe principal (ContapalavrasApplication) vamos adicionar o Bean Consumer, esse código somente vai consumir e fazer um print dos eventos emitidos no broker.
 
```
@SpringBootApplication
public class ContapalavrasApplication {
 
   public static void main(String[] args) {
       SpringApplication.run(ContapalavrasApplication.class, args);
   }
 
   @Bean
   public java.util.function.Consumer<KStream<String, String>> process() {
 
       return input -> input.foreach((key, value) -> {
           System.out.println("Key: " + key + " Value: " + value);
       });
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
bem
```
 
** Você pode acompanhar a chega dos eventos na aplicação java conta palavras.
 
 
 
7. Contando palavras
 
Vamos modificar o nosso processor, para contar as palavras.
 
```
@SpringBootApplication
public class ContapalavrasApplication {
  
 
   public static final int WINDOW_SIZE_MS = 30000;
 
   public static void main(String[] args) {
       SpringApplication.run(ContapalavrasApplication.class, args);
   }
 
   @Bean
   public Function<KStream<Bytes, String>, KStream<Bytes, WordCount>> process() {
 
       return input -> input
               .flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
               .map((key, value) -> new KeyValue<>(value, value))
               .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
               .windowedBy(TimeWindows.of(Duration.ofMillis(WINDOW_SIZE_MS)))
               .count(Materialized.as("WordCounts-1"))
               .toStream()
               .map((key, value) -> new KeyValue<>(null, new WordCount(key.key(), value, new Date(key.window().start()), new Date(key.window().end()))));
   }
 
}
```
 
Vamos precisar de um POJO para carregar os resultados(`WordCount`). Crie uma nova classe chamada `WordCount`.
 
 
```
public class WordCount implements Serializable {
  
   private String key;
   private Long count;
   private Date start;
   private Date end;
  
   public WordCount() {
  
   }
  
   public WordCount(String key, Long count, Date start, Date end) {
       super();
       this.key = key;
       this.count = count;
       this.start = start;
       this.end = end;
   }
  
  
 
   @Override
   public String toString() {
       return "WordCount [key=" + key + ", count=" + count + ", start=" + start + ", end=" + end + "]";
   }
 
   // getters and setters
}
```
 
Configure o tópico de saída
 
```
spring.kafka.bootstrapServers=localhost:9092
spring.cloud.stream.bindings.process-in-0.destination=entrada
spring.cloud.stream.bindings.process-out-0.destination=resultado
```
 
 
8. Conferido os resultados
 
```
docker-compose exec kafka  \
 kafka-console-consumer --bootstrap-server localhost:9092 --topic resultado --from-beginning
```
 
9. Inicie o projeto e gere alguns eventos como no passo (#6)

