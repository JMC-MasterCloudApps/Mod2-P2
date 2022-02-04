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