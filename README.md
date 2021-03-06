# Mod2-P2
Práctica 2 - Testing con Spring y Node.js

## Parte 1: Testing Java con Spring
Implementar las pruebas necesarias para comprobar la funcionalidad de una librería online. El código de la aplicación se proporciona en el Aula Virtual: **Java Enunciado**

### Tests E2E con RESTAssured
1. Verificar que se recuperan todos los libros (role: guest)
2. Verificar que se añade un nuevo libro y que se ha creado. (role: user)
3. Verificar que se borrar un libro y que se ha borrado. (role: admin)

### Tests con WebTestClient (Opcional)
Para los ejercicios anteriores utilizando WebTestClient:
1. Realizar tests unitarios.
2. Realizar test API REST.

### Consideraciones
- Tests independientes. No deben verse afectados por la información que otros tests pueden haber modificado.
- La aplicación utiliza **HTTPS** y **Basic Auth**.
- Se valorará la modularización de los tests en paquetes y clases.
- En los unit-test, es **obligatorio el uso de mocks**, pues la persistencia se realiza con una bbdd H2 y no queremos evitar ese tipo de test.

### Material de ayuda
Código de ayuda para manejar los test de API con autenticación.

#### Autenticación en RestAssured
En necesario añadir en el `given` la autenticación. En este caso, las credenciales de base de datos.
```java
given()
  .auth()
    .basic("user", "pass")
```

#### HTTPS en RestAssured
Es necesario sobreescribir la URL base a la que "atacarán" los test, forzando **HTTPS**. Por defecto, no acepta certificados autofirmados, por lo que es necesario relajar esta restricción.
```java
@BeforeEach
public void setUp() {
  RestAssured.port = port;
  RestAssured.useRelaxedHTTPSValidation();
  RestAssured.baseURI = "https://localhost:" + port;
}
```

#### Autenticación en WebTestClient
Es necesario añadir un modificador antes de la petición (GET, POST, DELETE):
```java
.mutate()
  .filter(basicAuthentication("user", "pass")).build()
```

#### HTTPS en WebTestClient
Se proporciona la clase **TestConfiguration.java** que inicializa el **WebTestClient** con **HTTPS**

## Parte 2: Testing con Node y Express
Implementar las pruebas necesarias para comprobar la funcionalidad de una aplicación de películas. Se proporciona el código en el Aula Virtual **Node enunciado**

### Test de API REST mockeando la conexion BBDD
1. Añadir nueva película
2. Recuperar todas las películas

### Test de API REST utilizando TestContainers
1. Añadir nueva película
2. Recuperar todas las películas

### Consideraciones
- Tests independientes. No deben verse afectados por la información que otros tests pueden haber modificado.
- Se valorará la modularización de los tests en paquetes y clases.
- Se debe utilizar Jest y Supertest.
- La aplicación usa una base de datos **DynamoDB**, normalmente gestionada por el
proveedor de servicios cloud Amazon Web Services. Es posible ejecutar la
aplicación en local levantando una instancia de esta base de datos a través de este
comando (solo es necesario tener instalado Docker):
    > $ docker run --rm -p 8000:8000 -d amazon/dynamodb-local:1.13.6
- En los unit-test, es **obligatorio el uso de mocks**, pues la persistencia se realiza con una bbdd H2 y no queremos evitar ese tipo de test.
- En los test de API REST. no queremos usar una base de datos existente (ya sea
remota o local). Para ello, es obligatorio el uso de TestContainers.

### Material de ayuda
- Configuración del cliente **DynamoDB (AWS)**

Para utilizar servicios de AWS, como DynamoDB, ya sea en local o remoto, es necesario
realizar una configuración del cliente de AWS antes de lanzar la aplicación (server.js):

```javascript
AWS.config.update({
    region: process.env.AWS_REGION || 'local',
    endpoint: process.env.AWS_DYNAMO_ENDPOINT || 'http://localhost:8000',
    accessKeyId: "xxxxxx", // No es necesario poner nada aquí
    secretAccessKey: "xxxxxx" // No es necesario poner nada aquí
});
```
> Esta configuración, si no se proporciona, por defecto fallará.

- Configuración del schema en DynamoDB
DynamoDB es una base de datos NoSQL similar a MongoDB. A diferencia de este, requiere
declarar un Schema para poder crear una tabla. Esta creación se hace de forma posterior a
la configuración del cliente mencionada en el punto anterior, pero antes de lanzar la
aplicación (server.js). Para ello utiliza el siguiente método (asíncrono):
createTableIfNotExist("films");

## Formato de entrega
La práctica se entregará teniendo en cuenta los siguientes aspectos:
- La práctica se entregará como un fichero .zip. Este fichero contendrá dos carpetas:
    - Java: La aplicación Java junto a los test desarrollados
    - Node: La aplicación Node.js junto a los test desarrollados
- El nombre del fichero .zip será el correo URJC del alumno (sin @alumnos.urjc.es).
- El proyecto se puede crear con cualquier editor o IDE.
Las prácticas se podrán realizar de forma individual o por parejas. En caso de que la
práctica se haga por parejas:
- Sólo será entregada por uno de los alumnos
- El nombre del fichero .zip contendrá el correo de ambos alumnos separado por
guión. Por ejemplo p.perezf2021-z.gonzalez2021.zip