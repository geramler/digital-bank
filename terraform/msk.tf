# MSK Kafka
resource "aws_msk_configuration" "kafka" {
  name          = "${var.project_name}-${var.environment}-msk-config"
  kafka_versions = ["3.6.0"]
  server_properties = <<PROPERTIES
auto.create.topics.enable = true
default.replication.factor = 2
min.insync.replicas = 1
num.io.threads = 8
num.network.threads = 5
num.partitions = 3
num.replica.fetchers = 2
replica.lag.time.max.ms = 30000
socket.receive.buffer.bytes = 102400
socket.request.max.bytes = 104857600
socket.send.buffer.bytes = 102400
unclean.leader.election.enable = true
zookeeper.session.timeout.ms = 18000
PROPERTIES
}

resource "aws_msk_cluster" "kafka" {
  cluster_name           = "${var.project_name}-${var.environment}-msk"
  kafka_version          = "3.6.0"
  number_of_broker_nodes = var.msk_number_of_broker_nodes

  broker_node_group_info {
    instance_type   = var.msk_instance_type
    client_subnets  = module.vpc.private_subnets
    security_groups = [aws_security_group.msk.id]

    storage_info {
      ebs_storage_info {
        volume_size = 100
      }
    }
  }

  configuration_info {
    arn      = aws_msk_configuration.kafka.arn
    revision = aws_msk_configuration.kafka.latest_revision
  }

  encryption_info {
    encryption_in_transit {
      client_broker = "TLS_PLAINTEXT"
      in_cluster    = true
    }
  }

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-msk"
  })
}