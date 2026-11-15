# Prodavnica – Web Shop (Spring Boot + Thymeleaf + MySQL)

Klasična server-side rendered (SSR) web prodavnica: katalog proizvoda, korpa, kreiranje
porudžbina, korisnički nalozi i administrativni deo.

- **Izvorni kod:** engleski jezik (klase, metode, paketi, tabele, kolone, komentari)
- **Korisnički interfejs:** srpski jezik, latinica (`messages.properties`, UTF-8)

Arhitektura, struktura projekta, ER dijagram i pregled funkcionalnosti nalaze se u
[`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

## Tehnologije

| Oblast | Tehnologija |
|---|---|
| Jezik | Java 21 |
| Framework | Spring Boot 3.5 (Spring MVC, Spring Data JPA / Hibernate, Spring Security, Bean Validation) |
| Prikaz | Thymeleaf + Bootstrap 5 (CDN), HTML5, CSS3 |
| Baza | MySQL 8+ (testovi: H2 u memoriji) |
| Build | Maven |

## Preduslovi

- JDK 21
- Maven 3.9+
- MySQL 8+ (ili Docker)
- Pristup internetu u browseru (Bootstrap i ikonice se učitavaju sa CDN-a)

## Pokretanje

### 1. Baza podataka

**Varijanta A – Docker (najbrže):**

```bash
docker compose up -d
```

Pokreće MySQL na `localhost:3306` sa bazom `webshop` i korisnikom `webshop` / `webshop`
(iste vrednosti su podrazumevane u `application.properties`).

**Varijanta B – postojeći MySQL server:**

```bash
mysql -u root -p < database/schema.sql
```

Skripta kreira bazu `webshop` i sve tabele. Zatim kreirajte korisnika baze (primer je u komentaru
na vrhu skripte) ili podesite svoje pristupne podatke (korak 2).

> Pokretanje skripte nije obavezno: uz `spring.jpa.hibernate.ddl-auto=update` (podrazumevano)
> Hibernate sam kreira tabele pri prvom startu. Ako koristite `schema.sql`, možete preći na
> `ddl-auto=validate`.

### 2. Konfiguracija konekcije

Podešavanja su u `src/main/resources/application.properties`. Pristupni podaci mogu da se zadaju
i kroz promenljive okruženja, bez izmene fajla:

```bash
export DB_URL="jdbc:mysql://localhost:3306/webshop?useUnicode=true&characterEncoding=UTF-8"
export DB_USERNAME=root
export DB_PASSWORD=
```

### 3. Start aplikacije

```bash
mvn spring-boot:run
```

ili

```bash
mvn clean package
java -jar target/webshop-1.0.0.jar
```

Aplikacija je dostupna na <http://localhost:8080>.

### 4. Početni podaci i nalozi

Pri prvom startu (kada je baza prazna i `app.seed.enabled=true`) automatski se kreiraju početni
nalozi, 4 kategorije i 12 proizvoda. Isti podaci postoje i kao SQL skripta
(`database/data.sql`) za ručni unos – koristite jedno ili drugo.

| Uloga | Korisničko ime | Lozinka |
|---|---|---|
| ADMIN | `admin` | `admin123` |
| CUSTOMER | `kupac` | `kupac123` |

Administracija: <http://localhost:8080/admin>

## Lozinke: razvojni i produkcioni režim

Prema zahtevu zadatka, u razvojnom/test okruženju lozinke se čuvaju **kao običan tekst**:

```properties
app.security.password-encoding=plain
```

Za produkciju je predviđen BCrypt – dovoljno je promeniti podešavanje:

```properties
app.security.password-encoding=bcrypt
```

Napomene:

- Režim se bira pre prvog pokretanja. Nalozi sačuvani u `plain` režimu ne mogu da se prijave u
  `bcrypt` režimu (i obrnuto) dok im se lozinka ponovo ne postavi.
- `database/data.sql` sadrži lozinke u običnom tekstu i važi samo za `plain` režim. U `bcrypt`
  režimu koristite automatski seed (`app.seed.enabled=true`), koji lozinke hešira.
- Obavezno promenite početne lozinke pre bilo kakve javne upotrebe.

## Važnija podešavanja (`application.properties`)

| Ključ | Značenje |
|---|---|
| `spring.datasource.url / username / password` | konekcija prema MySQL bazi |
| `spring.datasource.hikari.*` | connection pool (HikariCP) |
| `spring.jpa.hibernate.ddl-auto` | `update` (razvoj) ili `validate` (uz `schema.sql`) |
| `spring.jpa.show-sql`, `hibernate.format_sql` | ispis SQL upita u log |
| `spring.servlet.multipart.max-file-size` | najveća veličina slike (2 MB) |
| `app.upload.dir` | direktorijum za otpremljene slike (podrazumevano `uploads/`) |
| `app.security.password-encoding` | `plain` ili `bcrypt` |
| `app.seed.enabled` | automatski unos početnih podataka |

## Testovi

```bash
mvn test
```

Testovi koriste H2 bazu u memoriji (MySQL režim), pa MySQL nije potreban.

| Test | Šta pokriva |
|---|---|
| `ProductRepositoryTest` | repozitorijum: pretraga, filtriranje, paginacija, jedinstvena šifra |
| `CartServiceTest` | dodavanje/uklanjanje iz korpe, uvećanje količine, provera zaliha, tuđa korpa |
| `OrderServiceTest` | kreiranje porudžbine, smanjenje zaliha, pražnjenje korpe, istorijski podaci, otkazivanje |
| `UserServiceTest` | registracija (uvek CUSTOMER), jedinstveno korisničko ime, zaštita poslednjeg ADMIN-a |
| `FileStorageServiceTest` | bezbedan upload slika |
| `SecurityAccessTest` | autentifikacija, autorizacija po ulogama, CSRF |
| `ShopFlowControllerTest` | kontroleri: katalog, korpa, checkout, pristup tuđim porudžbinama, registracija |

## Bezbednost – kratak pregled

- Pravila pristupa su definisana u `SecurityConfig` **i** na admin kontrolerima (`@PreAuthorize`),
  ne samo sakrivanjem elemenata u šablonima.
- CSRF zaštita je uključena; Thymeleaf automatski dodaje token u svaku POST formu.
- Korpa i porudžbine se uvek učitavaju po ID-u prijavljenog korisnika; tuđi resursi vraćaju 404.
- Cene se nikada ne primaju od klijenta – čitaju se iz baze pri dodavanju u korpu i ponovo pri
  potvrdi porudžbine.
- Kreiranje porudžbine je jedna transakcija sa zaključavanjem redova proizvoda
  (`SELECT ... FOR UPDATE`) i `CHECK (stock_quantity >= 0)` ograničenjem u bazi.
- Upload: tip fajla se utvrđuje iz sadržaja (JPG/PNG/GIF/WebP), ime fajla je nasumično (UUID),
  veličina je ograničena, fajlovi se čuvaju van classpath-a.
- Poslednji aktivni ADMIN nalog ne može biti deaktiviran niti prebačen u CUSTOMER.

## Lokalizacija

Svi tekstovi interfejsa su u `src/main/resources/messages.properties`. Za novi jezik dodajte
`messages_xx.properties` i zamenite `FixedLocaleResolver` u `WebConfig` sa
`SessionLocaleResolver` + `LocaleChangeInterceptor`.

## Šta nije obuhvaćeno

Online plaćanje i integracija sa servisima za dostavu nisu deo ove faze (prema specifikaciji).
