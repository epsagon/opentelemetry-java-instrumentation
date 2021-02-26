/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.awssdk.v2_2;

import static io.opentelemetry.instrumentation.awssdk.v2_2.AwsSdkRequestType.DynamoDB;
import static io.opentelemetry.instrumentation.awssdk.v2_2.AwsSdkRequestType.Kinesis;
import static io.opentelemetry.instrumentation.awssdk.v2_2.AwsSdkRequestType.S3;
import static io.opentelemetry.instrumentation.awssdk.v2_2.AwsSdkRequestType.SQS;
import static io.opentelemetry.instrumentation.awssdk.v2_2.FieldMapping.request;
import static io.opentelemetry.instrumentation.awssdk.v2_2.FieldMapping.response;

import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
import software.amazon.awssdk.core.SdkRequest;

/**
 * Temporary solution - maps only DynamoDB attributes. Final solution should be generated from AWS
 * SDK automatically
 * (https://github.com/open-telemetry/opentelemetry-java-instrumentation/issues/2291).
 */
enum AwsSdkRequest {
  // generic requests
  DynamoDbRequest(DynamoDB, "DynamoDbRequest"),
  S3Request(S3, "S3Request"),
  SqsRequest(SQS, "SqsRequest"),
  KinesisRequest(Kinesis, "KinesisRequest"),
  // specific requests
  BatchGetItem(
      DynamoDB,
      "BatchGetItemRequest",
      request("aws.dynamodb.table_names", "RequestItems"),
      response("aws.dynamodb.consumed_capacity", "ConsumedCapacity")),
  BatchWriteItem(
      DynamoDB,
      "BatchWriteItemRequest",
      request("aws.dynamodb.table_names", "RequestItems"),
      response("aws.dynamodb.consumed_capacity", "ConsumedCapacity"),
      response("aws.dynamodb.item_collection_metrics", "ItemCollectionMetrics")),
  CreateTable(
      DynamoDB,
      "CreateTableRequest",
      request("aws.dynamodb.global_secondary_indexes", "GlobalSecondaryIndexes"),
      request("aws.dynamodb.local_secondary_indexes", "LocalSecondaryIndexes"),
      request(
          "aws.dynamodb.provisioned_throughput.read_capacity_units",
          "ProvisionedThroughput.ReadCapacityUnits"),
      request(
          "aws.dynamodb.provisioned_throughput.write_capacity_units",
          "ProvisionedThroughput.WriteCapacityUnits")),
  DeleteItem(
      DynamoDB,
      "DeleteItemRequest",
      response("aws.dynamodb.consumed_capacity", "ConsumedCapacity"),
      response("aws.dynamodb.item_collection_metrics", "ItemCollectionMetrics")),
  GetItem(
      DynamoDB,
      "GetItemRequest",
      request("aws.dynamodb.projection_expression", "ProjectionExpression"),
      response("aws.dynamodb.consumed_capacity", "ConsumedCapacity"),
      request("aws.dynamodb.consistent_read", "ConsistentRead")),
  ListTables(
      DynamoDB,
      "ListTablesRequest",
      request("aws.dynamodb.exclusive_start_table_name", "ExclusiveStartTableName"),
      response("aws.dynamodb.table_count", "TableNames"),
      request("aws.dynamodb.limit", "Limit")),
  PutItem(
      DynamoDB,
      "PutItemRequest",
      response("aws.dynamodb.consumed_capacity", "ConsumedCapacity"),
      response("aws.dynamodb.item_collection_metrics", "ItemCollectionMetrics")),
  Query(
      DynamoDB,
      "QueryRequest",
      request("aws.dynamodb.attributes_to_get", "AttributesToGet"),
      request("aws.dynamodb.consistent_read", "ConsistentRead"),
      request("aws.dynamodb.index_name", "IndexName"),
      request("aws.dynamodb.limit", "Limit"),
      request("aws.dynamodb.projection_expression", "ProjectionExpression"),
      request("aws.dynamodb.scan_index_forward", "ScanIndexForward"),
      request("aws.dynamodb.select", "Select"),
      response("aws.dynamodb.consumed_capacity", "ConsumedCapacity")),
  Scan(
      DynamoDB,
      "ScanRequest",
      request("aws.dynamodb.attributes_to_get", "AttributesToGet"),
      request("aws.dynamodb.consistent_read", "ConsistentRead"),
      request("aws.dynamodb.index_name", "IndexName"),
      request("aws.dynamodb.limit", "Limit"),
      request("aws.dynamodb.projection_expression", "ProjectionExpression"),
      request("aws.dynamodb.segment", "Segment"),
      request("aws.dynamodb.select", "Select"),
      request("aws.dynamodb.total_segments", "TotalSegments"),
      response("aws.dynamodb.consumed_capacity", "ConsumedCapacity"),
      response("aws.dynamodb.count", "Count"),
      response("aws.dynamodb.scanned_count", "ScannedCount")),
  UpdateItem(
      DynamoDB,
      "UpdateItemRequest",
      response("aws.dynamodb.consumed_capacity", "ConsumedCapacity"),
      response("aws.dynamodb.item_collection_metrics", "ItemCollectionMetrics")),
  UpdateTable(
      DynamoDB,
      "UpdateTableRequest",
      request("aws.dynamodb.attribute_definitions", "AttributeDefinitions"),
      request("aws.dynamodb.global_secondary_index_updates", "GlobalSecondaryIndexUpdates"),
      request(
          "aws.dynamodb.provisioned_throughput.read_capacity_units",
          "ProvisionedThroughput.ReadCapacityUnits"),
      request(
          "aws.dynamodb.provisioned_throughput.write_capacity_units",
          "ProvisionedThroughput.WriteCapacityUnits"));

  private final AwsSdkRequestType type;
  private final String requestClass;
  private final Map<FieldMapping.Type, List<FieldMapping>> fields;

  AwsSdkRequest(AwsSdkRequestType type, String requestClass, FieldMapping... fields) {
    this.type = type;
    this.requestClass = requestClass;
    this.fields = FieldMapping.groupByType(fields);
  }

  @Nullable
  static AwsSdkRequest ofSdkRequest(SdkRequest request) {
    // try request type
    AwsSdkRequest result = ofType(request.getClass().getSimpleName());
    // try parent - generic
    if (result == null) {
      result = ofType(request.getClass().getSuperclass().getSimpleName());
    }
    return result;
  }

  private static AwsSdkRequest ofType(String typeName) {
    for (AwsSdkRequest type : values()) {
      if (type.requestClass.equals(typeName)) {
        return type;
      }
    }
    return null;
  }

  List<FieldMapping> fields(FieldMapping.Type type) {
    return fields.get(type);
  }

  AwsSdkRequestType type() {
    return type;
  }
}