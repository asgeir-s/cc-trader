package com.cctrader

import java.util.Map.Entry

import akka.actor._
import com.cctrader.data.TSSettings
import com.cctrader.systems.ann.oneperiodahead.ANNOnePeriodAheadCoordinator
import com.typesafe.config.{ConfigFactory, ConfigObject, ConfigValue}

import scala.collection.JavaConverters._

/**
 *
 */
object Settings2Props {

  def convert2Props(dataActor: ActorRef, tsSettingsPath: String): Props = {

    val tsSetting = loadTsSetting(tsSettingsPath)

    if (tsSetting.tsType.equals("ANNOnePeriodAhead")) {
      ANNOnePeriodAheadCoordinator.props(dataActor, tsSetting)
    }
    else {
      ANNOnePeriodAheadCoordinator.props(dataActor, tsSetting)
    }

  }

  def loadTsSetting(tsSettingPath: String): TSSettings = {
    val config = ConfigFactory.load(tsSettingPath)

    lazy val machineLearningSettings: Map[String, String] = {
      val list: Iterable[ConfigObject] = config.getObjectList("machineLearningSettings").asScala
      (for {
        item: ConfigObject <- list
        entry: Entry[String, ConfigValue] <- item.entrySet().asScala
        key = entry.getKey
        value = entry.getValue.unwrapped().toString
      } yield (key, value)).toMap
    }

    //(id: Option[Long], name: String, tsType: String, dbTable: String, startUnixTime: Int, thresholdLong: Double, thresholdShort: Double, thresholdCloseLong: Double, thresholdCloseShort: Double, stopPercentage: Int, machineLearning: Map[String, Any]) {
    TSSettings(None, config.getString("name"), config.getString("tsType"), config.getString("tsTable"), config.getInt("startUnixTime"), config.getDouble("thresholds.long"), config.getDouble("thresholds.short"), config.getDouble("thresholds.closeLong"), config.getDouble("thresholds.closeShort"), config.getInt("thresholds.stopPercentage"), config.getInt("trainingSetSize"), config.getInt("numOfCalcPerTS"), machineLearningSettings)
  }

}
