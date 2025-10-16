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
