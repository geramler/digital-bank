# Digital Bank

This repository contains a microservices-based digital banking platform for demonstrating enterprise-grade architecture with Java 25, Spring Boot, PostgreSQL, Kafka, Redis, Docker, Kubernetes, JWT, OpenAPI, tests, and GitHub Actions.

## Architecture

- API Gateway
- Auth Service
- Customer Service
- Account Service
- Transaction Service
- Transfer Service
- Notification Service
- Config Server

## Getting Started (Local Development)

1. Install Docker and Docker Compose.
2. Ensure Java 25 or newer is installed and available on your PATH (or set JAVA_HOME with the provided helper script).
3. Run `docker compose up -d`.
4. Access Swagger UI via each service.

## AWS Deployment (Terraform)

The `terraform/` directory contains infrastructure-as-code for deploying the full stack to AWS.

### Architecture

| Component | AWS Service |
|---|---|
| Compute | EKS (Kubernetes) |
| Database | RDS PostgreSQL 16 |
| Cache | ElastiCache Redis 7 |
| Messaging | MSK Kafka 3.6 |
| Container Registry | ECR |
| Networking | VPC with public/private subnets, NAT Gateway |

### Prerequisites

- [Terraform](https://developer.hashicorp.com/terraform/downloads) >= 1.5.0
- [AWS CLI](https://aws.amazon.com/cli/) configured with credentials
- [kubectl](https://kubernetes.io/docs/tasks/tools/) for interacting with EKS

### Quick Start

```bash
cd terraform

# 1. Create the S3 backend bucket and DynamoDB lock table (one-time setup)
aws s3 mb s3://digital-bank-terraform-state --region us-east-1
aws dynamodb create-table \
  --table-name digital-bank-terraform-locks \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region us-east-1

# 2. Configure variables
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your settings (especially db_password!)

# 3. Initialize and apply
terraform init
terraform plan
terraform apply

# 4. Configure kubectl
aws eks update-kubeconfig --region us-east-1 --name digital-bank-dev-eks

# 5. Deploy services to EKS
kubectl apply -f ../k8s/
```

### State Management

Terraform state is stored remotely in S3 with DynamoDB locking. The backend is configured in `versions.tf`. For first-time setup, comment out the `backend "s3"` block, run `terraform init`, then uncomment and run `terraform init -reconfigure`.

### Variables

See `variables.tf` for all configurable variables and `terraform.tfvars.example` for recommended defaults.

## Runtime Notes

- The project is built and tested with Java 25-compatible bytecode in this workspace (Spring Boot 4.0.0).
- Use the helper script [set-java-home.sh](set-java-home.sh) to point JAVA_HOME to the installed JDK for local development.
