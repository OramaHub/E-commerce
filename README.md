## Docker 🐋

### Opção 1: Executar com Docker (Recomendado)

Esta opção inicia todos os serviços (PostgreSQL, API e pgweb) em containers Docker.

#### 1. Inicie os containers:

```bash
docker-compose up -d --build
```

#### 2. Verifique se os containers estão rodando:

```bash
docker-compose ps
```

Você deve ver 3 containers rodando:
- `ecommerce-postgres` - Banco de dados PostgreSQL (porta 5432)
- `ecommerce-app` - API Spring Boot (porta 8080)
- `ecommerce-pgweb` - Interface web do PostgreSQL (porta 8081)

## Documentação da API (Swagger)

- Acesse a UI em `http://localhost:8080/swagger-ui/index.html` (ou `/v3/api-docs` para o JSON).
- Para rotas protegidas, faça `POST /api/auth/login` com um cliente válido, copie o `token` retornado e clique em **Authorize** na UI. Informe `Bearer <token>` no campo `bearerAuth`.
- Endpoints públicos (ex.: catálogo de produtos e localização) podem ser testados sem autenticação.
