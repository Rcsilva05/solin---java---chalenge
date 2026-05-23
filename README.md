# SOLIN API — Monitoramento de Saúde de Pets

> Sprint 1 — Java Advanced — FIAP

API REST para monitoramento diário da saúde de pets, com geração automática de alertas baseados na frequência urinária. Faz parte do projeto **SOLIN**, que combina aplicativo mobile, backend Java/Spring Boot, banco de dados Oracle e um sensor IoT (ESP32 com sensor PIR) que detecta o uso de caixas de areia/tapetes higiênicos.

---

## 📌 Problema que resolvemos

Tutores de pets raramente mantêm um registro consistente da rotina diária do animal (urina, alimentação, hidratação). Sem esse histórico, o veterinário não consegue identificar padrões e o diagnóstico de problemas urinários, renais ou comportamentais fica prejudicado — especialmente em gatos, que escondem sintomas até estarem em estado avançado.

A **SOLIN API** centraliza o registro desses eventos (manualmente pelo tutor ou automaticamente via sensor IoT) e dispara alertas quando a frequência foge do padrão da espécie.

---

## 🧱 Arquitetura

```
Mobile App ──┐
             ├──► API REST (Spring Boot) ──► Oracle
ESP32 IoT ──┘
```

A API segue arquitetura em camadas: **Controller → Service → Repository → Entity**, com DTOs separando a camada de transporte da de domínio.

### Design Patterns utilizados

| Padrão | Uso no projeto |
|---|---|
| **Repository** | Interfaces Spring Data JPA para acesso a dados |
| **DTO** | Records separando request, response e entidade |
| **Service** | Regras de negócio isoladas dos controllers |
| **Builder** | Construção das entidades JPA (via Lombok `@Builder`) |
| **Strategy** | Cada regra de alerta é uma classe que implementa `RegraAlertaStrategy`. Adicionar uma nova regra não exige alterar nenhum código existente |

---

## 🛠️ Stack

- Java 17
- Spring Boot 3.2.5
- Spring Data JPA + Hibernate
- Oracle 19c (produção / FIAP) e H2 (desenvolvimento local)
- Bean Validation (Jakarta Validation)
- SpringDoc OpenAPI 3 (Swagger)
- Spring Cache (in-memory)
- Lombok
- Maven

---

## ▶️ Como rodar

### Pré-requisitos
- JDK 17+
- Maven 3.8+ (ou use o `./mvnw` que vem no projeto, caso adicionado)

### Rodando em desenvolvimento (banco H2, sem precisar de VPN)

```bash
mvn spring-boot:run
```

A aplicação sobe na porta **8080** com context path **/solin**.  
URL base: `http://localhost:8080/solin`

O perfil `dev` é o padrão. Ele já popula o banco com 3 espécies (Cão, Gato e Coelho) automaticamente.

**Console do H2** (para inspecionar tabelas):
- URL: http://localhost:8080/solin/h2
- JDBC URL: `jdbc:h2:mem:solindb`
- User: `sa` (sem senha)

### Rodando contra Oracle FIAP

1. Defina as variáveis de ambiente `DB_USER` e `DB_PASSWORD` com suas credenciais da FIAP (o `application-prod.properties` já as utiliza, sem expor senha no código).
2. Suba com o perfil prod:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```

---

## 📖 Documentação Swagger

Com a aplicação rodando:

- **UI:** http://localhost:8080/solin/swagger-ui.html
- **JSON:** http://localhost:8080/solin/v3/api-docs

---

## 📬 Coleção Postman

Em `documentos/postman/SOLIN_API.postman_collection.json`. Importe no Postman e a coleção já vem com a variável `{{baseUrl}}` apontando para `http://localhost:8080/solin`.

### Sequência sugerida de testes

1. **Espécies** → `GET /api/especies` (devem aparecer Cão, Gato, Coelho)
2. **Tutores** → `POST /api/tutores` (cadastrar Maria Silva)
3. **Pets** → `POST /api/pets` (cadastrar Rex usando tutorId=1, especieId=1)
4. **Eventos** → `POST /api/eventos` com `dataHora` antiga para disparar alerta
5. **Alertas** → `GET /api/alertas/pet/1` (deve listar o alerta gerado)

---

## 🚨 Regras de alerta (Strategy Pattern)

Cada espécie tem um `horasMaximasSemUrinar` configurado (Cão = 8h, Gato = 24h, Coelho = 12h).

| Regra | Quando dispara |
|---|---|
| `RegraSemUrinarAmarelo` | Entre `limite` e `2x limite` horas sem registro de urina |
| `RegraSemUrinarVermelho` | A partir de `2x limite` horas |

> **Como adicionar uma nova regra:** crie uma classe `@Component` que implemente `RegraAlertaStrategy`. O Spring injeta automaticamente no `AlertaService`. Nenhum código existente precisa ser alterado.

---

## ✅ Requisitos atendidos (Sprint 1)

- [x] Entidades JPA com relacionamentos (`@OneToMany`, `@ManyToOne`)
- [x] API REST seguindo princípios RESTful (verbos HTTP, status codes, plural, Location header em POST)
- [x] **Design Patterns**: Repository, DTO, Service, Builder, Strategy
- [x] **Paginação** (`Pageable`, `Page<T>`)
- [x] **Ordenação** (via `sort=campo,direcao` nos query params)
- [x] **Busca com parâmetros** (filtros opcionais combinados em JPQL)
- [x] **Cache** (`@Cacheable` / `@CacheEvict`)
- [x] **Bean Validation** (`@NotBlank`, `@Email`, `@Pattern`, etc.)
- [x] **Tratamento de exceções** (`@RestControllerAdvice` global)
- [x] **DTOs** (records separados para request/response)
- [x] **Swagger** (SpringDoc OpenAPI 3)
- [x] **Coleção Postman** exportada
- [x] Suporte a Oracle e H2 (perfis)
- [x] Spring JPA Query Methods + JPQL

---

## 📂 Estrutura do projeto

```
src/main/java/br/com/fiap/solin/
├── config/         → OpenAPI, DataSeeder
├── controller/     → 5 controllers REST
├── dto/
│   ├── request/    → DTOs de entrada com Bean Validation
│   └── response/   → DTOs de saída
├── entity/         → 5 entidades JPA
├── enums/          → TipoEvento, NivelAlerta, etc.
├── exception/      → Handler global + exceptions customizadas
├── mapper/         → Conversão entity ↔ DTO
├── repository/     → Spring Data JPA
├── service/        → Regras de negócio
└── strategy/       → Regras de alerta (Strategy Pattern)
```

---

## 📸 Evidências de Testes

Os endpoints foram testados em **duas ferramentas**: Swagger UI e Postman, comprovando o funcionamento completo do CRUD e a persistência dos dados.

### Documentação Swagger
![Swagger UI](documentos/Prints/swagger.png)

### Aplicação rodando
![Aplicação rodando](documentos/Prints/aplicacao-crud.png)

---

### Listar (GET)
**Swagger:**
![Swagger - listar](documentos/Prints/crud-get-lista.png)

**Postman:**
![Postman - listar espécies](documentos/Prints/postman-especie-lista.png)

---

### Buscar por ID (GET)
**Swagger:**
![Swagger - buscar por id](documentos/Prints/get-api-especie-id.png)

**Postman:**
![Postman - buscar alerta por id](documentos/Prints/postman-alerta-id.png)

---

### Cadastrar (POST)
**Postman - Espécie:**
![Postman - cadastrar espécie](documentos/Prints/postman-especie-post.png)

**Postman - Tutor:**
![Postman - cadastrar tutor](documentos/Prints/postman-tutor-post.png)

**Postman - Pet:**
![Postman - cadastrar pet](documentos/Prints/postman-pet-post.png)

**Postman - Evento:**
![Postman - cadastrar evento](documentos/Prints/postman-evento-post.png)

---

### Atualizar (PUT)
**Swagger:**
![Swagger - atualizar](documentos/Prints/put-crud.png)

---

### Excluir (DELETE)
**Swagger:**
![Swagger - excluir](documentos/Prints/delete-crud.png)

**Confirmação após exclusão (404 — recurso removido):**
![Swagger - get após delete](documentos/Prints/get-no-id-apagado-no-delete.png)

---

### Listagens complementares (Postman)
**Listar Tutores:**
![Postman - listar tutores](documentos/Prints/postman-tutor-lista.png)

**Listar Pets:**
![Postman - listar pets](documentos/Prints/postman-pet-lista.png)

**Listar Eventos:**
![Postman - listar eventos](documentos/Prints/postman-evento-lista.png)

**Listar Alertas:**
![Postman - listar alertas](documentos/Prints/postman-alerta-lista.png)

---

### Tratamento de erros
A API trata exceções de forma adequada, retornando status HTTP e mensagens claras.

![Erro tratado](documentos/Prints/delete-crud-erro.png)

---

## 👥 Equipe

| Nome | RM 

| Natália Cristina | RM564099 
| Nickolas Davi | RM564105 
| Rodrigo Silva | RM565162 
| Samara Vilela | RM566133 
| Otávio Ferreira | RM565960 

Turma: **2TDSR** — FIAP — 2026