# RDS PostgreSQL
resource "aws_db_subnet_group" "postgres" {
  name       = "${var.project_name}-${var.environment}-db-subnet"
  subnet_ids = module.vpc.private_subnets

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-db-subnet"
  })
}

resource "aws_db_instance" "postgres" {
  identifier = "${var.project_name}-${var.environment}-db"

  engine         = "postgres"
  engine_version = "16"
  instance_class = var.db_instance_class

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password
  port     = 5432

  allocated_storage     = var.db_allocated_storage
  max_allocated_storage = 100
  storage_encrypted     = true

  vpc_security_group_ids = [aws_security_group.rds.id]
  db_subnet_group_name   = aws_db_subnet_group.postgres.name

  publicly_accessible    = false
  skip_final_snapshot    = var.environment != "prod"
  deletion_protection    = var.environment == "prod"
  backup_retention_period = var.environment == "prod" ? 30 : 7

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-db"
  })
}