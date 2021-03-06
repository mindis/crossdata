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

package com.stratio.crossdata.server.actors

import akka.actor.{ReceiveTimeout, RootActorPath, ActorLogging, Actor, Props, Address}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{ClusterMetricsChanged, MemberEvent, MemberExited,
MemberRemoved, UnreachableMember, CurrentClusterState, MemberUp, ClusterDomainEvent}
import com.stratio.crossdata.common.connector.ConnectorClusterConfig
import com.stratio.crossdata.common.data
import com.stratio.crossdata.common.data.ConnectorName
import com.stratio.crossdata.common.result.{Result, ConnectResult}
import com.stratio.crossdata.common.utils.StringUtils
import com.stratio.crossdata.communication.{replyConnectorName, getConnectorName,Connect}
import com.stratio.crossdata.core.execution.ExecutionManager
import com.stratio.crossdata.core.metadata.MetadataManager
import org.apache.log4j.Logger
import com.stratio.crossdata.common.data.ConnectorStatus

import scala.collection.JavaConversions._
import scala.collection.mutable
import com.stratio.crossdata.common.statements.structures.SelectorHelper
import java.util

object ConnectorManagerActor {
  def props(sharedMemory: util.HashSet[Address]): Props = Props(new ConnectorManagerActor(sharedMemory))
}

class ConnectorManagerActor(sharedMemory: util.HashSet[Address]) extends Actor with ActorLogging {

  lazy val logger = Logger.getLogger(classOf[ConnectorManagerActor])
  logger.info("Lifting connector manager actor")
  val coordinatorActorRef = context.actorSelection("../CoordinatorActor")
  var connectorsAlreadyReset = false

  log.info("Lifting coordinator actor")
  try {
    val connectors = MetadataManager.MANAGER.getConnectorNames(ConnectorStatus.ONLINE)
    MetadataManager.MANAGER.setConnectorStatus(connectors, ConnectorStatus.OFFLINE)
  } catch {
    case e: Exception => {
      log.error("Couldn't set connectors to OFFLINE")
    }
  }

  override def preStart(): Unit = {
    Cluster(context.system).subscribe(self, classOf[ClusterDomainEvent])
  }

  override def postStop(): Unit = {
    Cluster(context.system).unsubscribe(self)
  }

  def receive : Receive= {

    /**
     * A new actor connects to the cluster. If the new actor is a connector, we requests its name.
     */
    //TODO Check that new actors are recognized and their information stored in the MetadataManager
    case mu: MemberUp => {
      logger.info("Member is Up: " + mu.toString + mu.member.getRoles)
      val it = mu.member.getRoles.iterator()
      while (it.hasNext()) {
        val role = it.next()
        role match {
          case "connector" => {
            if(!sharedMemory.contains(mu.member.address)){
              sharedMemory.add(mu.member.address)
              logger.debug("Asking its name to the connector " + mu.member.address)
              val connectorActorRef = context.actorSelection(RootActorPath(mu.member.address) / "user" / "ConnectorActor")
              connectorActorRef ! getConnectorName()
            }
          }
          case _ => {
            logger.debug(mu.member.address + " has the role: " + role)
          }
        }
      }
    }

    /**
     * CONNECTOR answers its name.
     */
    case msg: replyConnectorName => {
      sharedMemory.remove(sender.path.address)
      logger.info("Connector Name " + msg.name + " received from " + sender)
      val actorRefUri = StringUtils.getAkkaActorRefUri(sender)
      logger.info("Registering connector at: " + actorRefUri)
      val connectorName = new ConnectorName(msg.name)
      ExecutionManager.MANAGER.createEntry(actorRefUri, connectorName, true)

      MetadataManager.MANAGER.addConnectorRef(connectorName, actorRefUri)

      val connectorMetadata = MetadataManager.MANAGER.getConnector(connectorName)
      val clusterProps = connectorMetadata.getClusterProperties

      if((clusterProps != null) && (!clusterProps.isEmpty)){
        for(clusterProp <- clusterProps.entrySet()){
          val clusterName = clusterProp.getKey
          val optsCluster = MetadataManager.MANAGER.getCluster(clusterName).getOptions;

          val clusterMetadata = MetadataManager.MANAGER.getCluster(clusterName)
          val datastoreName = clusterMetadata.getDataStoreRef;
          val datastoreMetadata = MetadataManager.MANAGER.getDataStore(datastoreName)
          val optsDatastore = datastoreMetadata.getClusterAttachedRefs.get(clusterName)

          val clusterConfig = new ConnectorClusterConfig(clusterName,
            SelectorHelper.convertSelectorMapToStringMap(optsCluster),
            SelectorHelper.convertSelectorMapToStringMap(optsDatastore.getProperties))

          clusterConfig.setDataStoreName(clusterMetadata.getDataStoreRef)

          sender ! new Connect(null, clusterConfig)
        }
      }
      MetadataManager.MANAGER.setConnectorStatus(connectorName, ConnectorStatus.ONLINE)

    }

    case c: ConnectResult => {
      logger.info("Connect result from " + sender + " => " + c.getSessionId)
    }

    //Pass the message to the connectorActor to extract the member in the cluster
    case state: CurrentClusterState => {
      logger.info("Current members: " + state.members.mkString(", "))
      if(!connectorsAlreadyReset){
        var foundServers = mutable.HashSet[Address]()
        val members = state.getMembers
        for(member <- members){
          logger.info("Address: " + member.address + ", roles: " + member.getRoles )
          if(member.getRoles.contains("server")){
            foundServers += member.address
            logger.info("New server added. Size: " + foundServers.size)
          }
        }
        if(foundServers.size == 1){
          logger.info("Resetting Connectors status")
          val connectors = MetadataManager.MANAGER.getConnectorNames(data.ConnectorStatus.ONLINE)
          MetadataManager.MANAGER.setConnectorStatus(connectors, data.ConnectorStatus.OFFLINE)
          connectorsAlreadyReset = true
        }
      }
    }

    case member: UnreachableMember => {
      logger.info("Member detected as unreachable: " + member)
      //TODO Process UnreachableMember
    }

    case member: MemberRemoved => {
      logger.info("Member is Removed: " + member.member.address)
      logger.info("Member info: " + member.toString)
      val actorRefUri = StringUtils.getAkkaActorRefUri(member.member.address)
      val connectorName = ExecutionManager.MANAGER.getValue(actorRefUri + "/user/ConnectorActor/")
      MetadataManager.MANAGER.setConnectorStatus(connectorName.asInstanceOf[ConnectorName],
        data.ConnectorStatus.OFFLINE)
    }

    case member: MemberExited => {
      logger.info("Member is exiting: " + member.member.address)
      val actorRefUri = StringUtils.getAkkaActorRefUri(sender)
      val connectorName = ExecutionManager.MANAGER.getValue(actorRefUri)
      MetadataManager.MANAGER.setConnectorStatus(connectorName.asInstanceOf[ConnectorName],
        data.ConnectorStatus.SHUTTING_DOWN)
    }

    case _: MemberEvent => {
      logger.info("Receiving anything else")
      //TODO Process MemberEvent
    }

    case clusterMetricsChanged: ClusterMetricsChanged => {

    }

    case clusterDomainEvent: ClusterDomainEvent => {
      logger.info("ClusterDomainEvent: " + clusterDomainEvent)
      //TODO Process ClusterDomainEvent
    }

    case ReceiveTimeout => {
      logger.info("ReceiveTimeout")
      //TODO Process ReceiveTimeout
    }

    case unknown: Any=> {
      sender ! Result.createUnsupportedOperationErrorResult("Not recognized object")
      logger.error("Unknown event: " + unknown)
    }
  }

}
