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

package com.stratio.crossdata.connectors
import akka.actor.{ActorLogging, ActorRef, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{ClusterDomainEvent, MemberEvent}
import akka.util.Timeout
import com.stratio.crossdata
import com.stratio.crossdata.common.connector.{IConnector, IMetadataEngine, IResultHandler}
import com.stratio.crossdata.common.exceptions.ExecutionException
import com.stratio.crossdata.common.result._
import com.stratio.crossdata.communication._
import org.apache.log4j.Logger
import scala.collection.mutable.{ListMap, Map}
import scala.concurrent.duration.DurationInt
import com.stratio.crossdata.communication.CreateCatalog
import com.stratio.crossdata.communication.CreateIndex
import com.stratio.crossdata.communication.replyConnectorName
import com.stratio.crossdata.communication.Update
import akka.cluster.ClusterEvent.MemberRemoved
import com.stratio.crossdata.communication.IAmAlive
import com.stratio.crossdata.communication.Execute
import akka.cluster.ClusterEvent.MemberUp
import com.stratio.crossdata.communication.ACK
import com.stratio.crossdata.communication.getConnectorName
import com.stratio.crossdata.communication.CreateTableAndCatalog
import com.stratio.crossdata.communication.AlterTable
import scala.Some
import com.stratio.crossdata.communication.Truncate
import com.stratio.crossdata.communication.DropIndex
import com.stratio.crossdata.communication.CreateTable
import com.stratio.crossdata.communication.InsertBatch
import akka.cluster.ClusterEvent.CurrentClusterState
import com.stratio.crossdata.communication.AsyncExecute
import com.stratio.crossdata.communication.DeleteRows
import akka.cluster.ClusterEvent.UnreachableMember
import com.stratio.crossdata.communication.HeartbeatSig
import com.stratio.crossdata.communication.DropTable
import com.stratio.crossdata.communication.Insert
import com.stratio.crossdata.common.data.ClusterName

object State extends Enumeration {
  type state = Value
  val Started, Stopping, Stopped = Value
}
object ConnectorActor {
  def props(connectorName: String, connector: IConnector): Props = Props(new ConnectorActor
  (connectorName, connector))
}
class ConnectorActor(connectorName: String, conn: IConnector) extends HeartbeatActor with
ActorLogging with IResultHandler{

  override lazy val logger = Logger.getLogger(classOf[ConnectorActor])

  logger.info("Lifting connector actor")

  implicit val timeout = Timeout(20 seconds)

  //TODO: test if it works with one thread and multiple threads
  val connector = conn
  var state = State.Stopped
  var parentActorRef: Option[ActorRef] = None
  var runningJobs: Map[String, ActorRef] = new ListMap[String, ActorRef]()

  override def handleHeartbeat(heartbeat: HeartbeatSig): Unit = {

    runningJobs.foreach {
      keyval: (String, ActorRef) => keyval._2 ! IAmAlive(keyval._1)
    }
  }

  override def preStart(): Unit = {
    Cluster(context.system).subscribe(self, classOf[ClusterDomainEvent])
  }

  override def receive: Receive = super.receive orElse {
    case _: com.stratio.crossdata.communication.Start => {
      parentActorRef = Some(sender)
    }
    case connectRequest: com.stratio.crossdata.communication.Connect => {
      logger.debug("->" + "Receiving MetadataRequest")
      logger.info("Received connect command")
      connector.connect(connectRequest.credentials, connectRequest.connectorClusterConfig)
      this.state = State.Started //if it doesn't connect, an exception will be thrown and we won't get here
      sender ! ConnectResult.createConnectResult("Connected successfully"); //TODO once persisted sessionId,
    }
    case disconnectRequest: com.stratio.crossdata.communication.DisconnectFromCluster => {
      logger.debug("->" + "Receiving MetadataRequest")
      logger.info("Received disconnectFromCluster command")
      connector.close(new ClusterName(disconnectRequest.clusterName))
      this.state = State.Started //if it doesn't connect, an exception will be thrown and we won't get here
      sender ! ConnectResult.createConnectResult("Connected successfully"); //TODO once persisted sessionId,
    }
    case _: com.stratio.crossdata.communication.Shutdown => {
      logger.debug("->" + "Receiving Shutdown")
      this.shutdown()
    }
    case ex: Execute => {
      logger.info("Processing query: " + ex)
      methodExecute(ex, sender)
    }
    case aex: AsyncExecute => {
      logger.info("Processing asynchronous query: " + aex)
      methodAsyncExecute(aex, sender)
    }
    case metadataOp: MetadataOperation => {

      method1(metadataOp, sender)
    }
    case result: Result =>
      logger.debug("connectorActor receives Result with ID=" + result.getQueryId())
      parentActorRef.get ! result
    //TODO:  ManagementWorkflow
    case storageOp: StorageOperation => {
      methodStorageop(storageOp, sender)
    }
    case msg: getConnectorName => {
      logger.info(sender + " asked for my name")
      sender ! replyConnectorName(connectorName)
    }
    case MemberUp(member) => {
      logger.info("Member up")
      logger.debug("Member is Up: " + member.toString + member.getRoles + "!")
    }
    case state: CurrentClusterState => {
      logger.info("Current members: " + state.members.mkString(", "))
    }
    case UnreachableMember(member) => {
      logger.info("Member detected as unreachable: " + member)
    }
    case MemberRemoved(member, previousStatus) => {
      logger.info("Member is Removed: " + member.address + " after " + previousStatus)
    }
    case _: MemberEvent => {
      logger.info("Receiving anything else")
    }
  }
  def shutdown(): Unit = {
    logger.debug("ConnectorActor is shutting down")
    this.state = State.Stopping
    connector.shutdown()
    this.state = State.Stopped
  }

  override def processException(queryId: String, exception: ExecutionException): Unit = {
    logger.info("Processing exception for async query: " + queryId)
    val source = runningJobs.get(queryId).get
    if(source != None) {
      source ! Result.createErrorResult(exception)
    }else{
      logger.error("Exception for query " + queryId + " cannot be sent", exception)
    }
  }

  override def processResult(result: QueryResult): Unit = {
    logger.info("Processing results for async query: " + result.getQueryId)
    val source = runningJobs.get(result.getQueryId).get
    if(source != None) {
      source ! result
    }else{
      logger.error("Results for query " + result.getQueryId + " cannot be sent")
    }
  }

  private def methodExecute(ex:Execute, s:ActorRef): Unit ={

    try {
      runningJobs.put(ex.queryId, s)
      val result = connector.getQueryEngine().execute(ex.workflow)
      result.setQueryId(ex.queryId)
      s ! result
    } catch {
      case e: Exception => {
        val result = Result.createErrorResult(e)
        result.setQueryId(ex.queryId)
        s ! result
      }
      case err: Error =>
        logger.error("Error in ConnectorActor (Receiving LogicalWorkflow)")
        val result = new ErrorResult(err.getCause.asInstanceOf[Exception])
        result.setQueryId(ex.queryId)
        s ! result
    } finally {
      runningJobs.remove(ex.queryId)
    }
  }

  private def methodAsyncExecute(aex: AsyncExecute, sender:ActorRef) : Unit = {
    val asyncSender = sender
    try {
      runningJobs.put(aex.queryId, asyncSender)
      connector.getQueryEngine().asyncExecute(aex.queryId, aex.workflow, this)
      asyncSender ! ACK(aex.queryId, QueryStatus.IN_PROGRESS)
    } catch {
      case e: Exception => {
        val result = Result.createErrorResult(e)
        result.setQueryId(aex.queryId)
        asyncSender ! result
        runningJobs.remove(aex.queryId)
      }
      case err: Error =>
        logger.error("error in ConnectorActor( receiving async LogicalWorkflow )")
    }
  }

  private def method1(metadataOp: MetadataOperation, s: ActorRef): Unit = {
    var qId: String = metadataOp.queryId
    var metadataOperation: Int = 0
    logger.info("Received queryId = " + qId)
    var result: Result = null
    try {
      val opclass = metadataOp.getClass().toString().split('.')
      val eng = connector.getMetadataEngine()

      val abc = methodOpc(opclass,  metadataOp, eng)
      qId = abc._1
      metadataOperation = abc._2
      result = MetadataResult.createSuccessMetadataResult(metadataOperation)
    } catch {
      case ex: Exception => {
        logger.error("Connector exception: " + ex.getMessage)
        result = Result.createErrorResult(ex)
      }
      case err: Error => {
        logger.error("Error in ConnectorActor(Receiving CrossdataOperation)")
        result = Result.createExecutionErrorResult("Connector exception: " + err.getMessage)
      }
    }
    result.setQueryId(qId)
    logger.info("Sending back queryId = " + qId)
    s ! result
  }

  private def methodStorageop(storageOp: StorageOperation, s: ActorRef): Unit = {
    val qId: String = storageOp.queryId
    try {
      val eng = connector.getStorageEngine()
      storageOp match {
        case Insert(queryId, clustername, table, row) => {
          eng.insert(clustername, table, row)
        }
        case InsertBatch(queryId, clustername, table, rows) => {
          eng.insert(clustername, table, rows)
        }
        case DeleteRows(queryId, clustername, table, whereClauses) => {
          eng.delete(clustername, table, whereClauses)
        }
        case Update(queryId, clustername, table, assignments, whereClauses) => {
          eng.update(clustername, table, assignments, whereClauses)
        }
        case Truncate(queryId, clustername, table) => {
          eng.truncate(clustername, table)
        }
      }
      val result = StorageResult.createSuccessFulStorageResult("STORED successfully");
      result.setQueryId(qId)
      s ! result
    } catch {
      case ex: Exception => {
        logger.error(ex)
        val result = Result.createErrorResult(ex)
        result.setQueryId(qId)
        s ! result
      }
      case err: Error => {
        logger.error("Error in ConnectorActor(Receiving StorageOperation)")
        val result = crossdata.common.result.Result.createExecutionErrorResult("Error in ConnectorActor")
        result.setQueryId(qId)
        s ! result
      }
    }
  }

  private def methodOpc(opclass: Array[String], metadataOp: MetadataOperation, eng: IMetadataEngine): (String, Int) = {

    opclass(opclass.length - 1) match {
      case "CreateTable" => {
        logger.debug("creating table from  " + self.path)
        eng.createTable(metadataOp.asInstanceOf[CreateTable].targetCluster,
          metadataOp.asInstanceOf[CreateTable].tableMetadata)
        (metadataOp.asInstanceOf[CreateTable].queryId, MetadataResult.OPERATION_CREATE_TABLE)
      }
      case "CreateCatalog" => {
        eng.createCatalog(metadataOp.asInstanceOf[CreateCatalog].targetCluster,
          metadataOp.asInstanceOf[CreateCatalog].catalogMetadata)
        (metadataOp.asInstanceOf[CreateCatalog].queryId, MetadataResult.OPERATION_CREATE_CATALOG)
      }
      case "CreateIndex" => {
       eng.createIndex(metadataOp.asInstanceOf[CreateIndex].targetCluster,
        metadataOp.asInstanceOf[CreateIndex].indexMetadata)
        (metadataOp.asInstanceOf[CreateIndex].queryId, MetadataResult.OPERATION_CREATE_INDEX)
      }
      case "DropCatalog" => {
        eng.createCatalog(metadataOp.asInstanceOf[CreateCatalog].targetCluster,
          metadataOp.asInstanceOf[CreateCatalog].catalogMetadata)
        (metadataOp.asInstanceOf[DropIndex].queryId, MetadataResult.OPERATION_DROP_CATALOG)
      }
      case "DropIndex" => {
       eng.dropIndex(metadataOp.asInstanceOf[DropIndex].targetCluster, metadataOp.asInstanceOf[DropIndex].indexMetadata)
       (metadataOp.asInstanceOf[DropIndex].queryId, MetadataResult.OPERATION_DROP_INDEX)
      }
      case "DropTable" => {
        eng.dropTable(metadataOp.asInstanceOf[DropTable].targetCluster, metadataOp.asInstanceOf[DropTable].tableName)
       (metadataOp.asInstanceOf[DropTable].queryId, MetadataResult.OPERATION_DROP_TABLE)
      }
      case "AlterTable" => {
        eng.alterTable(metadataOp.asInstanceOf[AlterTable].targetCluster, metadataOp.asInstanceOf[AlterTable]
          .tableName, metadataOp.asInstanceOf[AlterTable].alterOptions)
        (metadataOp.asInstanceOf[AlterTable].queryId, MetadataResult.OPERATION_ALTER_TABLE)
      }
      case "CreateTableAndCatalog" => {
        eng.createCatalog(metadataOp.asInstanceOf[CreateTableAndCatalog].targetCluster,
          metadataOp.asInstanceOf[CreateTableAndCatalog].catalogMetadata)
        eng.createTable(metadataOp.asInstanceOf[CreateTableAndCatalog].targetCluster,
          metadataOp.asInstanceOf[CreateTableAndCatalog].tableMetadata)
        (metadataOp.asInstanceOf[CreateTableAndCatalog].queryId, MetadataResult.OPERATION_CREATE_TABLE)
      }
    }
  }

}
