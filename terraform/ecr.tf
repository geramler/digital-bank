# ECR Repositories for each microservice
locals {
  services = [
    "api-gateway",
    "auth-service",
    "customer-service",
    "account-service",
    "transaction-service",
    "transfer-service",
    "notification-service",
    "config-server",
  ]
}

resource "aws_ecr_repository" "services" {
  for_each = toset(local.services)

  name = "${var.project_name}/${each.key}"

  image_scanning_configuration {
    scan_on_push = true
  }

  image_tag_mutability = "MUTABLE"

  tags = merge(var.tags, {
    Name = "${var.project_name}-${each.key}-ecr"
  })
}

# Lifecycle policy to keep only the last 10 images
resource "aws_ecr_lifecycle_policy" "services" {
  for_each = aws_ecr_repository.services

  repository = each.value.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep last 10 images"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = 10
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}