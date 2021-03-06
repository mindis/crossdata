/*
 * Licensed to STRATIO (C) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  The STRATIO (C) licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.stratio.crossdata.server.coordinator

import com.stratio.crossdata.common.exceptions.ExecutionException
import com.stratio.crossdata.common.result.{MetadataResult, QueryResult, StorageResult}
import com.stratio.crossdata.server.ServerActorTest
import org.apache.log4j.Logger
import org.scalatest.Suite

import scala.concurrent.duration.DurationInt


class CoordinatorActorTest extends ServerActorTest{
  this: Suite =>

  override lazy val logger = Logger.getLogger(classOf[CoordinatorActorTest])
  //lazy val system1 = ActorSystem(clusterName, config)
  val testtext="updatemylastqueryId"
  test("Should return a KO message") {
    initialize()
    within(1000 millis) {
      coordinatorActor ! "anything; this doesn't make any sense"
      val exception = expectMsgType[ExecutionException]
    }
  }

  test("Select query") {
    initialize()
    initializeTablesInfinispan()
    connectorActor !(queryId + (1), testtext)
    coordinatorActor ! selectPlannedQuery
    expectMsgType[QueryResult]
  }

  test("Storage query") {
    initialize()
    initializeTablesInfinispan()
    connectorActor !(queryId + (2), testtext)
    coordinatorActor ! storagePlannedQuery
    expectMsgType[StorageResult]
  }

  test("Metadata query") {
    initialize()
    connectorActor !(queryId + (3), testtext)
    coordinatorActor ! metadataPlannedQuery0
    expectMsgType[MetadataResult]

    initializeTablesInfinispan()
    connectorActor !(queryId + (4), testtext)
    coordinatorActor ! metadataPlannedQuery1
    expectMsgType[MetadataResult]

  }

}
