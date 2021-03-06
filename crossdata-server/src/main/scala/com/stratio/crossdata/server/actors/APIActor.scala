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

import akka.actor.{Actor, Props}
import com.stratio.crossdata.common.ask.Command
import com.stratio.crossdata.common.result.Result
import com.stratio.crossdata.core.api.APIManager
import org.apache.log4j.Logger

object APIActor {
  def props(apiManager: APIManager): Props = Props(new APIActor(apiManager))
}

class APIActor(apiManager: APIManager) extends Actor with TimeTracker {
  override lazy val timerName = this.getClass.getName
  val log = Logger.getLogger(classOf[APIActor])

  def receive:Receive = {
    case cmd: Command => {
      log.debug("command received " + cmd.toString)
      val timer = initTimer()
      sender ! apiManager.processRequest(cmd)
      finishTimer(timer)
    }
    case _ => {
      log.info("command _ received ")
      sender ! Result.createUnsupportedOperationErrorResult("Unsupported command")
    }
  }
}
