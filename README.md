# mockingbird

**Generates fake, random JSON from JSON schemas.**

#### Build

Using `maven`:

```bash
mvn clean install
```

Packed JAR will be available at _target/mockingbird-jar-with-dependencies.jar_.

#### Run

Generate random JSON objects, one per line, from the provided JSON schema file.

For example, try:

```bash
java \
  -jar target/mockingbird-jar-with-dependencies.jar \
  src/main/resources/com/crimsonhexagon/mockingbird/tweet.json
```

