# soap-multilang

Clientes SOAP en 8 lenguajes de programación — Producto 2 Lista de cotejo 2

**Alumno:** Luis Joel Gomez Herrera | **Matrícula:** 2311080808 | **Grupo:** 9A | **Docente:** Javier Nolasco Hernández

Repositorio que implementa un cliente SOAP para el servicio público
`NumberConversion` de DataAccess.com en **8 lenguajes de programación**,
con 3 versiones por lenguaje.

## Servicio SOAP
`https://www.dataaccess.com/webservicesserver/NumberConversion.wso?WSDL`

## Versiones por lenguaje

| Versión | Descripción |
|---------|-------------|
| `clisoap1` | Cliente SOAP básico — retorna el número en palabras en **inglés** |
| `clisoap2` | Cliente SOAP + traducción automática **inglés → español** |
| `conintl`  | Conversión directa a **español** usando librería/código nativo del lenguaje |

## Lenguajes y puertos

| Lenguaje | Puerto | Ejecutar |
|----------|--------|---------|
| Ruby     | 4567   | `ruby clisoap1.rb` |
| Perl     | 8083   | `perl clisoap1.pl` |
| Node.js  | 3000   | `node clisoap1.js` |
| .NET 10  | 5000   | `dotnet run` |
| Golang   | 8080   | `go run clisoap1.go` |
| Java     | 9090   | `javac Clisoap1.java && java Clisoap1` |
| C++      | 8081   | `g++ -o clisoap1 clisoap1.cpp -lcurl -lpthread && ./clisoap1` |
| Rust     | 8090   | `cargo run` |

## URL de ejemplo

```
http://localhost:<puerto>/?n=42
```

## Flujo de trabajo — GitHub Flow

```
main
 └── feature/ruby
 └── feature/perl
 └── feature/node
 └── feature/dotnet
 └── feature/golang
 └── feature/java
 └── feature/cpp
 └── feature/rust
```

Cada lenguaje se desarrolla en su propia rama `feature/<lang>` y se integra
a `main` mediante Pull Request.

## Alumno
Luis Joel Gomez Herrera · Matrícula: 2311080808  
Grupo: 9A · Docente: Javier Nolasco Hernández
